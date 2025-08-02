package com.cm.uvsc.ble

import java.nio.charset.Charset

sealed interface SendPacket {
    val key: String
    val value: String

    val responseKey :String

    fun toCommandString(): String = "$key:$value"

    fun toByteArray(charset: Charset = Charsets.US_ASCII): ByteArray =
        toCommandString().toByteArray(charset)
}


data class SetUVTime(val nowDate: String) : SendPacket {
    override val key: String = "UVTime"
    override val value: String = nowDate
    override val responseKey: String
        get() = toCommandString()
}

data class SetChargeMode(val isOff: Boolean) : SendPacket {
    override val key: String = "ACS"
    override val value: String = if (isOff) "200" else "100"
    override val responseKey: String
        get() = toCommandString()
}
