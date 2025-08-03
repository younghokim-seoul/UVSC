package com.cm.geofence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cm.geofence.route.Navigator
import com.cm.geofence.route.RouteGeofence
import com.cm.geofence.route.RouteHome
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

    fun navigateGeofence() = viewModelScope.launch {
        navigator.navigate(
            route = RouteGeofence,
            saveState = true,
            launchSingleTop = true,
        )
    }

}