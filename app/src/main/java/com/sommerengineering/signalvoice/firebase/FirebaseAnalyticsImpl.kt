package com.sommerengineering.signalvoice.firebase

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsImpl @Inject constructor() {

    // events
    private val EVENT_APP_OPEN = "app_open"
    private val EVENT_ONBOARDING_COMPLETE = "onboarding_complete"
    private val EVENT_NOTIFICATIONS_CHANGED = "notifications_changed"
    private val EVENT_LISTENING_CHANGED = "listening_changed"
    private val EVENT_STREAM_CHANGED = "stream_changed"
    private val EVENT_SUBSCRIPTION_VIEWED = "subscription_viewed"
    private val EVENT_SUBSCRIPTION_PURCHASED = "subscription_purchased"

    // params
    private val PARAM_ENABLED = "is_enabled"
    private val PARAM_STREAM = "stream"
    private val PARAM_PRODUCT_ID = "product_id"

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
            putString(PARAM_ENABLED, enabled.toString())
        }
    )

    fun logListeningChanged(
        enabled: Boolean
    ) = log(
        EVENT_LISTENING_CHANGED,
        Bundle().apply {
            putString(PARAM_ENABLED, enabled.toString())
        }
    )

    fun logStreamChanged(
        stream: String,
        enabled: Boolean
    ) = log(
        EVENT_STREAM_CHANGED,
        Bundle().apply {
            putString(PARAM_STREAM, stream)
            putString(PARAM_ENABLED, enabled.toString())
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
        product: String
    ) = log(
        EVENT_SUBSCRIPTION_PURCHASED,
        Bundle().apply {
            putString(PARAM_PRODUCT_ID, product)
        }
    )
}

