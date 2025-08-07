package com.cm.uvsc.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cm.uvsc.MainNavigator
import com.cm.uvsc.MainViewModel
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import com.cm.uvsc.ui.home.HomeRoute
import com.cm.uvsc.ui.receive.ReceiveRoute
import com.cm.uvsc.ui.history.USCVRoute
import com.cm.uvsc.ui.home.HomeUiState

@Composable
internal fun MainNavHost(
    navigator: MainNavigator,
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
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
                val uiState by viewModel.homeUiState.collectAsState()
                HomeRoute(
                    padding = padding,
                    uiState = uiState,
                    onClickControl = {
                        val isCharging = uiState is HomeUiState.Charging
                        viewModel.toggleCharging(isOff = isCharging)
                    }
                )
            }
            composable<RouteUvscHistory> {
                val historyList by viewModel.uvscHistoryList.collectAsState()
                USCVRoute(
                    padding = padding,
                    historyList = historyList
                )
            }
            composable<RouteReceiveHistory> {
                val dataList by viewModel.receiveDataList.collectAsState()
                ReceiveRoute(
                    padding = padding,
                    dataList = dataList,
                    onCheckedChange = viewModel::toggleReceiveDataChecked,
                    onSendClick = viewModel::onSendPacketClick
                )
            }
        }
    }
}