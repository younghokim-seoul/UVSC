package com.cm.uvsc

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.cm.uvsc.route.LaunchedRouter
import com.cm.uvsc.route.RouteHome
import com.cm.uvsc.route.RouteReceiveHistory
import com.cm.uvsc.route.RouteUvscHistory
import com.cm.uvsc.ui.DeviceScanDialog
import com.cm.uvsc.ui.MainScreen
import com.cm.uvsc.ui.UiEvent
import com.cm.uvsc.ui.theme.UVSCTheme
import com.gun0912.tedpermission.coroutine.TedPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        collectEvent()

        setContent {
            val navigator: MainNavigator = rememberMainNavigator()
            LaunchedRouter(navigator.navController)
            UVSCTheme {
                MainScreen(
                    navigator = navigator,
                    onTabSelected = { tab ->
                        when (tab.route) {
                            is RouteHome -> viewModel.navigateHome()
                            is RouteUvscHistory -> viewModel.navigateUvscHistory()
                            is RouteReceiveHistory -> viewModel.navigateRouteReceiveHistory()
                        }
                    },
                    viewModel = viewModel
                )

                val isConnected by viewModel.isConnected.collectAsState()
                if (!isConnected) {
                    val searchQuery by viewModel.searchQuery.collectAsState()
                    val devices by viewModel.filteredDevices.collectAsState()
                    DeviceScanDialog(
                        searchQuery = searchQuery,
                        devices = devices,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        onConnectClick = viewModel::connectDevice
                    )
                } else {
                    Toast.makeText(this, "장치가 성공적으로 연결되었습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkPermissions() {

        lifecycleScope.launch {

            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }

            val permissionResult = TedPermission.create()
                .setPermissions(*permissions)
                .setDeniedMessage("권한을 거부하셨습니다.\n[설정] > [권한]에서 직접 권한을 허용해주세요.")
                .check() // 코루틴용 check() 함수

            if (permissionResult.isGranted) {
                // 권한 허용된경우 장치 오토 스캔 시작..
                Timber.i("start scan")
                viewModel.startScan()
            } else {
                finish()
            }
        }
    }

    private fun collectEvent() {
        lifecycleScope.launch {
            viewModel.uiEvent.collect {
                when (it) {
                    is UiEvent.ModeChangedResult -> {
                        if (it.isSuccess) {
                            Toast.makeText(this@MainActivity, "모드 변경이 완료되었습니다", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            Toast.makeText(this@MainActivity, "모드 변경에 실패했습니다", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    is UiEvent.SendPacketResult -> {
                        Toast.makeText(
                            this@MainActivity,
                            "패킷 전송 ${if (it.isSuccess) "성공" else "실패"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

}