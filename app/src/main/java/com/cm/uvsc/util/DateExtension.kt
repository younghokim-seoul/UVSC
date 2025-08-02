package com.cm.uvsc.util

import kotlinx.datetime.LocalDateTime

fun LocalDateTime.nowDateString(): String {
    fun Int.pad() = this.toString().padStart(2, '0')
    val month = this.monthNumber.pad()
    val day = this.dayOfMonth.pad()
    val hour = this.hour.pad()
    val minute = this.minute.pad()
    val second = this.second.pad()
    return "${this.year}-$month-$day $hour:$minute:$second"
}
