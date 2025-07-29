package com.cm.uvsc.ui

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cm.uvsc.MainNavigator
import com.cm.uvsc.MainTab
import com.cm.uvsc.component.MainNavHost
import com.cm.uvsc.component.MainTopBar
import com.cm.uvsc.rememberMainNavigator
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun MainScreen(
    onTabSelected: (MainTab) -> Unit,
    navigator: MainNavigator = rememberMainNavigator(),
) {
    val snackBarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()
    val localContextResource = LocalContext.current.resources

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
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            MainTopBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(start = 8.dp, end = 8.dp, top = 28.dp),
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