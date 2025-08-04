package com.cm.uvsc.ui

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
import com.cm.uvsc.ui.history.UvscHistory
import com.cm.uvsc.ui.history.UvscHistoryColumn
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
            itemsIndexed(historyList) { index, item ->
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
            UvscHistory(index = 1, date = "2025.09.08", time = "12:34:56", result = "정상", note = "특이사항 없음"),
            UvscHistory(index = 2, date = "2025.09.07", time = "11:30:00", result = "비정상", note = "배터리 부족"),
            UvscHistory(index = 3, date = "2025.09.06", time = "10:15:30", result = "정상", note = "정상 작동"),
            UvscHistory(index = 4, date = "2025.09.05", time = "09:45:20", result = "정상", note = "정상 작동")
        )
    )
}
