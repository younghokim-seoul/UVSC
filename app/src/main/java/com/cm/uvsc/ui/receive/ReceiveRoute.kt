package com.cm.uvsc.ui.receive

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cm.uvsc.ui.component.TableCell
import com.cm.uvsc.ui.component.VDivider
import com.cm.uvsc.ui.theme.USCVColor

@Composable
fun ReceiveRoute(
    padding: PaddingValues,
    dataList: List<ReceiveData>,
    onCheckedChange: (String) -> Unit,
    onSendClick: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(USCVColor.White)
            .imePadding()
    ) {
        ReceiveTableHeader()
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(dataList, key = { _, item -> item.key }) { index, item ->
                ReceiveTableRow(
                    item = item,
                    isOdd = index % 2 != 0,
                    onCheckedChange = { onCheckedChange(item.key) }
                )
            }
        }
        PacketInputSection(
            inputText = inputText,
            onTextChange = { inputText = it },
            onSendClick = onSendClick
        )
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
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor = if (isOdd) USCVColor.Blue02A30 else USCVColor.PaleGray
    val textColor = if (item.isLatest) USCVColor.Neon01 else Color.Black

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

@Composable
fun PacketInputSection(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSendClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("메세지를 입력하세요") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = USCVColor.PaleGray,
                unfocusedContainerColor = USCVColor.PaleGray,
                disabledContainerColor = USCVColor.PaleGray,
                focusedIndicatorColor = USCVColor.Blue02,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Button(
            onClick = { onSendClick(inputText) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Send", fontSize = 16.sp, color = USCVColor.White)
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
        onCheckedChange = {},
        onSendClick = {}
    )
}