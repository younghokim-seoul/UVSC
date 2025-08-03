package com.cm.geofence.route

import kotlinx.serialization.Serializable

//메인
@Serializable
data object RouteHome : Route

//지오팬스 등록
@Serializable
data object RouteGeofence : Route

