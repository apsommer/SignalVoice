package com.sommerengineering.signalvoice.source

import androidx.compose.ui.graphics.Color
import com.sommerengineering.signalvoice.R
import com.sommerengineering.signalvoice.message.MessageItemStyle
import com.sommerengineering.signalvoice.uitls.btcStream
import com.sommerengineering.signalvoice.uitls.clStream
import com.sommerengineering.signalvoice.uitls.e6Stream
import com.sommerengineering.signalvoice.uitls.esStream
import com.sommerengineering.signalvoice.uitls.gcStream
import com.sommerengineering.signalvoice.uitls.nqStream
import com.sommerengineering.signalvoice.uitls.znStream

val znAsset = Asset(
    origin = znStream,
    symbol = "ZN",
    displayName = "10Y Treasury",
    spokenName = "ten year treasury",
    category = "Rates",
    exchange = "CBOT",
    assetDescription = "CBOT · Rates · ZN",
    signalDescription = "Macro rate shifts",
    order = 0,
    style = MessageItemStyle(
        primary = Color(0xFF2FA38A),
        accent = Color(0xFF6FC9B5),
        surface = Color(0xFF0E2A26),
        text = Color(0xFFE6FFFA),
        iconRes = null,
        iconText = "10Y"
    ),
    isPremium = false
)

val nqAsset = Asset(
    origin = nqStream,
    symbol = "NQ",
    displayName = "Nasdaq 100",
    spokenName = "Nasdaq one hundred",
    category = "Equity Index",
    exchange = "CME",
    assetDescription = "CME · Index · NQ",
    signalDescription = "High velocity momentum",
    order = 1,
    style = MessageItemStyle(
        primary = Color(0xFF9A84FF),
        accent = Color(0xFFC2B5FF),
        surface = Color(0xFF15122A),
        text = Color(0xFFEAE6FF),
        iconRes = null,
        iconText = "100"
    ),
    isPremium = false
)

val btcAsset = Asset(
    origin = btcStream,
    symbol = "BTC",
    displayName = "Bitcoin",
    spokenName = "Bitcoin",
    category = "Cryptocurrency",
    exchange = "Binance",
    assetDescription = "Binance · Crypto · BTC",
    signalDescription = "Volatility breakouts",
    order = 2,
    style = MessageItemStyle(
        primary = Color(0xFFF7931A),
        accent = Color(0xFFFFB347),
        surface = Color(0xFF2B1700),
        text = Color(0xFFFFF4E6),
        iconRes = R.drawable.btc
    ),
    isPremium = false
)

// premium /////////////////////////////////////////////////////////////////////////////////////////

val esAsset = Asset(
    origin = esStream,
    symbol = "ES",
    displayName = "S&P 500",
    spokenName = "S and P five hundred",
    category = "Equity Index",
    exchange = "CME",
    assetDescription = "CME · Index · ES",
    signalDescription = "Balanced trend structure",
    order = 3,
    style = MessageItemStyle(
        primary = Color(0xFF69A6FF),
        accent = Color(0xFF9CC4FF),
        surface = Color(0xFF0D1A2B),
        text = Color(0xFFE6F0FF),
        iconRes = null,
        iconText = "500"
    ),
    isPremium = true
)

val gcAsset = Asset(
    origin = gcStream,
    symbol = "GC",
    displayName = "Gold",
    spokenName = "Gold",
    category = "Metals",
    exchange = "COMEX",
    assetDescription = "COMEX · Metals · GC",
    signalDescription = "Macro impulse swings",
    order = 4,
    style = MessageItemStyle(
        primary = Color(0xFFE6C96A),
        accent = Color(0xFFF4DE9A),
        surface = Color(0xFF332900),
        text = Color(0xFFFFF9E6),
        iconRes = R.drawable.gc
    ),
    isPremium = true
)

val e6Asset = Asset(
    origin = e6Stream,
    symbol = "E6",
    displayName = "Euro",
    spokenName = "Euro",
    category = "Currencies",
    exchange = "CME",
    assetDescription = "CME · Currency · E6",
    signalDescription = "Macro currency repricing",
    order = 5,
    style = MessageItemStyle(
        primary = Color(0xFF7EC7D8),
        accent = Color(0xFFDFF7FC),
        surface = Color(0xFF1A3036),
        text = Color(0xFFF1FCFF),
        iconRes = R.drawable.euro
    ),
    isPremium = true
)

val clAsset = Asset(
    origin = clStream,
    symbol = "CL",
    displayName = "Oil",
    spokenName = "Oil",
    category = "Energy",
    exchange = "NYMEX",
    assetDescription = "NYMEX · Energy · CL",
    signalDescription = "Violent inventory repricing",
    order = 6,
    style = MessageItemStyle(
        primary = Color(0xFFD2E4F2),
        accent = Color(0xFFF4FAFF),
        surface = Color(0xFF141A20),
        text = Color(0xFFF2F8FD),
        iconRes = R.drawable.oil
    ),
    isPremium = true
)

val allAssets = listOf(znAsset, nqAsset, btcAsset, esAsset, gcAsset, e6Asset, clAsset)
val assetOrigins = allAssets.associateBy { it.origin }
val assetDisplayNames = allAssets.map { it.displayName }.toSet()
fun resolveAsset(stream: String) =
    assetOrigins[stream] ?: error("Unknown asset for stream: $stream")
