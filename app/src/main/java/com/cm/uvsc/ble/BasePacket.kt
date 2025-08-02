package com.cm.uvsc.ble

sealed interface BasePacket {
    val key: String
}

/**
 * "ACS:value" 형태의 패킷
 */
data class AcsPacket(val value: String) : BasePacket {
    override val key: String = "ACS"
}
/**
 * "ACHT:value" 형태의 패킷
 */
data class AchtPacket(val value: String) : BasePacket {
    override val key: String = "ACHT"
}

/**
 * "ACHS:value" 형태의 패킷
 */
data class AchsPacket(val value: String) : BasePacket {
    override val key: String = "ACHS"
}

/**
 * 이력 데이터 패킷 (예: "ACH:2,1970-01-01,13349")
 */
data class AchPacket(
    val index: Int,
    val date: String,
    val time: String
) : BasePacket {
    override val key: String = "ACH"
}

/**
 * 위에서 정의되지 않은 모든 종류의 패킷 (수신 데이터 탭용)
 */
data class RawPacket(
    override val key: String,
    val rawData: String
) : BasePacket
