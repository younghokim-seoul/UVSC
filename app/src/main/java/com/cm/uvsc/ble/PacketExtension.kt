package com.cm.uvsc.ble

fun ByteArray.isPureAsciiText(): Boolean {
    return all { byte ->
        val charCode = byte.toInt()
        (charCode in 32..126) || charCode == 10 || charCode == 13
    }
}