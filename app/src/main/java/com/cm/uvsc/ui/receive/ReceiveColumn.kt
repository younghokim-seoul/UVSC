package com.cm.uvsc.ui.receive

import androidx.compose.runtime.Immutable

@Immutable
sealed class ReceiveColumn() {
    data object Checkbox : ReceiveColumn()

    abstract class TextColumn(
        val title: String,
        val getValue: (ReceiveData) -> String
    ) : ReceiveColumn()

    data object Key : TextColumn("Key", { it.key })
    data object Value : TextColumn("Value", { it.value })
    data object Remarks : TextColumn("Remarks", { it.remarks })

    companion object {
        val ordered = listOf(Checkbox, Key, Value, Remarks)
    }
}