package com.cm.geofence.route

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
internal class RouterViewModel @Inject constructor(private val navigator: Navigator) : ViewModel() {

    val sideEffect by lazy(LazyThreadSafetyMode.NONE) {
        navigator.channel.receiveAsFlow()
            .map { router ->
                when (router) {
                    is InternalRoute.Navigate -> RouteSideEffect.Navigate(
                        router.route,
                        router.saveState,
                        router.launchSingleTop,
                    )

                    is InternalRoute.NavigateBack -> RouteSideEffect.NavigateBack
                }
            }
    }
}
