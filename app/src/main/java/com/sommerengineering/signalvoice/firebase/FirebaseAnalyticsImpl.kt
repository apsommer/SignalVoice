package com.sommerengineering.signalvoice.firebase

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import javax.inject.Inject
import javax.inject.Singleton

// events
private const val EVENT_APP_OPEN = "app_open"
private const val EVENT_ONBOARDING_COMPLETE = "onboarding_complete"
private const val EVENT_NOTIFICATIONS_CHANGED = "notifications_changed"
private const val EVENT_LISTENING_CHANGED = "listening_changed"
private const val EVENT_STREAM_CHANGED = "stream_changed"
private const val EVENT_SUBSCRIPTION_VIEWED = "subscription_viewed"
private const val EVENT_SUBSCRIPTION_PURCHASED = "subscription_purchased"

// params
private const val PARAM_ENABLED = "is_enabled"
private const val PARAM_STREAM = "stream"
private const val PARAM_PRODUCT_ID = "product_id"

@Singleton
class FirebaseAnalyticsImpl @Inject constructor() {

    private fun log(
        event: String,
        params: Bundle? = null
    ) =
        Firebase.analytics.logEvent(event, params)

    fun logAppOpen() = log(EVENT_APP_OPEN)

    fun logOnboardingComplete() = log(EVENT_ONBOARDING_COMPLETE)

    fun logNotificationsChanged(
        enabled: Boolean
    ) = log(
        EVENT_NOTIFICATIONS_CHANGED,
        Bundle().apply {
            putBoolean(PARAM_ENABLED, enabled)
        }
    )

    fun logListeningChanged(
        enabled: Boolean
    ) = log(
        EVENT_LISTENING_CHANGED,
        Bundle().apply {
            putBoolean(PARAM_ENABLED, enabled)
        }
    )

    fun logStreamChanged(
        stream: String,
        enabled: Boolean
    ) = log(
        EVENT_STREAM_CHANGED,
        Bundle().apply {
            putString(PARAM_STREAM, stream)
            putBoolean(PARAM_ENABLED, enabled)
        }
    )

    fun logSubscriptionViewed(
        productId: String
    ) = log(
        EVENT_SUBSCRIPTION_VIEWED,
        Bundle().apply {
            putString(PARAM_PRODUCT_ID, productId)
        }
    )

    fun logSubscriptionPurchased(
        productId: String
    ) = log(
        EVENT_SUBSCRIPTION_PURCHASED,
        Bundle().apply {
            putString(PARAM_PRODUCT_ID, productId)
        }
    )
}

