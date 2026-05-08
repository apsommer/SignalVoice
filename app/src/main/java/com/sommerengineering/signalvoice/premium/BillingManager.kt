package com.sommerengineering.signalvoice.premium

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.sommerengineering.signalvoice.uitls.logMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val productId = "premium"

    private val client =
        BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener { _, _ -> } // required, not used
            .build()

    suspend fun connect() {

        if (client.isReady) return

        suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() =
                        logMessage("Billing service disconnected")

                    override fun onBillingSetupFinished(result: BillingResult) {
                        val isSuccess = result.responseCode == BillingClient.BillingResponseCode.OK
                        if (!isSuccess) logMessage("Billing connection failed with code: ${result.responseCode}")
                        continuation.resume(Unit)
                    }
                }
            )
        }
    }

    suspend fun isPremium(): Boolean {

        connect()

        return suspendCancellableCoroutine { continuation ->

            // fetch purchases
            client.queryPurchasesAsync(
                QueryPurchasesParams
                    .newBuilder()
                    .setProductType(ProductType.SUBS)
                    .build()
            ) { result, purchases ->

                val isSuccess = result.responseCode == BillingClient.BillingResponseCode.OK

                // handle error
                if (!isSuccess) {
                    logMessage("Failed to query purchases with code: ${result.responseCode}")
                    continuation.resume(false)
                    return@queryPurchasesAsync
                }

                // check active purchases
                val isPremium = purchases.any { purchase ->
                    val isValid = purchase.products.contains(productId)
                    val isPurchased = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    val isAcknowledged = purchase.isAcknowledged
                    return@any isValid && isPurchased && isAcknowledged
                }

                continuation.resume(isPremium)
            }
        }
    }
}