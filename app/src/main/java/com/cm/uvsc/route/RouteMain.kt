package com.cm.uvsc.route

import kotlinx.serialization.Serializable

//메인
@Serializable
data object RouteHome : Route

//UVSC 이력 탭
@Serializable
data object RouteUvscHistory : Route

//수신 이력 탭
@Serializable
data object RouteReceiveHistory : Route