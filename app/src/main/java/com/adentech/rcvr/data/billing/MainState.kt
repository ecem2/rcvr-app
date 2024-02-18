package com.adentech.rcvr.data.billing

import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase

data class MainState(
    val hasWeeklyBasic: Boolean? = false,
    val basicWeeklyDetails: ProductDetails? = null,
    val hasMonthlyBasic: Boolean? = false,
    val basicMonthlyDetails: ProductDetails? = null,
    val hasYearlyBasic: Boolean? = false,
    val basicYearlyDetails: ProductDetails? = null,
    val purchases: List<Purchase>? = null,
)