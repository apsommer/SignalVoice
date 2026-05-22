package com.sommerengineering.signalvoice.message

import androidx.compose.ui.graphics.Color

data class MessageItemStyle(
    val primary: Color,
    val accent: Color,
    val surface: Color,
    val text: Color,
    val iconRes: Int?,
    val iconText: String? = null
)
