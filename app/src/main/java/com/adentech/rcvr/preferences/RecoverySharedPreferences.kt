package com.adentech.rcvr.preferences

import android.content.Context

abstract class RecoverySharedPreferences(context: Context) {

    abstract fun getPrefName(): String

    protected val prefs = context.getSharedPreferences(getPrefName(), Context.MODE_PRIVATE)

}