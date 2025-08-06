package com.cm.uvsc.ui.receive

data class ReceiveData(
    val key: String,
    val value: String,
    val remarks: String,
    val isLatest: Boolean = false,
    val isChecked: Boolean = false
)
