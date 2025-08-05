package com.cm.geofence.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cm.geofence.MainNavigator
import com.cm.geofence.MainTab
import com.cm.geofence.rememberMainNavigator
import com.cm.geofence.ui.component.MainNavHost
import com.cm.geofence.ui.component.MainTopBar
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun MainScreen(
    onTabSelected: (MainTab) -> Unit,
    navigator: MainNavigator = rememberMainNavigator(),
) {
    val snackBarHostState = remember { SnackbarHostState() }

    MainScreenContent(
        onTabSelected = onTabSelected,
        navigator = navigator,
        snackBarHostState = snackBarHostState
    )
}

@Composable
private fun MainScreenContent(
    navigator: MainNavigator,
    onTabSelected: (MainTab) -> Unit,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.navigationBarsPadding(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            MainTopBar(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 28.dp),
                visible = navigator.shouldShowBottomBar(),
                tabs = MainTab.entries.toPersistentList(),
                currentTab = navigator.currentTab,
                onTabSelected = onTabSelected,
            )
        },
        content = { padding ->
            MainNavHost(
                navigator = navigator,
                padding = padding,
            )
        })
}