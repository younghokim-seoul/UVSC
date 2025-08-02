package com.cm.uvsc.ble

object PacketParser {

    /**
     * 장치를 통해 들어오는 문자열 데이터
     * 예: Key:Value
     */
    fun parse(rawData: String): BasePacket {
        val parts = rawData.split(":", limit = 2)
        val key = parts.getOrNull(0) ?: ""
        val value = parts.getOrNull(1) ?: ""

        return when (key) {
            "ACS" -> AcsPacket(value.trim())
            "ACHT" -> AchtPacket(value.trim())
            "ACHS" -> AchsPacket(value.trim())
            "ACH" -> parseAchPacket(value.trim())
            else -> RawPacket(key, value) // 정의되지않는 그외 패킷
        }
    }

    /**
     * "ACH" 이력 데이터의 value 부분을 파싱
     * 예: "2,1970-01-01,13349"
     */
    private fun parseAchPacket(value: String): BasePacket {
        return try {
            val parts = value.split(",")
            AchPacket(
                index = parts[0].toInt(),
                date = parts[1],
                time = parts[2]
            )
        } catch (e: Exception) {
            RawPacket("ACH", value)
        }
    }

}