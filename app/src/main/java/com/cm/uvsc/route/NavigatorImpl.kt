package com.cm.uvsc.route

import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

@ActivityRetainedScoped
internal class NavigatorImpl @Inject constructor() : Navigator {

    override val channel by lazy { Channel<InternalRoute>(Channel.BUFFERED) }

    override suspend fun navigate(
        route: Route,
        saveState: Boolean,
        launchSingleTop: Boolean
    ) {
        channel.send(
            InternalRoute.Navigate(
                route = route,
                saveState = saveState,
                launchSingleTop = launchSingleTop,
            )
        )
    }

    override suspend fun navigateBack() {
        channel.send(InternalRoute.NavigateBack)
    }

}