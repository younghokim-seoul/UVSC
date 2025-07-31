@file:OptIn(ExperimentalCoroutinesApi::class)

package com.cm.uvsc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.uvsc.ble.BleClient
import com.polidea.rxandroidble3.RxBleConnection
import com.polidea.rxandroidble3.RxBleDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.rx3.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleClient: BleClient
) : ViewModel() {

    private val _scannedDevices = MutableStateFlow<List<RxBleDevice>>(emptyList())
    val scannedDevices: StateFlow<List<RxBleDevice>> = _scannedDevices

    private val _targetDevice = MutableSharedFlow<RxBleDevice?>(replay = 1)

    private var scanJob: Job? = null

    val connection: SharedFlow<RxBleConnection> = _targetDevice
        .flatMapLatest { device ->
            device?.let { bleClient.connect(it) } ?: emptyFlow()
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val notifications: SharedFlow<ByteArray> = connection
        .flatMapLatest { conn ->
            bleClient.getNotificationFlow(conn, UUID.fromString(BleClient.CHARACTERISTIC_UUID))
                .catch { e -> Log.e("BleViewModel", "Notification error", e) }
        }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000))

    val connectionState: StateFlow<RxBleConnection.RxBleConnectionState?> = _targetDevice
        .flatMapLatest { device ->
            device?.let { bleClient.connectionStateFlow(it) } ?: flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    fun startScan() {
        scanJob?.cancel()
        _scannedDevices.value = emptyList()
        scanJob = bleClient.startScan()
            .onEach { device ->
                _scannedDevices.update { list -> if (list.any { it.macAddress == device.macAddress }) list else list + device }
            }.launchIn(viewModelScope)
    }

    fun stopScan() {
        scanJob?.cancel()
    }

    fun connect(device: RxBleDevice) {
        // 같은 기기 재연결을 위해 tryEmit 사용
        _targetDevice.tryEmit(device)
    }

    fun disconnect() {
        _targetDevice.tryEmit(null)
    }
}