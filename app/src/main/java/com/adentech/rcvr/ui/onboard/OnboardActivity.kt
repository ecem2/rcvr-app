package com.adentech.rcvr.ui.onboard

import android.content.Context
import android.content.Intent
import com.adentech.rcvr.core.common.ArgumentKey
import com.adentech.rcvr.core.activities.BaseActivity
import com.adentech.rcvr.databinding.ActivityOnboardBinding
import com.adentech.rcvr.ui.home.MainViewModel
import com.adentech.rcvr.ui.home.ImagesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardActivity : BaseActivity<MainViewModel, ActivityOnboardBinding>() {

    override fun viewModelClass() = MainViewModel::class.java

    override fun viewDataBindingClass() = ActivityOnboardBinding::class.java

    override fun onInitDataBinding() {
        hideSystemUI()
        viewBinding.continueButton.setOnClickListener {
            viewModel.preferences.setFirstTimeLaunch(false)
            startActivity(ImagesActivity.newIntent(this@OnboardActivity)).also {
                finishAffinity()
            }
        }
    }

    companion object {
        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, OnboardActivity::class.java).apply {
                putExtra(ArgumentKey.ONBOARD_SCREEN, returnScreen)
            }
    }
}