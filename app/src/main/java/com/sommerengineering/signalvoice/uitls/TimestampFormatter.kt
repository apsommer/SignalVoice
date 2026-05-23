package com.sommerengineering.signalvoice.uitls

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimestampFormatter {

    private const val minute = 60_000L
    private const val hour = 60 * minute

    private fun weekdayFormat() = // Mon, Tue, Wed
        DateTimeFormatter.ofPattern("EEE", Locale.getDefault())

    private fun fullFormat() = // 12:00:00 • January 1, 2024
        DateTimeFormatter.ofPattern("HH:mm:ss • MMMM d, yyyy", Locale.getDefault())

    private fun zonedInstant(time: Long) = // capture instant in system timezone
        Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault())

    fun beautifyCompact(timestamp: Long): String {

        val now = System.currentTimeMillis()
        val diff = (now - timestamp).coerceAtLeast(0) // rare server vs client clock skew

        val hours = diff / hour
        val minutes = (diff / minute) % 60

        return when {
            minute > diff -> "just now"
            hours < 12 ->
                when {
                    hours == 0L -> "${minutes}m"
                    minutes > 0 -> "${hours}h ${minutes}m"
                    else -> "${hours}h"
                }

            else -> weekdayFormat().format(zonedInstant(timestamp))
        }
    }

    fun beautifyFull(timestamp: Long) =
        fullFormat().format(zonedInstant(timestamp))
}