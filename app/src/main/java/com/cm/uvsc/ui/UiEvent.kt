package com.cm.uvsc.ui

sealed class UiEvent {
    data class ModeChanged(val isSuccess: Boolean) : UiEvent()
    data class ConnectResult(val isSuccess: Boolean) : UiEvent()
}