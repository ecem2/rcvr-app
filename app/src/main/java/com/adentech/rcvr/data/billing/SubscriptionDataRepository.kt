package com.adentech.rcvr.data.billing

import com.adentech.rcvr.core.common.Constants.MONTHLY_PREMIUM
import com.adentech.rcvr.core.common.Constants.WEEKLY_PREMIUM
import com.adentech.rcvr.core.common.Constants.YEARLY_PREMIUM
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class SubscriptionDataRepository(billingClientWrapper: BillingClientWrapper) {

    val weeklyBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList->
        purchaseList.any { purchase ->
            purchase.products.contains(WEEKLY_PREMIUM) && purchase.isAutoRenewing
        }
    }

    val monthlyBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList->
        purchaseList.any { purchase ->
            purchase.products.contains(MONTHLY_PREMIUM) && purchase.isAutoRenewing
        }
    }

    val yearlyBasic: Flow<Boolean> = billingClientWrapper.purchases.map { purchaseList->
        purchaseList.any { purchase ->
            purchase.products.contains(YEARLY_PREMIUM) && purchase.isAutoRenewing
        }
    }

    val weeklyDetail: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(WEEKLY_PREMIUM)
        }.map { it[WEEKLY_PREMIUM]!! }

    val monthlyDetail: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(MONTHLY_PREMIUM)
        }.map { it[MONTHLY_PREMIUM]!! }

    val yearlyDetail: Flow<ProductDetails> =
        billingClientWrapper.productWithProductDetails.filter {
            it.containsKey(YEARLY_PREMIUM)
        }.map { it[YEARLY_PREMIUM]!! }

    val purchases: Flow<List<Purchase>> = billingClientWrapper.purchases
    val isNewPurchaseAcknowledged: Flow<Boolean> = billingClientWrapper.isNewPurchaseAcknowledged
    val billingErrorMessage: Flow<String> = billingClientWrapper.billingErrorMessage
}