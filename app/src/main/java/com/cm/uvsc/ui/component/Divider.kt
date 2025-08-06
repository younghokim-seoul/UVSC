package com.cm.uvsc.ui.component

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VDivider() {
    VerticalDivider(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp),
        color = Color.White
    )
}
