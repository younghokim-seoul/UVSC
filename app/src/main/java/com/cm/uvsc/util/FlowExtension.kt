package com.cm.uvsc.util

import com.cm.uvsc.ble.AchPacket
import com.cm.uvsc.ble.AchsPacket
import com.cm.uvsc.ble.AchtPacket
import com.cm.uvsc.ble.AcsPacket
import com.cm.uvsc.ble.ReceivePacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.mapNotNull

inline fun <reified T : ReceivePacket> Flow<Map<String, ReceivePacket>>.packetOf(key: String): Flow<T> =
    mapNotNull { it[key] as? T }.distinctUntilChangedBy { it.valueAsString }

fun Flow<Map<String, ReceivePacket>>.acsPackets() = packetOf<AcsPacket>("ACS")

fun Flow<Map<String, ReceivePacket>>.achtPackets() = packetOf<AchtPacket>("ACHT")

fun Flow<Map<String, ReceivePacket>>.achsPackets() = packetOf<AchsPacket>("ACHS")

fun Flow<Map<String, ReceivePacket>>.achPackets() = packetOf<AchPacket>("ACH")