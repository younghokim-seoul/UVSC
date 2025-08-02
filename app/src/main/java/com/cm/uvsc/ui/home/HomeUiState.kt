package com.cm.uvsc.ui.home

import com.cm.uvsc.R

sealed class HomeUiState {
    open val statusText: String = ""
    open val batteryResId: Int = R.drawable.battery_blue
    open val controlBtnText: String = ""

    data object NoData : HomeUiState()
    data class Charging(
        override val recentUvscTime: String,
        override val uvscTime: String,
        override val uvscResult: String,
        override val expectedTime: String,
    ) : HomeUiState(), UvscInfo {
        override val statusText = "충전 중(UVSC 대기)"
        override val controlBtnText = "UVSC 시작"
    }

    data class UvscInProgress(
        val progressTime: Int,
        override val recentUvscTime: String,
        override val uvscTime: String,
        override val uvscResult: String,
        override val expectedTime: String,
    ) : HomeUiState(), UvscInfo {
        override val statusText = "UVSC 중(${progressTime}분)"
        override val batteryResId = R.drawable.battery_red
        override val controlBtnText = "UVSC 멈춤"
    }
}