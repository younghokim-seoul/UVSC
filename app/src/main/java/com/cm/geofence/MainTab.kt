package com.cm.geofence

import androidx.compose.runtime.Composable
import com.cm.geofence.route.Route
import com.cm.geofence.route.RouteGeofence
import com.cm.geofence.route.RouteHome

internal enum class MainTab(
    internal val contentDescription: String,
    val route: Route,
) {
    HOME(
        contentDescription = "Map",
        route = RouteHome,
    ),
    GEO_FENCE(
        contentDescription = "GeoFence",
        route = RouteGeofence,
    );

    companion object {
        @Composable
        fun find(predicate: @Composable (Route) -> Boolean): MainTab? {
            return entries.find { predicate(it.route) }
        }

        @Composable
        fun contains(predicate: @Composable (Route) -> Boolean): Boolean {
            return entries.map { it.route }.any { predicate(it) }
        }
    }
}
