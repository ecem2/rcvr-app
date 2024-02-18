package com.adentech.rcvr.core.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.adentech.rcvr.core.common.ViewBindingUtil
import com.adentech.rcvr.databinding.ActivityBaseBinding
import com.adentech.rcvr.core.viewmodel.BaseViewModel
import com.google.android.gms.ads.interstitial.InterstitialAd

abstract class BaseActivity<VM : BaseViewModel, DB : ViewDataBinding> :
    RecoveryBaseVmDbActivity<VM, DB>() {

    private lateinit var baseViewBinding: ViewDataBinding
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemUI()
    }

    override fun setResourceViewBinding(): View {
        baseViewBinding =
            ViewBindingUtil.inflate<ActivityBaseBinding>(layoutInflater)
        viewBinding = ViewBindingUtil.inflate(
            layoutInflater,
            (baseViewBinding as ActivityBaseBinding).baseContentFrame,
            true,
            viewDataBindingClass()
        )
        viewBinding.lifecycleOwner = this
        return baseViewBinding.root
    }

    fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }
}