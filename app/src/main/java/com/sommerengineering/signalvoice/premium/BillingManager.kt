package com.sommerengineering.signalvoice.premium

import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.sommerengineering.signalvoice.MainActivity
import com.sommerengineering.signalvoice.uitls.logMessage
import com.sommerengineering.signalvoice.uitls.productId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val client =
        BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener { result, purchases -> handlePurchase(result, purchases) }
            .build()

    private val _purchaseEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1 // ensure no race between purchase event and entitlement update
    )
    val purchaseEvents = _purchaseEvents.asSharedFlow()

    suspend fun connect(): Boolean {

        if (client.isReady) return true

        return suspendCancellableCoroutine { continuation ->
            client.startConnection(
                object : BillingClientStateListener {
                    override fun onBillingServiceDisconnected() =
                        logMessage("Billing service disconnected")

                    override fun onBillingSetupFinished(result: BillingResult) {
                        val isSuccess = result.responseCode == BillingClient.BillingResponseCode.OK
                        if (!isSuccess) {
                            logMessage("Billing connection failed with code: ${result.responseCode}")
                        }
                        continuation.resume(isSuccess)
                    }
                }
            )
        }
    }

    suspend fun isPremium(): Boolean {

        val isConnected = connect()
        if (!isConnected) {
            logMessage("Cannot check premium status without billing connection")
            return false
        }

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

    suspend fun launchBillingFlow(activity: MainActivity) {

        val isConnected = connect()
        if (!isConnected) {
            logMessage("Cannot launch billing flow without billing connection")
            return
        }

        // fetch configured product
        val result = client.queryProductDetails(
            QueryProductDetailsParams
                .newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product
                            .newBuilder()
                            .setProductId(productId)
                            .setProductType(ProductType.SUBS)
                            .build()
                    )
                )
                .build()
        )

        val isSuccess = result.billingResult.responseCode == BillingClient.BillingResponseCode.OK
        if (!isSuccess) {
            logMessage("Failed to query product details with code: ${result.billingResult.responseCode}")
            return
        }

        // extract product
        val product = result.productDetailsList?.firstOrNull()
        if (product == null) {
            logMessage("Product details not found for product id: $productId")
            return
        }

        // extract subscription offer
        val offer = product.subscriptionOfferDetails?.firstOrNull()
        if (offer == null) {
            logMessage("No subscription offers found for product id: $productId")
            return
        }

        // launch billing flow
        client.launchBillingFlow(
            activity,
            BillingFlowParams
                .newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams
                            .newBuilder()
                            .setProductDetails(product)
                            .setOfferToken(offer.offerToken)
                            .build()
                    )
                )
                .build()
        )
    }

    private fun handlePurchase(
        result: BillingResult,
        purchases: List<Purchase>?
    ) {

        // user canceled flow
        val isCanceled = result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED
        if (isCanceled) {
            logMessage("User canceled billing flow")
            return
        }

        // validate purchase result
        val isSuccess = result.responseCode == BillingClient.BillingResponseCode.OK
        if (!isSuccess) {
            logMessage("Purchase failed with code: ${result.responseCode}")
            return
        }

        // extract purchase
        val purchase = purchases?.firstOrNull {
            it.products.contains(productId)
        }
        if (purchase == null) {
            logMessage("Purchase successful but no purchase data found")
            return
        }

        // validate purchase
        val isValid = purchase.products.contains(productId)
        val isPurchased = purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        if (!isValid || !isPurchased) {
            logMessage("Purchase invalid")
            return
        }

        // already acknowledged
        if (purchase.isAcknowledged) {
            logMessage("Purchase already acknowledged, updating entitlement ...")
            _purchaseEvents.tryEmit(Unit)
            return
        }

        // acknowledge purchase
        client.acknowledgePurchase(
            AcknowledgePurchaseParams
                .newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
        ) { result ->

            val isSuccess = result.responseCode == BillingClient.BillingResponseCode.OK
            if (!isSuccess) {
                logMessage("Failed to acknowledge purchase with code: ${result.responseCode}")
                return@acknowledgePurchase
            }

            logMessage("Purchase acknowledged successfully, updating entitlement ...")
            _purchaseEvents.tryEmit(Unit)
        }
    }
}