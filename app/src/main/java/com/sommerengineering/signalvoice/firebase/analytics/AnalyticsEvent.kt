package com.sommerengineering.signalvoice.firebase.analytics

sealed interface AnalyticsEvent {

    data object AppOpen : AnalyticsEvent

    data object OnboardingComplete : AnalyticsEvent

    data class NotificationPermission(
        val isGranted: Boolean
    ) : AnalyticsEvent

    data class ListeningStateChanged(
        val isListening: Boolean
    ) : AnalyticsEvent

    data class StreamJoined(
        val stream: String
    ) : AnalyticsEvent

    data object SubscriptionScreenViewed : AnalyticsEvent

    data class SubscriptionPurchased(
        val productId: String
    ) : AnalyticsEvent

    data object ForegroundServiceStarted : AnalyticsEvent

    data object ForegroundServiceStopped : AnalyticsEvent
}