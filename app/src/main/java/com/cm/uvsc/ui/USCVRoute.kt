package com.cm.uvsc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cm.uvsc.ui.theme.USCVColor

@Composable
fun USCVRoute(
    padding: PaddingValues,
) {
    Box(modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .background(USCVColor.LightBlack)) {

    }
}