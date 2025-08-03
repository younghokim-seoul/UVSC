package com.cm.uvsc.util

import com.cm.uvsc.ble.ReceivePacket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

private val HOME_KEYS = setOf("ACS", "ACHT", "ACHS")
private val HISTORY_KEY = "ACH"

fun Flow<Map<String, ReceivePacket>>.filterForHome(): Flow<Map<String, ReceivePacket>> =
    filter { it.keys.any(HOME_KEYS::contains) }

fun Flow<Map<String, ReceivePacket>>.filterForHistory(): Flow<Map<String, ReceivePacket>> =
    filter { it.containsKey(HISTORY_KEY) }

fun Flow<Map<String, ReceivePacket>>.filterForOthers(): Flow<Map<String, ReceivePacket>> =
    filter { map ->
        map.keys.any { key -> key !in HOME_KEYS && key != HISTORY_KEY }
    }