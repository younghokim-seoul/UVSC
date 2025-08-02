package com.cm.uvsc.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
    return Clock.System.now().toLocalDateTime(timeZone)
}


fun LocalDateTime.asDateString(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val javaLocalDateTime = this.toJavaLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern(format, Locale.getDefault())
    return javaLocalDateTime.format(formatter)
}
