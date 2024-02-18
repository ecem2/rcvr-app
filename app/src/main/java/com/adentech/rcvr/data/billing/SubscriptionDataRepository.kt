package com.adentech.rcvr.data.billing


import com.adentech.rcvr.BuildConfig
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class SubscriptionDataRepository(billingClientWrapper: BillingClientWrapper) {

    val weeklyBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList->
        purchaseList.any { purchase ->
            purchase.products.contains(BuildConfig.WEEKLY_PREMIUM) && purchase.isAutoRenewing
        }
    }

    val monthlyBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList->
        purchaseList.any { purchase ->
            purchase.products.contains(BuildConfig.MONTHLY_PREMIUM) && purchase.isAutoRenewing
        }
    }

    val yearlyBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList->
        purchaseList.any { purchase ->
            purchase.products.contains(BuildConfig.YEARLY_PREMIUM) && purchase.isAutoRenewing
        }
    }

    val weeklyDetail: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(BuildConfig.WEEKLY_PREMIUM)
        }.map { it[BuildConfig.WEEKLY_PREMIUM]!! }

    val monthlyDetail: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(BuildConfig.MONTHLY_PREMIUM)
        }.map { it[BuildConfig.MONTHLY_PREMIUM]!! }

    val yearlyDetail: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(BuildConfig.YEARLY_PREMIUM)
        }.map { it[BuildConfig.YEARLY_PREMIUM]!! }

    val purchases: Flow<List<Purchase>> = billingClientWrapper.purchases
    val isNewPurchaseAcknowledged: Flow<Boolean> = billingClientWrapper.isNewPurchaseAcknowledged
    val billingErrorMessage: Flow<String> = billingClientWrapper.billingErrorMessage
}