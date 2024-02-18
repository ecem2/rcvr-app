package com.adentech.rcvr.data.preferences


interface Preferences {

    fun setFirstTimeLaunch(isFirstTime: Boolean)
    fun getFirstTimeLaunch(): Boolean

    fun setIsUserPremium(isPremium: Boolean)
    fun getIsUserPremium(): Boolean

    fun setLastResetDate(date: Long)
    fun getLastResetDate(): Long

    fun setRewardCount(count: Int)
    fun getRewardCount(): Int

    fun resetRewardData()
}