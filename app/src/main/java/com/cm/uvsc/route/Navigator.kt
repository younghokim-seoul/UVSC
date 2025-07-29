package com.cm.uvsc.route

import kotlinx.coroutines.channels.Channel


interface Navigator {

    val channel: Channel<InternalRoute>

    suspend fun navigate(route: Route, saveState: Boolean = false, launchSingleTop: Boolean = false)


    suspend fun navigateBack()
}
