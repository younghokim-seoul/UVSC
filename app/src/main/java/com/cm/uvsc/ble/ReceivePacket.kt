package com.cm.uvsc.ble

sealed interface ReceivePacket {
    val key: String
    val valueAsString: String
}

/**
 * "ACS:value" 형태의 패킷
 */
data class AcsPacket(val value: String) : ReceivePacket {
    override val key: String = "ACS"
    override val valueAsString: String = value
}

/**
 * "ACHT:value" 형태의 패킷
 */
data class AchtPacket(val value: String) : ReceivePacket {
    override val key: String = "ACHT"
    override val valueAsString: String = value

}

/**
 * "ACHS:25,100,70" 형태의 패킷
 */
data class AchsPacket(val value: String) : ReceivePacket {
    override val key: String = "ACHS"
    override val valueAsString: String = value
}

/**
 * "UVTime:value" 형태의 패킷
 */
data class UVTimePacket(val value: String) : ReceivePacket {
    override val key: String = "UVTime"
    override val valueAsString: String = value
}

/**
 * 이력 데이터 패킷 (예: "ACH:2,1970-01-01,13349")
 */
data class AchPacket(
    val value: String
) : ReceivePacket {
    override val key: String = "ACH"
    override val valueAsString: String = value
}

/**
 * 위에서 정의되지 않은 모든 종류의 패킷 (수신 데이터 탭용)
 */
data class RawPacket(
    override val key: String, override val valueAsString: String
) : ReceivePacket


