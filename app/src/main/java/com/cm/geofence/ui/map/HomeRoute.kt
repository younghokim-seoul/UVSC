package com.cm.geofence.ui.map

import android.Manifest
import android.os.Build
import android.widget.ProgressBar
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cm.geofence.ui.component.MapTopBar
import com.cm.geofence.utils.checkLocationPermission
import com.cm.geofence.utils.checkNotificationPermission
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.CircleOverlay
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import timber.log.Timber

val permissionsToRequest = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
    }
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
    }
    else -> {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}

@OptIn(ExperimentalNaverMapApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeRoute(
    padding: PaddingValues,
    mapViewModel: MapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    var isLocationPermissionsGranted by remember {
        mutableStateOf(
            activity?.checkLocationPermission() ?: false
        )
    }

    var isNotificationPermissionGranted by remember {
        mutableStateOf(
            activity?.checkNotificationPermission() ?: false
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { activity?.checkLocationPermission() }
            .distinctUntilChanged()
            .collect { isGranted ->
                if (isGranted != null) {
                    isLocationPermissionsGranted = isGranted
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { activity?.checkNotificationPermission() }
            .distinctUntilChanged()
            .collect { isGranted ->
                if (isGranted != null) {
                    isNotificationPermissionGranted = isGranted
                }
            }
    }

    val permissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            permissionsToRequest.forEach { permission ->
                when (permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                        isLocationPermissionsGranted = permissions[permission] == true
                    }

                    Manifest.permission.POST_NOTIFICATIONS -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            isNotificationPermissionGranted = permissions[permission] == true
                        }
                    }
                }

            }
        },
    )



    MapScreen(padding = padding)


}



@ExperimentalNaverMapApi
@ExperimentalFoundationApi
@Composable
internal fun MapScreen(
    padding: PaddingValues
) {
    val cameraPositionState = rememberCameraPositionState()
    var isCameraMoving by remember { mutableStateOf(false) }

    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .collect { 
                isCameraMoving = it
                if (!it) {
                    Timber.d(
                        "## cameraPositionState, ${cameraPositionState.position.target.latitude}, ${cameraPositionState.position.target.longitude}"
                    )
                }
            }
    }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(padding),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MapContent(
            cameraPositionState = cameraPositionState,
            isCameraMoving = isCameraMoving,
        )
    }

}

@OptIn(ExperimentalNaverMapApi::class)
@Composable
internal fun MapContent(
    cameraPositionState: CameraPositionState,
    isCameraMoving: Boolean,
) {
    Box {
        NaverMap(
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                locationTrackingMode = LocationTrackingMode.NoFollow,
                isNightModeEnabled = isSystemInDarkTheme(),
            ),
            uiSettings = MapUiSettings(
                isZoomControlEnabled = true,
                isScaleBarEnabled = false,
                isLogoClickEnabled = false,
                isLocationButtonEnabled = true,
            ),
            locationSource = rememberFusedLocationSource(),

            ) {

            CircleOverlay(
                center = LatLng(37.558948, 127.035811),
                radius = 2000.0,
                color = Color.Blue.copy(alpha = 0.2f),
                outlineColor = Color.Blue,
                outlineWidth = 2.dp,
            )


            CircleOverlay(
                center = LatLng(37.5716, 126.9763),
                radius = 150.0,
                color = Color.Red.copy(alpha = 0.2f),
                outlineColor = Color.Red,
                outlineWidth = 2.dp,
            )

            Marker(
                state = MarkerState(position = LatLng(37.5716, 126.9763)),
                icon = MarkerIcons.BLACK,
                iconTintColor = Color.Red,
            )
        }


        AnimatedVisibility(
            visible = !isCameraMoving,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
        ) {
            MapTopBar()
        }
    }
}