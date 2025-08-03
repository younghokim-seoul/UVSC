package com.cm.geofence.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cm.geofence.MainNavigator
import com.cm.geofence.route.RouteGeofence
import com.cm.geofence.route.RouteHome
import com.cm.geofence.ui.HomeRoute
import com.cm.geofence.ui.GeofenceRoute

@Composable
internal fun MainNavHost(
    navigator: MainNavigator,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceDim)
    ) {
        NavHost(
            navController = navigator.navController,
            startDestination = navigator.startDestination,
        ) {
            composable<RouteHome> {
                HomeRoute(
                    padding = padding,
                )
            }
            composable<RouteGeofence> {
                GeofenceRoute(
                    padding = padding,
                )
            }
        }
    }
}