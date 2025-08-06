package com.cm.uvsc.ui.receive

import com.cm.uvsc.ble.ReceivePacket

data class ReceiveScanResult(
    val currentMap: Map<String, ReceivePacket>,
    val changedPacket: ReceivePacket?
)