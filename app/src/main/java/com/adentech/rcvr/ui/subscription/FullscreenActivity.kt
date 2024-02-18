package com.adentech.rcvr.ui.subscription

import android.annotation.SuppressLint
import android.webkit.WebViewClient
import com.adentech.rcvr.core.activities.BaseActivity
import com.adentech.rcvr.core.common.Constants.PRIVACY_POLICY_LINK
import com.adentech.rcvr.core.common.Constants.TERMS_OF_USE_LINK
import com.adentech.rcvr.databinding.ActivityFullscreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullscreenActivity : BaseActivity<SubscriptionViewModel, ActivityFullscreenBinding>() {

    override fun viewModelClass() = SubscriptionViewModel::class.java

    override fun viewDataBindingClass() = ActivityFullscreenBinding::class.java

    @SuppressLint("SetJavaScriptEnabled")
    override fun onInitDataBinding() {
        val title = intent.getStringExtra("title")
        viewBinding.apply {
            webView.webViewClient = WebViewClient()
            val url: String = if (title == "PRIVACY") {
                PRIVACY_POLICY_LINK
            } else {
                TERMS_OF_USE_LINK
            }
            webView.loadUrl(url)
            webView.settings.javaScriptEnabled = true
            webView.settings.setSupportZoom(true)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (viewBinding.webView.canGoBack()) {
            viewBinding.webView.goBack()
        }
        super.onBackPressed()
    }
}