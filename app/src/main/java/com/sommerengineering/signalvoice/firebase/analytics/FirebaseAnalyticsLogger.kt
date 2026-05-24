package com.sommerengineering.signalvoice.firebase.analytics

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsLogger @Inject constructor() {

    // events
    private val EVENT_APP_OPEN = "app_open"
    private val EVENT_ONBOARDING_COMPLETE = "onboarding_complete"
    private val EVENT_NOTIFICATION_PERMISSION = "notification_permission"
    private val EVENT_LISTENING_STATE_CHANGED = "listening_state_changed"
    private val EVENT_STREAM_JOINED = "stream_joined"
    private val EVENT_SUBSCRIPTION_SCREEN_VIEWED = "subscription_screen_viewed"
    private val EVENT_SUBSCRIPTION_PURCHASED = "subscription_purchased"
    private val EVENT_FOREGROUND_SERVICE_STARTED = "foreground_service_started"
    private val EVENT_FOREGROUND_SERVICE_STOPPED = "foreground_service_stopped"

    // params
    private val PARAM_IS_GRANTED = "is_granted"
    private val PARAM_IS_LISTENING = "is_listening"
    private val PARAM_STREAM = "stream"
    private val PARAM_PRODUCT_ID = "product_id"

    private val analytics =
        Firebase.analytics.apply {
            setAnalyticsCollectionEnabled(true)
        }

    fun log(event: AnalyticsEvent) {

        when (event) {

            AnalyticsEvent.AppOpen -> {
                analytics.logEvent(
                    EVENT_APP_OPEN,
                    null
                )
            }

            AnalyticsEvent.OnboardingComplete -> {
                analytics.logEvent(
                    EVENT_ONBOARDING_COMPLETE,
                    null
                )
            }

            is AnalyticsEvent.NotificationPermission -> {
                analytics.logEvent(
                    EVENT_NOTIFICATION_PERMISSION,
                    bundleOf(
                        PARAM_IS_GRANTED,
                        event.isGranted.toString()
                    )
                )
            }

            is AnalyticsEvent.ListeningStateChanged -> {
                analytics.logEvent(
                    EVENT_LISTENING_STATE_CHANGED,
                    bundleOf(
                        PARAM_IS_LISTENING,
                        event.isListening.toString()
                    )
                )
            }

            is AnalyticsEvent.StreamJoined -> {
                analytics.logEvent(
                    EVENT_STREAM_JOINED,
                    bundleOf(
                        PARAM_STREAM,
                        event.stream
                    )
                )
            }

            AnalyticsEvent.SubscriptionScreenViewed -> {
                analytics.logEvent(
                    EVENT_SUBSCRIPTION_SCREEN_VIEWED,
                    null
                )
            }

            is AnalyticsEvent.SubscriptionPurchased -> {
                analytics.logEvent(
                    EVENT_SUBSCRIPTION_PURCHASED,
                    bundleOf(
                        PARAM_PRODUCT_ID,
                        event.productId
                    )
                )
            }

            AnalyticsEvent.ForegroundServiceStarted -> {
                analytics.logEvent(
                    EVENT_FOREGROUND_SERVICE_STARTED,
                    null
                )
            }

            AnalyticsEvent.ForegroundServiceStopped -> {
                analytics.logEvent(
                    EVENT_FOREGROUND_SERVICE_STOPPED,
                    null
                )
            }
        }
    }
}

private fun bundleOf(
    key: String,
    value: String
): Bundle {
    return Bundle().apply {
        putString(
            key,
            value
        )
    }
}
