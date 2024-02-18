package com.adentech.rcvr.core.common

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

object RemoteConfigUtils {

    private const val TAG = "RemoteConfigUtils"
    private const val IS_RELEASED = "is_released"
    private const val WEEKLY_ONLY = "week_only"
    private const val MONTHLY_ONLY = "month_only"
    private const val YEARLY_ONLY = "yearly_only"
    private const val MONTHLY_BUTTON = "is_monthly"
    private const val YEARLY_BUTTON = "is_yearly"
    private const val WEEKLY_BUTTON = "is_weekly"

    private val DEFAULTS : HashMap<String, Any> = hashMapOf(
        WEEKLY_BUTTON to false
    )

    @SuppressLint("StaticFieldLeak")
    private lateinit var remoteConfig: FirebaseRemoteConfig

    fun init() {
        remoteConfig = getFirebaseRemoteConfig()
    }

    private fun getFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }

        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
            setDefaultsAsync(DEFAULTS)
            fetchAndActivate().addOnCompleteListener {
                if (it.isSuccessful) {
                    val isReleased: Boolean = remoteConfig.getBoolean(IS_RELEASED)
                    Log.d(TAG, "isReleased: $isReleased")
                    val weekOnly: Boolean = remoteConfig.getBoolean(WEEKLY_ONLY)
                    Log.d(TAG, "weekOnly: $weekOnly")
                    val monthOnly: Boolean = remoteConfig.getBoolean(MONTHLY_ONLY)
                    Log.d(TAG, "monthOnly: $monthOnly")
                    val yearOnly: Boolean = remoteConfig.getBoolean(YEARLY_ONLY)
                    Log.d(TAG, "yearOnly: $yearOnly")
                    val monthlyButtonBoolean: Boolean = remoteConfig.getBoolean(MONTHLY_BUTTON)
                    Log.d(TAG, "monthlyButtonBoolean: $monthlyButtonBoolean")
                    val yearlyButtonBoolean: Boolean = remoteConfig.getBoolean(YEARLY_BUTTON)
                    Log.d(TAG, "yearlyButtonBoolean: $yearlyButtonBoolean")
                }
            }
        }
        return remoteConfig
    }

    fun checkWeekOnly(): Boolean = remoteConfig.getBoolean(WEEKLY_ONLY)
    fun checkMonthOnly(): Boolean = remoteConfig.getBoolean(MONTHLY_ONLY)
    fun checkYearlyOnly(): Boolean = remoteConfig.getBoolean(YEARLY_ONLY)


    fun getReleased(): Boolean = remoteConfig.getBoolean(IS_RELEASED)

}