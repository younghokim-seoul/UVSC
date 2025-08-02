@file:OptIn(ExperimentalCoroutinesApi::class)

package com.cm.uvsc.ble

import com.cm.uvsc.di.ApplicationScope
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.rx3.await
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
    private val _latestPacketsMap = MutableStateFlow<Map<String, BasePacket>>(emptyMap())
    val latestPacketsMap: StateFlow<Map<String, BasePacket>> = _latestPacketsMap


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

    private val notifications: SharedFlow<BasePacket> = connection
        .flatMapLatest { conn ->
            bleClient.getNotificationFlow(conn, UUID.fromString(BleClient.CHARACTERISTIC_UUID))
                .filter { rawBytes -> rawBytes.isPureAsciiText() }
                .map { newData -> PacketParser.parse(String(newData, Charsets.US_ASCII)) }
                .catch { e -> Timber.e("Notification error", e) }
        }
        .shareIn(externalScope, SharingStarted.WhileSubscribed(5000))

    init {
        connectionState.onEach { state ->
            Timber.i("RxBleConnectionState $state")
            when (state) {
                RxBleConnection.RxBleConnectionState.CONNECTED -> startPolling()
                RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                    stopPolling()
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


    fun startScan() {
        scanJob?.cancel()
        scanJob = bleClient.startScan()
            .onEach { device ->
                connect(device)
                stopScan()
            }
            .launchIn(externalScope) // 앱 스코프에서 실행
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


    private fun startPolling(intervalMillis: Long = 1000) {
        pollingJob?.cancel()
        Timber.d("Starting polling...")

        pollingJob = connection
            .flatMapLatest { conn ->
                flow {
                    while (true) {
                        emit(conn); delay(intervalMillis)
                    }
                }
            }
            .map { conn ->
                conn.readCharacteristic(UUID.fromString(BleClient.CHARACTERISTIC_UUID)).await()
            }
            .map { bytes ->
                String(bytes, Charsets.US_ASCII)
            }
            .onEach { parsedData ->
                Timber.d("Polling Read: $parsedData")
            }
            .catch { e -> Timber.e(e, "Polling stream failed") }
            .launchIn(externalScope)
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        Timber.d("Polling stopped.")
    }

    private fun disconnectTrigger() {
        _targetDevice.tryEmit(null)
    }

    private fun resetPacket() {
        _latestPacketsMap.value = emptyMap()
    }


}