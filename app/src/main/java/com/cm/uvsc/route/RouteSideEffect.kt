package com.cm.uvsc.route


internal sealed interface RouteSideEffect {

    data class Navigate(
        val route: Route,
        val saveState: Boolean,
        val launchSingleTop: Boolean,
    ) : RouteSideEffect

    data object NavigateBack : RouteSideEffect
}
