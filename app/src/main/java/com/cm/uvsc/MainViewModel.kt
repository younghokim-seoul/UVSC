package com.cm.uvsc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.uvsc.ble.BleRepository
import com.cm.uvsc.route.Navigator
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import com.cm.uvsc.ui.home.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var timerJob: Job? = null

    init {
        Timber.i("bleRepository => $bleRepository")
    }

    fun startUvsc() {
        timerJob?.cancel()
        val startTime = System.currentTimeMillis()
        val initialState = HomeUiState.UvscInProgress(
            progressTime = 0,
            recentUvscTime = "2025.09.08 12:34:56",
            uvscTime = "25분",
            uvscResult = "정상",
            expectedTime = "70분 이상"
        )
        _homeUiState.value = initialState
        timerJob = launchUvscTimer(startTime)
    }

    private fun launchUvscTimer(startTime: Long): Job = viewModelScope.launch {
        while (true) {
            delay(60_000)
            val elapsedMinutes = ((System.currentTimeMillis() - startTime) / 60_000).toInt()
            _homeUiState.update {
                (it as? HomeUiState.UvscInProgress)?.copy(progressTime = elapsedMinutes) ?: it
            }
        }
    }

    fun stopUvsc() {
        timerJob?.cancel()
        _homeUiState.value = HomeUiState.Charging(
            recentUvscTime = "2025.09.08 12:34:56",
            uvscTime = "25분",
            uvscResult = "정상",
            expectedTime = "70분 이상"
        )
    }

    fun startScan() {
        bleRepository.startScan()
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