package com.cm.uvsc.util

fun String.splitTrimmed(delimiter: String = ","): List<String> =
    this.split(delimiter).map { it.trim() }