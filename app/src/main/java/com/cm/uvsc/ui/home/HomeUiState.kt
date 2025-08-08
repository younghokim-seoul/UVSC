package com.cm.uvsc.ui.home

import com.cm.uvsc.R
import com.cm.uvsc.ui.home.HomeUiState.Charging
import com.cm.uvsc.ui.home.HomeUiState.UvscInProgress

sealed class HomeUiState {
    open val statusText: String = ""
    open val batteryResId: Int = R.drawable.battery_blue
    open val controlBtnText: String = ""

    data class Charging(
        override val recentUvscTime: String,
        override val uvscTime: String,
        override val uvscResult: String,
        override val expectedTime: String
    ) : HomeUiState(), UvscInfo {
        override val statusText = "충전 중(UVSC 대기)"
        override val controlBtnText = "UVSC 시작"
    }

    data class UvscInProgress(
        val progressTime: Int,
        override val recentUvscTime: String,
        override val uvscTime: String,
        override val uvscResult: String,
        override val expectedTime: String
    ) : HomeUiState(), UvscInfo {
        override val statusText = "UVSC 중(${progressTime}분)"
        override val batteryResId = R.drawable.battery_red
        override val controlBtnText = "UVSC 멈춤"
    }
}

val emptyCharging = Charging(
    recentUvscTime = "-",
    uvscTime = "-",
    uvscResult = "-",
    expectedTime = "-"
)

fun UvscInfo?.toUvscInProgress(progressTime: Int) = UvscInProgress(
    progressTime = progressTime,
    recentUvscTime = this?.recentUvscTime ?: "-",
    uvscTime = this?.uvscTime ?: "-",
    uvscResult = this?.uvscResult ?: "-",
    expectedTime = this?.expectedTime ?: "-"
)

fun UvscInfo?.toUvscInProgress(
    progressTime: Int = 0,
    uvscTime: String = "-",
    uvscResult: String = "-",
    expectedTime: String = "-"
) = UvscInProgress(
    progressTime = progressTime,
    recentUvscTime = this?.recentUvscTime ?: "",
    uvscTime = uvscTime,
    uvscResult = uvscResult,
    expectedTime = expectedTime
)

fun UvscInfo?.toCharging() = Charging(
    recentUvscTime = this?.recentUvscTime ?: "-",
    uvscTime = this?.uvscTime ?: "-",
    uvscResult = this?.uvscResult ?: "-",
    expectedTime = this?.expectedTime ?: "-"
)