package com.cm.uvsc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cm.uvsc.ui.home.HomeUiState
import com.cm.uvsc.ui.home.UvscInfo
import com.cm.uvsc.ui.theme.USCVColor

@Composable
fun HomeRoute(
    padding: PaddingValues,
    uiState: HomeUiState,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(USCVColor.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is HomeUiState.Charging,
            is HomeUiState.UvscInProgress -> {
                val onClick = if (uiState is HomeUiState.Charging) onStartClick else onStopClick
                HomeContentView(state = uiState, onClick = onClick)
            }

            is HomeUiState.NoData -> {
                InfoPanel(info = null, batteryIcon = uiState.batteryResId)
            }
        }
    }
}

@Composable
fun ColumnScope.HomeContentView(state: HomeUiState, onClick: () -> Unit) {
    StatusCard(statusText = state.statusText)
    Spacer(modifier = Modifier.height(16.dp))
    InfoPanel(info = state as? UvscInfo, batteryIcon = state.batteryResId)
    Spacer(modifier = Modifier.weight(1f))
    ControlButton(buttonText = state.controlBtnText, onClick = onClick)
}

@Composable
fun StatusCard(statusText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = USCVColor.PaleGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                fontSize = 20.sp,
                color = USCVColor.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InfoPanel(info: UvscInfo?, batteryIcon: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = USCVColor.Neon05)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                InfoRow(label = "최근 UVSC 시각", value = info?.recentUvscTime ?: "-")
                InfoRow(label = "UVSC 시간", value = info?.uvscTime ?: "-")
                InfoRow(label = "UVSC 결과", value = info?.uvscResult ?: "-")
                InfoRow(label = "예상 유효점등시간", value = info?.expectedTime ?: "-")
            }
            BatteryIcon(batteryIcon)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontSize = 16.sp, color = USCVColor.Black)
        Text(text = value, fontSize = 16.sp, color = USCVColor.Black)
    }
}

@Composable
fun BatteryIcon(resourceId: Int) {
    Icon(
        painter = painterResource(id = resourceId),
        contentDescription = null,
        modifier = Modifier.size(40.dp, 100.dp),
        tint = Color.Unspecified
    )
}

@Composable
fun ControlButton(buttonText: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = USCVColor.DarkGray),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = buttonText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeRoute() {
    val inProgress = HomeUiState.UvscInProgress(
        progressTime = 5,
        recentUvscTime = "2025.09.08 12:34:56",
        uvscTime = "25분",
        uvscResult = "정상",
        expectedTime = "70분 이상"
    )
    HomeRoute(
        padding = PaddingValues(0.dp),
        uiState = inProgress,
        onStartClick = {},
        onStopClick = {}
    )
}