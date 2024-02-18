package com.adentech.rcvr.data.billing

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.BuildConfig
import androidx.multidex.MultiDex
import com.adentech.rcvr.core.common.RemoteConfigUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RecoveryApplication : Application() {

    companion object {
        const val isPremium = false // Testte premium acmak icin true yap
        var hasSubscription = if (BuildConfig.DEBUG) {
            isPremium
        } else {
            false
        }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        RemoteConfigUtils.init()
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}