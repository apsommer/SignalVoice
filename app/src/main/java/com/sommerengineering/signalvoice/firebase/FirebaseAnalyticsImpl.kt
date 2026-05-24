package com.sommerengineering.signalvoice.firebase

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import javax.inject.Inject
import javax.inject.Singleton

// events
const val EVENT_APP_OPEN = "app_open"
const val EVENT_ONBOARDING_COMPLETE = "onboarding_complete"
const val EVENT_NOTIFICATION_PERMISSION = "notification_permission"
const val EVENT_LISTENING_STATE_CHANGED = "listening_state_changed"
const val EVENT_STREAM_JOINED = "stream_joined"
const val EVENT_SUBSCRIPTION_SCREEN_VIEWED = "subscription_screen_viewed"
const val EVENT_SUBSCRIPTION_PURCHASED = "subscription_purchased"
const val EVENT_FOREGROUND_SERVICE_STARTED = "foreground_service_started"
const val EVENT_FOREGROUND_SERVICE_STOPPED = "foreground_service_stopped"

// params
const val PARAM_IS_GRANTED = "is_granted"
const val PARAM_IS_LISTENING = "is_listening"
const val PARAM_STREAM = "stream"
const val PARAM_PRODUCT_ID = "product_id"

@Singleton
class FirebaseAnalyticsImpl @Inject constructor() {

    private val analytics = Firebase.analytics

    fun log(event: String) =
        analytics.logEvent(event, null)

    fun logNotificationPermission(
        isGranted: Boolean
    ) {
        analytics.logEvent(
            EVENT_NOTIFICATION_PERMISSION,
            bundleOf(
                PARAM_IS_GRANTED,
                isGranted.toString()
            )
        )
    }

    fun logListeningStateChanged(
        isListening: Boolean
    ) {
        analytics.logEvent(
            EVENT_LISTENING_STATE_CHANGED,
            bundleOf(
                PARAM_IS_LISTENING,
                isListening.toString()
            )
        )
    }

    fun logStreamJoined(
        stream: String
    ) {
        analytics.logEvent(
            EVENT_STREAM_JOINED,
            bundleOf(
                PARAM_STREAM,
                stream
            )
        )
    }

    fun logSubscriptionPurchased(
        productId: String
    ) {
        analytics.logEvent(
            EVENT_SUBSCRIPTION_PURCHASED,
            bundleOf(
                PARAM_PRODUCT_ID,
                productId
            )
        )
    }

    private fun bundleOf(key: String, value: String) =
        Bundle().apply { putString(key, value) }
}

