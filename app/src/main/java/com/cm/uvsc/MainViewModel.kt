package com.cm.uvsc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.uvsc.route.Navigator
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val navigator: Navigator) : ViewModel() {

    fun navigateHome() = viewModelScope.launch {
        navigator.navigate(
            route = RouteHome,
            saveState = true,
            launchSingleTop = true,
        )
    }

    fun navigatUvscHistory() = viewModelScope.launch {
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