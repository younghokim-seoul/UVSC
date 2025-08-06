package com.cm.uvsc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cm.uvsc.ui.theme.USCVColor
import com.polidea.rxandroidble3.RxBleDevice

@Composable
fun DeviceScanDialog(
    searchQuery: String,
    devices: List<RxBleDevice>,
    onSearchQueryChanged: (String) -> Unit,
    onConnectClick: (RxBleDevice) -> Unit
) {
    Dialog(
        onDismissRequest = { /* Do nothing to prevent dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = USCVColor.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Bluetooth 기기 검색",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = USCVColor.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("기기 이름으로 검색") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = USCVColor.PaleGray,
                        unfocusedContainerColor = USCVColor.PaleGray,
                        disabledContainerColor = USCVColor.PaleGray,
                        focusedIndicatorColor = USCVColor.Blue02,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (devices.isEmpty()) {
                    NoDeviceView()
                } else {
                    DeviceListView(devices = devices, onConnectClick = onConnectClick)
                }
            }
        }
    }
}

@Composable
fun NoDeviceView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "검색된 기기가 없습니다",
            fontSize = 16.sp,
            color = USCVColor.Gray
        )
    }
}

@Composable
fun DeviceItem(device: RxBleDevice, onConnectClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.name ?: "알 수 없는 기기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = USCVColor.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device.bluetoothDevice.address,
                fontSize = 14.sp,
                color = USCVColor.Gray
            )
        }
        Button(onClick = onConnectClick) {
            Text("연결")
        }
    }
}

@Composable
fun ColumnScope.DeviceListView(devices: List<RxBleDevice>, onConnectClick: (RxBleDevice) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .drawWithContent {
                drawContent()

                val fadeHeight = 30.dp.toPx()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(USCVColor.White, Color.Transparent),
                        endY = fadeHeight
                    ),
                    size = this.size.copy(height = fadeHeight)
                )

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, USCVColor.White),
                        startY = this.size.height - fadeHeight,
                        endY = this.size.height
                    ),
                    topLeft = Offset(
                        x = 0f,
                        y = this.size.height - fadeHeight
                    ),
                    size = this.size.copy(height = fadeHeight)
                )
            },
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        itemsIndexed(
            items = devices,
            key = { _, device -> device.bluetoothDevice.address }
        ) { index, device ->
            DeviceItem(device = device, onConnectClick = { onConnectClick(device) })
            if (index < devices.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}
