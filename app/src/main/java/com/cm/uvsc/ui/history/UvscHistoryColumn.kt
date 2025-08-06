package com.cm.uvsc.ui.history

import androidx.compose.runtime.Immutable

@Immutable
sealed interface UvscHistoryColumn {
    val title: String
    fun getValue(history: UvscHistory): String

    data object Date : UvscHistoryColumn {
        override val title = "UVSC 일자"
        override fun getValue(history: UvscHistory) = history.date
    }

    data object Time : UvscHistoryColumn {
        override val title = "UVSC 시간"
        override fun getValue(history: UvscHistory) = history.time
    }

    data object Result : UvscHistoryColumn {
        override val title = "UVSC 결과"
        override fun getValue(history: UvscHistory) = history.result
    }

    data object Note : UvscHistoryColumn {
        override val title = "비고"
        override fun getValue(history: UvscHistory) = history.note
    }

    companion object {
        val ordered: List<UvscHistoryColumn> = listOf(Date, Time, Result, Note)
    }
}