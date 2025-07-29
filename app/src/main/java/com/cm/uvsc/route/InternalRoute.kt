package com.cm.uvsc.route

sealed interface InternalRoute {
    data class Navigate(
        val route: Route,
        val saveState: Boolean,
        val launchSingleTop: Boolean,
    ) : InternalRoute

    data object NavigateBack : InternalRoute
}