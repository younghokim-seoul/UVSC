package com.cm.uvsc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cm.uvsc.ui.component.TableCell
import com.cm.uvsc.ui.component.VDivider
import com.cm.uvsc.ui.receive.ReceiveColumn
import com.cm.uvsc.ui.receive.ReceiveData
import com.cm.uvsc.ui.theme.USCVColor

@Composable
fun ReceiveRoute(
    padding: PaddingValues,
    dataList: List<ReceiveData>,
    onCheckedChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(USCVColor.White)
    ) {
        ReceiveTableHeader()
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(dataList, key = { _, item -> item.key }) { index, item ->
                ReceiveTableRow(
                    item = item,
                    isOdd = index % 2 != 0,
                    isLatest = index == 0,
                    onCheckedChange = { onCheckedChange(item.key) }
                )
            }
        }
    }
}

@Composable
fun ReceiveTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(USCVColor.Blue01)
            .height(IntrinsicSize.Min)
            .border(1.dp, Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReceiveColumn.ordered.forEachIndexed { index, column ->
            when (column) {
                is ReceiveColumn.Checkbox -> {
                    Spacer(modifier = Modifier.weight(1f))
                }

                is ReceiveColumn.TextColumn -> {
                    TableCell(
                        text = column.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (index < ReceiveColumn.ordered.lastIndex) VDivider()
        }
    }
}

@Composable
fun ReceiveTableRow(
    item: ReceiveData,
    isOdd: Boolean,
    isLatest: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor = if (isOdd) USCVColor.Blue02A30 else USCVColor.PaleGray
    val textColor = if (isLatest) USCVColor.Neon01 else Color.Black

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(1.dp, Color.White)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReceiveColumn.ordered.forEachIndexed { index, column ->
            when (column) {
                is ReceiveColumn.Checkbox -> {
                    Checkbox(
                        modifier = Modifier.weight(1f),
                        checked = item.isChecked,
                        onCheckedChange = onCheckedChange
                    )
                }

                is ReceiveColumn.TextColumn -> {
                    TableCell(text = column.getValue(item), color = textColor)
                }
            }
            if (index < ReceiveColumn.ordered.lastIndex) VDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewReceiveRoute() {
    ReceiveRoute(
        padding = PaddingValues(0.dp),
        listOf(
            ReceiveData(
                key = "A1",
                value = "123.456",
                remarks = "Sample Remarks 1",
                isChecked = false
            ),
            ReceiveData(
                key = "2",
                value = "Sample Value 2",
                remarks = "",
                isChecked = true
            )
        ),
        onCheckedChange = {}
    )
}