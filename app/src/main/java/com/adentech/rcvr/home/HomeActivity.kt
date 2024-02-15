package com.adentech.rcvr.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.adentech.rcvr.common.ArgumentKey
import com.adentech.rcvr.activities.BaseActivity
import com.adentech.rcvr.databinding.ActivityHomeBinding
import com.adentech.rcvr.onboard.OnboardActivity
import com.adentech.rcvr.home.ImagesActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity() : BaseActivity<MainViewModel, ActivityHomeBinding>() {

    private var isSplashDone = false


    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition {
            isSplashDone
        }
    }

    override fun viewModelClass() = MainViewModel::class.java

    override fun viewDataBindingClass() = ActivityHomeBinding::class.java

    override fun onInitDataBinding() {
        if (viewModel.preferences.getFirstTimeLaunch()) {
            isSplashDone = true
            launchOnboard()
        } else {
            startActivity(Intent(ImagesActivity.newIntent(this@HomeActivity)))
            finish()
        }
        isSplashDone = false
    }

    private fun launchOnboard() {
        startActivity(OnboardActivity.newIntent(this)).also {
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context, returnScreen: String? = null) =
            Intent(context, HomeActivity::class.java).apply {
                putExtra(ArgumentKey.HOME_SCREEN, returnScreen)
            }
    }
}