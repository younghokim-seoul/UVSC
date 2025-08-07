package com.cm.uvsc.ui

sealed class UiEvent {
    data class ModeChangedResult(val isSuccess: Boolean) : UiEvent()
    data class SendPacketResult(val isSuccess: Boolean) : UiEvent()
}