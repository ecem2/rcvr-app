package com.adentech.rcvr.subscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.adentech.rcvr.BuildConfig
import com.adentech.rcvr.BuildConfig.MONTHLY_PREMIUM
import com.adentech.rcvr.BuildConfig.YEARLY_PREMIUM
import com.adentech.rcvr.R
import com.adentech.rcvr.adapter.ViewPagerAdapter
import com.adentech.rcvr.common.ArgumentKey
import com.adentech.rcvr.common.Constants
import com.adentech.rcvr.common.Constants.FROM_SUBSCRIPTION
import com.adentech.rcvr.common.RemoteConfigUtils
import com.adentech.rcvr.activities.BaseActivity
import com.adentech.rcvr.billing.MainState
import com.adentech.rcvr.databinding.ActivitySubscriptionBinding
import com.adentech.rcvr.extensions.observe
import com.adentech.rcvr.home.HomeActivity
import com.adentech.rcvr.home.MainViewModel
import com.adentech.rcvr.images.DeletedImagesFragment
import com.adentech.rcvr.videos.DeletedVideosFragment
import com.adentech.rcvr.viewpager.SetPagerDummy
import com.android.billingclient.api.Purchase
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class SubscriptionActivity() : BaseActivity<MainViewModel, ActivitySubscriptionBinding>() {

    private var selectedSub: String? = null
    private var weeklyPrice: String? = null
    private var monthlyPrice: String? = null
    private var yearlyPrice: String? = null
    private var productsForSale: MainState? = null
    private var currentPurchases: List<Purchase> = listOf()
    private var subsDone: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null
    private var errorMessage: String? = null
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun viewModelClass() = MainViewModel::class.java

    override fun viewDataBindingClass() = ActivitySubscriptionBinding::class.java

    override fun onInitDataBinding() {
        MobileAds.initialize(this@SubscriptionActivity) {
            loadInterAd()
        }.also {
            val testDeviceIds = Arrays.asList("\tca-app-pub-3940256099942544/1033173712")
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
            )
        }
        setData()
        setupRemoteConfig()
        clickListeners()
        observe(viewModel.billingConnectionState, ::onBillingConnected)
        observe(viewModel.subscriptionType, ::onSubscriptionTypeChanged)
        selectedSub = YEARLY_PREMIUM

    }

    private fun clickListeners() {
        viewBinding.apply {
            ivCloseButton.setOnClickListener {
                if (mInterstitialAd == null) {
                    finish()
                } else {
                    loadInterAd()
                    showInterAds()
                }
            }


            monthlyButton.setOnClickListener {
                selectedSub = MONTHLY_PREMIUM
                monthlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                yearlyButton.background = null
                monthlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )
                yearlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.white
                    )
                )
            }

            yearlyButton.setOnClickListener {
                selectedSub = YEARLY_PREMIUM
                monthlyButton.background = null
                yearlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                monthlyButton.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
                yearlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )

            }

            termsOfUse.setOnClickListener {
                val intent = Intent(this@SubscriptionActivity, FullscreenActivity::class.java)
                intent.putExtra("title", "TERMS")
                startActivity(intent)
            }

            privacyPolicy.setOnClickListener {
                val intent = Intent(this@SubscriptionActivity, FullscreenActivity::class.java)
                intent.putExtra("title", "PRIVACY")
                startActivity(intent)
            }
            monthlyButton.setOnClickListener {
                when (selectedSub) {
                    MONTHLY_PREMIUM -> {
                        buyMonthlyPremium()
                    }
                }
            }
            yearlyButton.setOnClickListener {
                when (selectedSub) {
                    YEARLY_PREMIUM -> {
                        buyYearlyPremium()
                    }
                }
            }

        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val parentIntent = NavUtils.getParentActivityIntent(this)
                parentIntent?.flags =
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(parentIntent)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setData() {
        viewPagerAdapter = ViewPagerAdapter()

        viewPagerAdapter.setPagerAdapter(SetPagerDummy.setDataPager())
        viewBinding.viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(viewBinding.intoTabLayout, viewBinding.viewPager) { tab, position -> }.attach()

    }

    private fun setupRemoteConfig() {
        if (RemoteConfigUtils.checkMonthOnly().toString() == "true") {
            viewBinding.apply {
                selectedSub = MONTHLY_PREMIUM
                monthlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                monthlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )
                yearlyButton.text = yearlyPrice
                // subscriptionName.text = getString(R.string.weekly_title)
            }
        } else if (RemoteConfigUtils.checkYearlyOnly().toString() == "true") {
            selectedSub = YEARLY_PREMIUM
            viewBinding.apply {
                yearlyButton.background = ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.subscription_toggle_selected_bg
                )
                yearlyButton.setTextColor(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.black_text
                    )
                )
                monthlyButton.text = monthlyPrice
                //subscriptionName.text = getString(R.string.yearly_title)
            }
        }
    }
    private fun onSubscriptionTypeChanged(subscriptionType: MainViewModel.SubscriptionType) {
        if (subscriptionType != MainViewModel.SubscriptionType.NOT_SUBSCRIBED) {
            val intent = Intent(this@SubscriptionActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun onBillingConnected(value: Boolean) {
        if (!value) {
            // When false connection to the billing library is not established yet.
        } else {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    viewModel.productsForSaleFlows.collectLatest {
                        productsForSale = it
                        showProducts()
                    }

                    viewModel.currentPurchasesFlow.collectLatest {
                        currentPurchases = it
                    }

                    viewModel.isAcknowledged.collectLatest {

                        subsDone = it
                    }

                    viewModel.errorMessage.collectLatest {
                        errorMessage = it
                        if (it.isNotBlank() && it != "") {
                            Toast.makeText(
                                this@SubscriptionActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        if (currentPurchases.isNotEmpty() || subsDone) {

            val intent = Intent(this@SubscriptionActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun showProducts() {
        productsForSale?.basicMonthlyDetails?.subscriptionOfferDetails?.forEach {
            it.pricingPhases.pricingPhaseList.forEach { it1 ->
                monthlyPrice = it1.formattedPrice
            }
        }

        productsForSale?.basicYearlyDetails?.subscriptionOfferDetails?.forEach {
            it.pricingPhases.pricingPhaseList.forEach { prodDetail ->
                yearlyPrice = prodDetail.formattedPrice
            }
        }
    }

    private fun buyWeeklyPremium() {
        productsForSale?.basicWeeklyDetails?.let {
            viewModel.buy(
                productDetails = it,
                activity = this@SubscriptionActivity
            )
        }
    }

    private fun buyMonthlyPremium() {
        productsForSale?.basicMonthlyDetails?.let {
            viewModel.buy(
                productDetails = it,
                activity = this@SubscriptionActivity
            )
        }
    }

    private fun buyYearlyPremium() {
        productsForSale?.basicYearlyDetails?.let {
            viewModel.buy(
                productDetails = it,
                activity = this@SubscriptionActivity
            )
        }
    }

    private fun showInterAds() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    mInterstitialAd = null
                    finish()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    mInterstitialAd = null
                    finish()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                }
            }
            mInterstitialAd?.show(this@SubscriptionActivity)
        } else {
            finish()
        }
    }
    private fun loadInterAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            BuildConfig.INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    Log.e("InterstitialAd", "Failed to load ad: $adError")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    Log.d("InterstitialAd", "Ad loaded successfully")
                }
            })
    }
    override fun onBackPressed() {
        showInterAds()
    }
    companion object {
        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, SubscriptionActivity::class.java).apply {
                putExtra(ArgumentKey.SUBSCRIPTION_SCREEN, returnScreen)
            }
    }
}

