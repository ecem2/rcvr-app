package com.adentech.rcvr.extensions

import android.content.Context
import com.adentech.rcvr.common.Constants.EMPTY_STRING

fun Any.getString(context: Context): String {
    return when (this) {
        is String -> {
            this
        }
        is Int -> {
            context.getString(this)
        }
        else -> {
            EMPTY_STRING
        }
    }
}