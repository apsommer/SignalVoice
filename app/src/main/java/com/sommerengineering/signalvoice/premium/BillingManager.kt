package com.sommerengineering.signalvoice.premium

import com.sommerengineering.signalvoice.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor() {

    suspend fun connect() {

    }

    suspend fun isPremium(): Boolean {
        return true
    }

    suspend fun launchPurchaseFlow(
        activity: MainActivity
    ) {

    }
}