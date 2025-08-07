@file:OptIn(ExperimentalCoroutinesApi::class)

package com.cm.uvsc.ble

import com.cm.uvsc.util.asDateString
import com.cm.uvsc.util.now
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@ActivityRetainedScoped
class BleRepository @Inject constructor(
    private val bleClient: BleClient,
    private val externalScope: CoroutineScope
) {

    private var _scanJob: Job? = null
    private var connectionJob: Job? = null

    private val _connection = MutableStateFlow<Pair<RxBleDevice?, RxBleConnection?>>(null to null)

    private val _latestPacketsMap = MutableStateFlow<Map<String, ReceivePacket>>(emptyMap())
    val latestPacketsMap: StateFlow<Map<String, ReceivePacket>> = _latestPacketsMap.asStateFlow()

    private val _scannedDevicesSet = MutableStateFlow<Set<RxBleDevice>>(emptySet())
    val scannedDevices: StateFlow<Set<RxBleDevice>> = _scannedDevicesSet.asStateFlow()


    private val _connectionState =
        MutableStateFlow(RxBleConnection.RxBleConnectionState.DISCONNECTED)
    val connectionState: StateFlow<RxBleConnection.RxBleConnectionState> =
        _connectionState.asStateFlow()


    suspend fun sendDynamicPacket(data: String): Boolean {
        if (_connectionState.value != RxBleConnection.RxBleConnectionState.CONNECTED) {
            Timber.w("Cannot send data: Not connected.")
            return false
        }
        val sendSuccess = sendData(data.toByteArray(Charsets.US_ASCII))
        return sendSuccess
    }

    suspend fun sendToRetry(
        data: SendPacket,
        retryCount: Int = 5,
        timeoutMillis: Long = 500
    ): Boolean {
        if (_connectionState.value != RxBleConnection.RxBleConnectionState.CONNECTED) {
            Timber.w("Cannot send data: Not connected.")
            return false
        }

        repeat(retryCount) { attempt ->
            Timber.d("Sending data (Attempt ${attempt + 1}/$retryCount)...")

            val sendSuccess = sendData(data.toByteArray())
            if (!sendSuccess) {
                delay(timeoutMillis)
                return@repeat
            }

            val ack = runCatching {
                withTimeout(timeoutMillis) {
                    latestPacketsMap
                        .filter { map ->
                            val receivedPacket = map[data.key]
                            receivedPacket?.valueAsString == data.value
                        }
                        .first()
                }
            }

            if (ack.isSuccess) {
                Timber.d("ACK received for key '${data}'. Send successful.")
                return true
            } else {
                Timber.w("ACK timeout for key '${data}'. Retrying...")
            }

        }

        Timber.e("Send failed after $retryCount retryCount.")
        return false
    }


    fun startScan() {
        _scannedDevicesSet.value = emptySet()
        _scanJob?.cancel()
        _scanJob = bleClient.startScan()
            .onEach { device ->
                _scannedDevicesSet.update { currentSet ->
                    currentSet + device
                }
            }
            .launchIn(externalScope)
    }

    fun stopScan() {
        _scanJob?.cancel()
        _scanJob = null
    }

    fun connect(device: RxBleDevice) {
        Timber.d("Connecting to device: ${device.macAddress}")

        _connection.value = device to null

        connectionJob?.cancel()

        connectionJob = externalScope.launch {

            launch {
                bleClient.connectionStateFlow(device).onEach { state ->
                    Timber.d("Connection state: $state")
                    when (state) {
                        RxBleConnection.RxBleConnectionState.CONNECTED -> {
                            _connectionState.value = state
                            sendToRetry(SetUVTime(nowDate = LocalDateTime.now().asDateString()))
                        }

                        RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                            Timber.i("Disconnected from device: ${  _connection.value}")
                            resetPacket()
                            _connection.value.first?.let { deviceToReconnect ->
                                Timber.d("attempting auto-reconnect...")
                                connect(deviceToReconnect)
                            }

                            _connectionState.value = RxBleConnection.RxBleConnectionState.DISCONNECTED

                        }

                        else -> {}
                    }
                }.launchIn(this)
            }

            bleClient.connect(device)
                .flatMapLatest { connection ->
                    _connection.value = device to connection

                    bleClient.getNotificationFlow(
                        connection,
                        UUID.fromString(BleClient.CHARACTERISTIC_UUID)
                    )
                        .filter { rawBytes -> rawBytes.isPureAsciiText() }
                        .map { bytes -> PacketParser.parse(String(bytes, Charsets.US_ASCII)) }
                }
                .onEach { newPacket ->
                    _latestPacketsMap.update { currentMap ->
                        currentMap + (newPacket.key to newPacket)
                    }
                    Timber.d("Packet received & map updated: $newPacket")
                }
                .catch { e ->
                    Timber.e(e, "Connection failed during connect")
                    _connection.value = null to null
                }
                .launchIn(this)
        }
    }

    fun disconnect() {
        Timber.d("force Disconnecting...")
        _connection.value = null to null
        connectionJob?.cancel()
    }

    private suspend fun sendData(data: ByteArray): Boolean {
        return try {
            bleClient.send(_connection.value.second!!, data)
            true
        } catch (e: Exception) {
            Timber.e(e, "Single send failed")
            false
        }
    }

    private fun resetPacket() {
        _latestPacketsMap.value = emptyMap()
    }


}