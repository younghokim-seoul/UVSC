package com.cm.uvsc.ble

object PacketParser {

    /**
     * 장치를 통해 들어오는 문자열 데이터
     * 예: Key:Value
     */
    fun parse(rawData: String): ReceivePacket {
        val parts = rawData.split(":", limit = 2)
        val key = parts.getOrNull(0) ?: ""
        val value = parts.getOrNull(1) ?: ""

        return when (key) {
            "ACS" -> AcsPacket.fromValue(value.trim())
            "ACHT" -> AchtPacket(value.trim())
            "ACHS" -> AchsPacket(value.trim())
            "UVTime" -> UVTimePacket(value.trim())
            "ACH" -> AchPacket(value.trim())
            else -> RawPacket(key, value) // 정의되지않는 그외 패킷
        }
    }
}