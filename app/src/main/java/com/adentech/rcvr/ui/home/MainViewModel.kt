package com.adentech.rcvr.ui.home


import android.app.Activity
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adentech.rcvr.core.common.Resource
import com.adentech.rcvr.core.viewmodel.BaseViewModel
import com.adentech.rcvr.data.billing.BillingClientWrapper
import com.adentech.rcvr.data.billing.MainState
import com.adentech.rcvr.data.billing.SubscriptionDataRepository
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.data.preferences.Preferences
import com.adentech.rcvr.data.repository.ImageRepository
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    val preferences: Preferences,
    private val repository: ImageRepository
) : BaseViewModel() {

    var isProgressDone: Boolean = false
    private val _imageList = MutableLiveData<Resource<ArrayList<FileModel>>>()
    val imageList: LiveData<Resource<ArrayList<FileModel>>> = _imageList

    private val _videoList = MutableLiveData<Resource<ArrayList<FileModel>>>()
    val videoList: LiveData<Resource<ArrayList<FileModel>>> = _videoList
    init {
        _imageList.postValue(Resource.loading(null))
        _videoList.postValue(Resource.loading(null))

    }

    fun getAllGalleryImages() = viewModelScope.launch {
        val images = repository.getGalleryImages()
        if (images.data.isNullOrEmpty() || images.data.size == 0) {
            _imageList.postValue(Resource.error(images.message.toString(), null))
        } else {
            _imageList.postValue(images)
        }
    }
    fun getAllGalleryVideos() = viewModelScope.launch {
        val videos = repository.getGalleryImages()
        if (videos.data.isNullOrEmpty() || videos.data.size == 0) {
            _videoList.postValue(Resource.error(videos.message.toString(), null))
        } else {
            _videoList.postValue(videos)
        }
    }

    private var billingClient: BillingClientWrapper = BillingClientWrapper(application, preferences)

    private var repo: SubscriptionDataRepository =
        SubscriptionDataRepository(billingClientWrapper = billingClient)

    private val _billingConnectionState = MutableLiveData(false)
    val billingConnectionState: LiveData<Boolean> = _billingConnectionState

    private val _subscriptionType = MutableLiveData<SubscriptionType>()
    val subscriptionType: LiveData<SubscriptionType> = _subscriptionType

    val productsForSaleFlows = combine(
        repo.weeklyDetail,
        repo.monthlyDetail,
        repo.yearlyDetail
    ) { weekly, monthly, yearly ->
        MainState(
            basicWeeklyDetails = weekly,
            basicMonthlyDetails = monthly,
            basicYearlyDetails = yearly
        )
    }

    private val userCurrentSubscriptionFlow = combine(
        repo.weeklyBasic,
        repo.monthlyBasic,
        repo.yearlyBasic,
    ) { hasWeeklyBasic, hasMonthlyBasic, hasYearlyBasic ->
        MainState(
            hasWeeklyBasic = hasWeeklyBasic,
            hasMonthlyBasic = hasMonthlyBasic,
            hasYearlyBasic = hasYearlyBasic
        )
    }

    val currentPurchasesFlow = repo.purchases
    val isAcknowledged = repo.isNewPurchaseAcknowledged
    val errorMessage = repo.billingErrorMessage

    init {
        viewModelScope.launch {
            billingClient.startBillingConnection(billingConnectionState = _billingConnectionState)
            userCurrentSubscriptionFlow.collectLatest { collectedSubscriptions ->
                when {
                    collectedSubscriptions.hasWeeklyBasic == true &&
                            collectedSubscriptions.hasMonthlyBasic == false &&
                            collectedSubscriptions.hasYearlyBasic == false -> {
                        _subscriptionType.postValue(SubscriptionType.WEEKLY)
                    }

                    collectedSubscriptions.hasMonthlyBasic == true &&
                            collectedSubscriptions.hasYearlyBasic == false &&
                            collectedSubscriptions.hasWeeklyBasic == false -> {
                        _subscriptionType.postValue(SubscriptionType.MONTHLY)
                    }

                    collectedSubscriptions.hasYearlyBasic == true &&
                            collectedSubscriptions.hasMonthlyBasic == false &&
                            collectedSubscriptions.hasWeeklyBasic == false -> {
                        _subscriptionType.postValue(SubscriptionType.YEARLY)
                    }
                }
            }
        }
    }

    private fun retrieveEligibleOffers(
        offerDetails: MutableList<ProductDetails.SubscriptionOfferDetails>,
        tag: String,
    ): List<ProductDetails.SubscriptionOfferDetails> {
        val eligibleOffers = emptyList<ProductDetails.SubscriptionOfferDetails>().toMutableList()
        offerDetails.forEach { offerDetail ->
            if (offerDetail.offerTags.contains(tag)) {
                eligibleOffers.add(offerDetail)
            }
        }

        return eligibleOffers
    }

    private fun leastPricedOfferToken(
        offerDetails: List<ProductDetails.SubscriptionOfferDetails>,
    ): String {
        var offerToken = String()

        if (offerDetails.isNotEmpty()) {
            for (offer in offerDetails) {
                for (price in offer.pricingPhases.pricingPhaseList) {
                    offerToken = offer.offerToken
                }
            }
        }
        return offerToken
    }

    private fun billingFlowParamsBuilder(
        productDetails: ProductDetails,
        offerToken: String,
    ): BillingFlowParams.Builder {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        )
    }

    fun buy(
        productDetails: ProductDetails,
        activity: Activity
    ) {
        val offerList: ArrayList<String> = ArrayList()
        productDetails.subscriptionOfferDetails?.forEach {
            offerList.add(it.offerToken)
        }
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerList[0])
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

//        val offers = productDetails.subscriptionOfferDetails?.let {
//            retrieveEligibleOffers(
//                offerDetails = it,
//                tag = tag.lowercase()
//            )
//        }
//        val offerToken = offers?.let { leastPricedOfferToken(it) }
//        val billingParams = offerToken?.let {
//            billingFlowParamsBuilder(
//                productDetails = productDetails,
//                offerToken = it
//            )
//        }
//        if (billingParams != null) {
//            billingClient.launchBillingFlow(
//                activity,
//                billingParams.build()
//            )
//        }
    }

    override fun onCleared() {
        billingClient.terminateBillingConnection()
    }

    enum class SubscriptionType {
        NOT_SUBSCRIBED,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}