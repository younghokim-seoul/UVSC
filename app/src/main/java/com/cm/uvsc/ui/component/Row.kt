package com.cm.uvsc.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.TableCell(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    fontWeight: FontWeight? = null,
    color: Color = Color.Black
) {
    Text(
        text = text,
        modifier = modifier
            .weight(1f)
            .padding(vertical = 8.dp),
        textAlign = textAlign,
        fontWeight = fontWeight,
        color = color
    )
}