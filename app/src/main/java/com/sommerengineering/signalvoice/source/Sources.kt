package com.sommerengineering.signalvoice.source

import androidx.compose.ui.graphics.Color
import com.sommerengineering.signalvoice.R
import com.sommerengineering.signalvoice.message.MessageItemStyle

val tradingViewSource = Source(
    key = "tradingview",
    displayName = "TradingView",
    order = 0,
    style = MessageItemStyle(
        primary = Color(0xFF5B8CFF),
        accent = Color(0xFFA9C4FF),
        surface = Color(0xFF111827),
        text = Color(0xFFF3F7FF),
        iconRes = R.drawable.tradingview
    )
)

val trendSpiderSource = Source(
    key = "trendspider",
    displayName = "TrendSpider",
    order = 1,
    style = MessageItemStyle(
        primary = Color(0xFF7FD6AE),
        accent = Color(0xFFC7F3DE),
        surface = Color(0xFF10241C),
        text = Color(0xFFF1FFF8),
        iconRes = R.drawable.trendspider
    )
)

val insomniaSource = Source(
    key = "insomnia",
    displayName = "Insomnia",
    order = 2,
    style = MessageItemStyle(
        primary = Color(0xFFA07CFF),
        accent = Color(0xFFD6C8FF),
        surface = Color(0xFF1B1630),
        text = Color(0xFFF7F3FF),
        iconRes = R.drawable.insomnia
    )
)

val unknownSource = Source(
    key = "unknown",
    displayName = "External",
    order = 3,
    style = MessageItemStyle(
        primary = Color(0xFFB05A6A),
        accent = Color(0xFFE7A7B3),
        surface = Color(0xFF241318),
        text = Color(0xFFFFF1F4),
        iconRes = R.drawable.unknown
    )
)

val allSignalSources = listOf(tradingViewSource, trendSpiderSource, insomniaSource, unknownSource)
private val signalSourceMap = allSignalSources.associateBy { it.key }
fun resolveSignalSource(key: String) =
    signalSourceMap[key] ?: unknownSource