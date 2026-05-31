package com.sommerengineering.signalvoice.session

data class TokenState(
    val isConnecting: Boolean = true,
    val error: String? = null,
    val isReady: Boolean = false
)