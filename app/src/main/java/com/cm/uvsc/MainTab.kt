package com.cm.uvsc

import androidx.compose.runtime.Composable
import com.cm.uvsc.route.Route
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory

internal enum class MainTab(
    internal val contentDescription: String,
    val route: Route,
) {
    HOME(
        contentDescription = "메인",
        route = RouteHome,
    ),
    UVSC_HISTORY(
        contentDescription = "UVSC 이력",
        route = RouteUvscHistory,
    ),
    RECEIVE_HISTORY(
        contentDescription = "수신데이터",
        route = RouteReceiveHistory,
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
