package com.sommerengineering.signalvoice.firebase.analytics

interface AnalyticsLogger {
    fun log(event: AnalyticsEvent)
}