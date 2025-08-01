@file:OptIn(ExperimentalCoroutinesApi::class)

package com.cm.uvsc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.uvsc.ble.BleClient
import com.cm.uvsc.ble.isPureAsciiText
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
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
import kotlinx.coroutines.rx3.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleClient: BleClient
) : ViewModel() {


    private val _targetDevice = MutableSharedFlow<RxBleDevice?>(replay = 1)

    private var scanJob: Job? = null

    private var pollingJob: Job? = null

    private val connection: SharedFlow<RxBleConnection> = _targetDevice
        .flatMapLatest { device ->
            device?.let {
                bleClient.connect(it)
                    // 👇 이 부분을 추가합니다.
                    .catch { e ->
                        Timber.e(e, "Connection stream failed")
                    }
            } ?: emptyFlow()
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val notifications: SharedFlow<ByteArray> = connection
        .flatMapLatest { conn ->
            bleClient.getNotificationFlow(conn, UUID.fromString(BleClient.CHARACTERISTIC_UUID))
                .catch { e -> Timber.e("Notification error", e) }
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    private val connectionState: StateFlow<RxBleConnection.RxBleConnectionState?> = _targetDevice
        .flatMapLatest { device ->
            device?.let { bleClient.connectionStateFlow(it) } ?: flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        connectionState.onEach { state ->
            Timber.d("Connection state: $state")
            when (state) {
                RxBleConnection.RxBleConnectionState.CONNECTED -> {
                    startPolling() // 연결되면 폴링 시작
                }

                RxBleConnection.RxBleConnectionState.DISCONNECTED -> {
                    disconnectTrigger() // 연결이 끊어지면 트리거
                    stopPolling()
                }

                else -> {
                    // 다른 상태 처리 필요 시 추가
                }
            }
        }.catch {
            Timber.e("Error in connection state flow", it)
        }.launchIn(viewModelScope)


        notifications.onEach {}.catch { Timber.e("Error in notifications flow", it) }
            .launchIn(viewModelScope)

    }

    private fun startPolling(intervalMillis: Long = 1000) {
        pollingJob?.cancel()

        val ticker = flow {
            while (true) {
                emit(Unit)
                delay(intervalMillis)
            }
        }


        pollingJob = connection.flatMapLatest { connection ->
            ticker.map {
                connection.readCharacteristic(
                    UUID.fromString(BleClient.CHARACTERISTIC_UUID)
                ).await()
            }
        }.filter {
            it.isPureAsciiText()
        }.onEach { bytes ->
            // 최종적으로 읽어온 데이터를 처리합니다.
            val packet = String(bytes)
            Timber.d("Polling Read: $packet")
            // TODO: 읽어온 bytes를 파싱하여 UI 상태 업데이트
        }.catch { e ->
            // 스트림 전체에서 발생하는 에러를 처리합니다.
            Timber.e("Polling stream failed.", e)
        }.launchIn(viewModelScope)
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun startScan() {
        scanJob?.cancel()
        scanJob = bleClient.startScan()
            .onEach { device -> connect(device = device) }
            .launchIn(viewModelScope)
    }

    fun stopScan() {
        scanJob?.cancel()
    }

    fun connect(device: RxBleDevice) {
        // 같은 기기 재연결을 위해 tryEmit 사용
        Timber.d("Connecting to device: ${device.name} (${device.macAddress})")
        _targetDevice.tryEmit(device)
    }

    fun disconnectTrigger() {
        _targetDevice.tryEmit(null)
    }
}