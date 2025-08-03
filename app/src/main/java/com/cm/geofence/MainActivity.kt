package com.cm.geofence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.cm.geofence.route.LaunchedRouter
import com.cm.geofence.route.RouteGeofence
import com.cm.geofence.route.RouteHome
import com.cm.geofence.ui.MainScreen
import com.cm.geofence.ui.theme.UVSCTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navigator: MainNavigator = rememberMainNavigator()
            LaunchedRouter(navigator.navController)
            UVSCTheme {
                MainScreen(
                    navigator = navigator,
                    onTabSelected = { tab ->
                        when (tab.route) {
                            is RouteHome -> viewModel.navigateHome()
                            is RouteGeofence -> viewModel.navigateGeofence()
                        }
                    },
                )
            }
        }
    }
}
