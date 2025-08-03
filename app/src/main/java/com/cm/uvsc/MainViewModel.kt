package com.cm.uvsc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.uvsc.ble.AchsPacket
import com.cm.uvsc.ble.AchtPacket
import com.cm.uvsc.ble.AcsPacket
import com.cm.uvsc.ble.BleRepository
import com.cm.uvsc.ble.SetChargeMode
import com.cm.uvsc.route.Navigator
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import com.cm.uvsc.ui.history.UvscHistory
import com.cm.uvsc.ui.home.HomeUiState
import com.cm.uvsc.ui.receive.ReceiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
        updateHomeUiStateFromBle()
    }

    private fun updateHomeUiStateFromBle() {
        viewModelScope.launch {
            bleRepository.latestPacketsMap.collect { map ->
                val acs = map["ACS"] as? AcsPacket
                val acht = map["ACHT"] as? AchtPacket
                val achs = map["ACHS"] as? AchsPacket

                val acsValue = acs?.value ?: "-"
                val achtValue = acht?.value ?: "-"
                val achsParts = achs?.value?.split(",").orEmpty()

                val uvscTime = achsParts.getOrNull(0) ?: "-"
                val uvscResult = achsParts.getOrNull(1) ?: "-"
                val expectedTime = achsParts.getOrNull(2) ?: "-"

                when {
                    acsValue == "200" -> {
                        _homeUiState.value = HomeUiState.UvscInProgress(
                            progressTime = 0,
                            recentUvscTime = achtValue,
                            uvscTime = uvscTime,
                            uvscResult = uvscResult,
                            expectedTime = expectedTime
                        )
                    }

                    acsValue.toIntOrNull()?.let { it >= 1000 } == true -> {
                        val minutes = acsValue.toInt() - 1000
                        _homeUiState.value = HomeUiState.UvscInProgress(
                            progressTime = minutes,
                            recentUvscTime = achtValue,
                            uvscTime = uvscTime,
                            uvscResult = uvscResult,
                            expectedTime = expectedTime
                        )
                    }

                    acsValue == "100" || acsValue == "-100" -> {
                        _homeUiState.value = HomeUiState.Charging(
                            recentUvscTime = achtValue,
                            uvscTime = uvscTime,
                            uvscResult = uvscResult,
                            expectedTime = expectedTime
                        )
                    }

                    else -> {}
                }
            }
        }
    }

    fun setUvscState(startUvsc: Boolean) {
        viewModelScope.launch {
            retryUntilReceive(packet = SetChargeMode(isOff = startUvsc))
        }
    }

    private suspend fun retryUntilReceive(packet: SetChargeMode) {
        val success = bleRepository.sendToRetry(data = packet, retryCount = Int.MAX_VALUE)
        if (!success) {
            Timber.w("BLE response not received for $packet after retry")
        }
    }

    fun startScan() {
        bleRepository.startScan()
    }

    fun addReceiveData(data: ReceiveData) {
        _receiveDataList.update { currentList ->
            (currentList + data).sortedByDescending { it.isChecked }
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