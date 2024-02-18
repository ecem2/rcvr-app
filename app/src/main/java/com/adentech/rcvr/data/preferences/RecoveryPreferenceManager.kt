package com.adentech.rcvr.data.preferences


import android.content.Context
import com.adentech.rcvr.data.preferences.PreferenceConstants.IS_FIRST_TIME_LAUNCH
import com.adentech.rcvr.data.preferences.PreferenceConstants.IS_USER_PREMIUM
import com.adentech.rcvr.data.preferences.PreferenceConstants.LAST_RESET_DATE
import com.adentech.rcvr.data.preferences.PreferenceConstants.REWARD_COUNT
import com.adentech.rcvr.extensions.delete
import com.adentech.rcvr.extensions.set
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecoveryPreferenceManager@Inject constructor(
    @ApplicationContext
    private val context: Context
): RecoverySharedPreferences(context), Preferences {

    override fun getPrefName() = "RecoveryPrefs"

    override fun setFirstTimeLaunch(isFirstTime: Boolean) {
        prefs.set(
            IS_FIRST_TIME_LAUNCH,
            isFirstTime
        )
    }

    override fun getFirstTimeLaunch(): Boolean {
        return prefs.getBoolean(
            IS_FIRST_TIME_LAUNCH,
            true
        )
    }

    override fun setIsUserPremium(isPremium: Boolean) {
        prefs.set(
            IS_USER_PREMIUM,
            isPremium
        )
    }

    override fun getIsUserPremium(): Boolean {
        return prefs.getBoolean(
            IS_USER_PREMIUM,
            false
        )
    }

    override fun setLastResetDate(date: Long) {
        prefs.set(
            LAST_RESET_DATE,
            date
        )
    }

    override fun getLastResetDate(): Long {
        return prefs.getLong(
            LAST_RESET_DATE,
            0
        )
    }

    override fun setRewardCount(count: Int) {
        prefs.set(
            REWARD_COUNT,
            count
        )
    }

    override fun getRewardCount(): Int {
        return prefs.getInt(
            REWARD_COUNT,
            3
        )
    }

    override fun resetRewardData() {
        prefs.delete(LAST_RESET_DATE)
        prefs.delete(REWARD_COUNT)
    }
}