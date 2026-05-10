package com.sommerengineering.signalvoice.message

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.sommerengineering.signalvoice.source.MessageOrigin

data class MessageItemStyle(
    val primary: Color,
    val accent: Color,
    val surface: Color,
    val text: Color,
    val iconRes: Int?,
    val iconText: String? = null
)

@Composable // todo doesn't need to be composable, raw hex palletes
fun resolveMessageStyle(
    origin: MessageOrigin
) = when (origin) {

    is MessageOrigin.BroadcastStream -> origin.asset.style
    is MessageOrigin.UserSignal -> origin.source.style
}
