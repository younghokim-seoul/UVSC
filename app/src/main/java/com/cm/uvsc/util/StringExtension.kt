package com.cm.uvsc.util

fun String.splitTrimmed(delimiter: String = ","): List<String> =
    this.split(delimiter).map { it.trim() }

fun String?.normalizeDate(): String? {
    if (this.isNullOrEmpty()) return null
    return Regex("""\d{4}-\d{2}-\d{2}""").find(this)?.value
}