package com.cm.uvsc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.uvsc.ble.AchPacket
import com.cm.uvsc.ble.BleRepository
import com.cm.uvsc.ble.ReceivePacket
import com.cm.uvsc.ble.SetChargeMode
import com.cm.uvsc.route.Navigator
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import com.cm.uvsc.ui.history.UvscHistory
import com.cm.uvsc.ui.home.HomeUiState
import com.cm.uvsc.ui.home.UvscInfo
import com.cm.uvsc.ui.home.toCharging
import com.cm.uvsc.ui.home.toUvscInProgress
import com.cm.uvsc.ui.receive.ReceiveData
import com.cm.uvsc.util.filterForHistory
import com.cm.uvsc.util.filterForHome
import com.cm.uvsc.util.filterForOthers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigator: Navigator,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.NoData)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _uvscHistoryList = MutableStateFlow<List<UvscHistory>>(emptyList())
    val uvscHistoryList: StateFlow<List<UvscHistory>> = _uvscHistoryList.asStateFlow()

    private val _receiveDataList = MutableStateFlow<List<ReceiveData>>(emptyList())
    val receiveDataList: StateFlow<List<ReceiveData>> = _receiveDataList.asStateFlow()

    init {
        Timber.i("bleRepository => $bleRepository")
        observeHomePacket()
        observeHistoryPacket()
    }

    fun startScan() {
        bleRepository.startScan()
    }

    fun stopScan() {
        bleRepository.stopScan()
        bleRepository.disconnect()
    }

    private fun observeHomePacket() =
        bleRepository.latestPacketsMap
            .filterForHome()
            .onEach { map ->
                map["ACS"]?.let { updateUiStateFromAcs(it) }
                map["ACHT"]?.let { updateUiStateFromAcht(it) }
                map["ACHS"]?.let { updateUiStateFromAchs(it) }
            }
            .launchIn(viewModelScope)

    private fun updateUiStateFromAcs(packet: ReceivePacket) {
        val value = packet.valueAsString.toIntOrNull() ?: return
        val info = _homeUiState.value as? UvscInfo

        _homeUiState.update { state ->
            when {
                value == 200 -> {
                    info?.toUvscInProgress(0) ?: state
                }

                value >= 1000 -> {
                    val minutes = value - 1000
                    when (state) {
                        is HomeUiState.UvscInProgress -> state.copy(progressTime = minutes)
                        else -> state
                    }
                }

                value == 100 || value == -100 -> {
                    info?.toCharging() ?: state
                }

                else -> state
            }
        }
    }

    private fun updateUiStateFromAcht(packet: ReceivePacket) {
        val value = packet.valueAsString
        _homeUiState.update {
            when (it) {
                is HomeUiState.Charging -> it.copy(recentUvscTime = value)
                is HomeUiState.UvscInProgress -> it.copy(recentUvscTime = value)
                else -> it
            }
        }
    }

    private fun updateUiStateFromAchs(packet: ReceivePacket) {
        val value = packet.valueAsString
        val (time, result, expected) = value.split(",").let {
            Triple(
                it.getOrNull(0) ?: "-",
                it.getOrNull(1) ?: "-",
                it.getOrNull(2) ?: "-"
            )
        }

        _homeUiState.update {
            when (it) {
                is HomeUiState.Charging -> it.copy(
                    uvscTime = time,
                    uvscResult = result,
                    expectedTime = expected
                )

                is HomeUiState.UvscInProgress -> it.copy(
                    uvscTime = time,
                    uvscResult = result,
                    expectedTime = expected
                )

                else -> it
            }
        }
    }

    fun toggleCharging(isOff: Boolean) {
        viewModelScope.launch {
            retryUntilReceive(packet = SetChargeMode(isOff = isOff))
        }
    }

    private suspend fun retryUntilReceive(packet: SetChargeMode) {
        val success = bleRepository.sendToRetry(data = packet, retryCount = Int.MAX_VALUE)
        if (!success) {
            Timber.w("BLE response not received for $packet after retry")
        }
    }

    private fun observeHistoryPacket() =
        bleRepository.latestPacketsMap
            .filterForHistory()
            .onEach { map -> map["ACH"]?.let { updateUvscHistoryFromPacket(it) } }
            .launchIn(viewModelScope)

    private fun updateUvscHistoryFromPacket(packet: ReceivePacket) {
        val ach = packet as? AchPacket ?: return

        val parts = ach.valueAsString.split(",")
        val index = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val date = parts.getOrNull(1) ?: ""
        val time = parts.getOrNull(2) ?: ""
        val result = parts.getOrNull(3) ?: ""
        val note = parts.getOrNull(4) ?: ""

        val entry = UvscHistory(
            index = index,
            date = date,
            time = time,
            result = result,
            note = note
        )

        _uvscHistoryList.update { current ->
            (current + entry)
                .sortedByDescending { it.index }
                .take(10)
        }
    }

    private fun observeReceiveData() =
        bleRepository.latestPacketsMap
            .filterForOthers()
            .onEach { map ->
            }
            .launchIn(viewModelScope)

    private fun updateReceiveData(packet: ReceivePacket) {
        _receiveDataList.update { currentList ->
            (currentList).sortedByDescending { it.isChecked }
        }
    }

    fun toggleReceiveDataChecked(key: String) {
        _receiveDataList.update { currentList ->
            currentList.map {
                if (it.key == key) it.copy(isChecked = !it.isChecked) else it
            }.sortedByDescending { it.isChecked }
        }
    }

    fun navigateHome() = viewModelScope.launch {
        navigator.navigate(
            route = RouteHome,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigateUvscHistory() = viewModelScope.launch {
        navigator.navigate(
            route = RouteUvscHistory,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigateRouteReceiveHistory() = viewModelScope.launch {
        navigator.navigate(
            route = RouteReceiveHistory,
            saveState = true,
            launchSingleTop = true,
        )
    }
}