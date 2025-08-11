package com.cm.uvsc.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cm.uvsc.ui.component.TableCell
import com.cm.uvsc.ui.component.VDivider
import com.cm.uvsc.ui.theme.USCVColor

@Composable
fun USCVRoute(
    padding: PaddingValues,
    historyList: List<UvscHistory>
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(USCVColor.White)
    ) {
        HistoryTableHeader()
        LazyColumn {
            itemsIndexed(
                items = historyList,
                key = { _, item -> item.date ?: "-" }
            ) { index, item ->
                HistoryTableRow(item, index % 2 != 0)
            }
        }
    }
}

@Composable
fun HistoryTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(USCVColor.Blue01)
            .height(IntrinsicSize.Min)
            .border(1.dp, Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UvscHistoryColumn.ordered.forEachIndexed { index, column ->
            TableCell(text = column.title, color = Color.White, fontWeight = FontWeight.Bold)
            if (index < UvscHistoryColumn.ordered.lastIndex) VDivider()
        }
    }
}

@Composable
fun HistoryTableRow(item: UvscHistory, isOdd: Boolean) {
    val backgroundColor = if (isOdd) USCVColor.Blue02A30 else USCVColor.PaleGray
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(1.dp, Color.White)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UvscHistoryColumn.ordered.forEachIndexed { index, column ->
            TableCell(text = column.getValue(item))
            if (index < UvscHistoryColumn.ordered.lastIndex) VDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUSCVRoute() {
    USCVRoute(
        padding = PaddingValues(0.dp),
        historyList = listOf(
            UvscHistory(
                date = "2025.09.08",
                time = 25480,
                result = "정상"
            ),
            UvscHistory(
                date = "2025.09.07",
                time = 25480,
                result = "비정상"
            ),
            UvscHistory(
                date = "2025.09.06",
                time = 25480,
                result = "정상"
            ),
            UvscHistory(
                date = "2025.09.05",
                time = 25480,
                result = "정상"
            )
        )
    )
}
