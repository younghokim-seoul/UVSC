@file:OptIn(ExperimentalCoroutinesApi::class)

package com.cm.uvsc.ble

import com.cm.uvsc.di.ApplicationScope
import com.cm.uvsc.util.asDateString
import com.cm.uvsc.util.now
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleRepository @Inject constructor(
    private val bleClient: BleClient,
    @ApplicationScope private val externalScope: CoroutineScope
) {
    private val _targetDevice = MutableSharedFlow<RxBleDevice?>(replay = 1)

    private var _scanJob: Job? = null

    private var _lastSuccessfullyConnectedDevice: RxBleDevice? = null

    private val _latestPacketsMap = MutableStateFlow<Map<String, ReceivePacket>>(emptyMap())
    val latestPacketsMap: StateFlow<Map<String, ReceivePacket>> = _latestPacketsMap

    private val _scannedDevicesSet = MutableStateFlow<Set<RxBleDevice>>(emptySet())
    val scannedDevices: StateFlow<Set<RxBleDevice>> = _scannedDevicesSet


    private val _connection: SharedFlow<Pair<RxBleDevice, RxBleConnection>> = _targetDevice
        .flatMapLatest { device ->
            device?.let { bleClient.connect(it).map { connection -> device to connection } }
                ?: emptyFlow()
        }
        .onEach { (device, _) ->
            _lastSuccessfullyConnectedDevice = device
        }
        .catch { e -> Timber.e(e, "Connection stream failed") }
        .shareIn(externalScope, SharingStarted.WhileSubscribed(5000), 1)

    private val _connectionState: StateFlow<RxBleConnection.RxBleConnectionState?> = _targetDevice
        .flatMapLatest { device ->
            device?.let { bleClient.connectionStateFlow(it) } ?: flowOf(null)
        }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        _connectionState.onEach { state ->
            Timber.i("RxBleConnectionState $state")
            when (state) {
                RxBleConnection.RxBleConnectionState.CONNECTED -> {
                    sendToRetry(SetUVTime(nowDate = LocalDateTime.now().asDateString()))
                }

                RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                    resetPacket()
                    _lastSuccessfullyConnectedDevice?.let { deviceToReconnect ->
                        Timber.d("attempting auto-reconnect...")
                        connect(deviceToReconnect)
                    }

                }

                else -> {}
            }
        }.launchIn(externalScope)

        _connection
            .flatMapLatest { (_, connection) ->
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
            .catch { e -> Timber.e(e, "Notification processing error") }
            .launchIn(externalScope)
    }

    suspend fun sendToRetry(
        data: SendPacket,
        retryCount: Int = 5,
        retryDelay: Long = 500,
        ackTimeout: Long = 60_000L
    ): Boolean {
        if (_connectionState.value != RxBleConnection.RxBleConnectionState.CONNECTED) {
            Timber.w("Cannot send data: Not connected.")
            return false
        }

        repeat(retryCount) { attempt ->
            Timber.d("Sending data (Attempt ${attempt + 1}/$retryCount)...")

            val sendSuccess = sendData(data.toByteArray())
            if (!sendSuccess) {
                delay(retryDelay)
                return@repeat
            }

            val ack = runCatching {
                withTimeout(ackTimeout) {
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
        _targetDevice.tryEmit(device)
    }

    fun disconnect() {
        Timber.d("Disconnecting...")
        _lastSuccessfullyConnectedDevice = null
        _targetDevice.tryEmit(null)
    }

    private suspend fun sendData(data: ByteArray): Boolean {
        return try {
            bleClient.send(_connection.first().second, data)
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