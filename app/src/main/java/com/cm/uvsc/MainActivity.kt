package com.cm.uvsc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.cm.uvsc.route.LaunchedRouter
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import com.cm.uvsc.ui.MainScreen
import com.cm.uvsc.ui.theme.UVSCTheme

import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

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
                            is RouteUvscHistory -> viewModel.navigatUvscHistory()
                            is RouteReceiveHistory -> viewModel.navigateRouteReceiveHistory()
                        }
                    },
                )
            }
        }
    }
}
