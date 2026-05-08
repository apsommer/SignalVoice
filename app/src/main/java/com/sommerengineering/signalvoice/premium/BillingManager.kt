package com.sommerengineering.signalvoice.premium

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.sommerengineering.signalvoice.MainActivity
import com.sommerengineering.signalvoice.uitls.logMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingClientStateListener {

    private val client =
        BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener { result, purchases -> }
            .build()

    override fun onBillingServiceDisconnected() {
        logMessage("Billing service disconnected")
    }

    override fun onBillingSetupFinished(result: BillingResult) {
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            logMessage("Billing service connection failed with code: ${result.responseCode}")
        }
    }

    suspend fun connect() {
        if (client.isReady) return
        client.startConnection(this)
    }

    suspend fun isPremium(): Boolean {
        return true
    }

    suspend fun launchPurchaseFlow(
        activity: MainActivity
    ) {

    }
}