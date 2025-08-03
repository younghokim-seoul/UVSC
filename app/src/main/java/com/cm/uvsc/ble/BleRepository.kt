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

    private var scanJob: Job? = null
    private var pollingJob: Job? = null


    /**
     * 디바이스에서 받는 패킷을 Map형태로 적재.. 각 Compose 화면 마다 필요한 데이터 Filter로 가져다쓰면될듯
     */
    private val _latestPacketsMap = MutableStateFlow<Map<String, ReceivePacket>>(emptyMap())
    val latestPacketsMap: StateFlow<Map<String, ReceivePacket>> = _latestPacketsMap


    private val connection: SharedFlow<RxBleConnection> = _targetDevice
        .flatMapLatest { device ->
            device?.let { bleClient.connect(it) } ?: emptyFlow()
        }
        .catch { e -> Timber.e(e, "Connection stream failed") }
        .shareIn(externalScope, SharingStarted.WhileSubscribed(5000), 1)

    private val connectionState: StateFlow<RxBleConnection.RxBleConnectionState?> = _targetDevice
        .flatMapLatest { device ->
            device?.let { bleClient.connectionStateFlow(it) } ?: flowOf(null)
        }
        .stateIn(externalScope, SharingStarted.WhileSubscribed(5000), null)

    private val notifications: SharedFlow<ReceivePacket> = connection
        .flatMapLatest { conn ->
            bleClient.getNotificationFlow(conn, UUID.fromString(BleClient.CHARACTERISTIC_UUID))
                .filter { rawBytes -> rawBytes.isPureAsciiText() }
                .map { newData -> PacketParser.parse(String(newData, Charsets.US_ASCII)) }
                .catch { e -> Timber.e(e, "Notification error") }
        }
        .shareIn(externalScope, SharingStarted.WhileSubscribed(5000))

    init {
        connectionState.onEach { state ->
            Timber.i("RxBleConnectionState $state")
            when (state) {
                RxBleConnection.RxBleConnectionState.CONNECTED -> {
                    sendToRetry(SetUVTime(nowDate = LocalDateTime.now().asDateString()))
                }

                RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                    resetPacket()
                    disconnectTrigger()
                }

                else -> {}
            }
        }.launchIn(externalScope)

        notifications
            .onEach { newPacket ->
                _latestPacketsMap.update { currentMap ->
                    currentMap + (newPacket.key to newPacket)
                }
                Timber.d("Packet received & map updated: $newPacket")
            }.catch { e ->
                Timber.e(e, "Notification processing error")
            }.launchIn(externalScope)
    }


    /**
     * 응답이 true일 경우에 로직 처리해주세요.
     */
    suspend fun sendToRetry(
        data: SendPacket,
        retryCount: Int = 5,
        timeoutMillis: Long = 500
    ): Boolean {
        if (connectionState.value != RxBleConnection.RxBleConnectionState.CONNECTED) {
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
        scanJob?.cancel()
        scanJob = bleClient.startScan()
            .onEach { device ->
                connect(device)
                stopScan()
            }
            .launchIn(externalScope)
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
    }

    fun connect(device: RxBleDevice) {
        Timber.d("Connecting to device: ${device.macAddress}")
        _targetDevice.tryEmit(device)
    }

    fun disconnect() {
        Timber.d("Disconnecting...")
        _targetDevice.tryEmit(null)
    }

    private suspend fun sendData(data: ByteArray): Boolean {
        return try {
            bleClient.send(connection.first(), data)
            true
        } catch (e: Exception) {
            Timber.e(e, "Single send failed")
            false
        }
    }

    private fun disconnectTrigger() {
        _targetDevice.tryEmit(null)
    }

    private fun resetPacket() {
        _latestPacketsMap.value = emptyMap()
    }


}