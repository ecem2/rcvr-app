package com.adentech.rcvr.extensions

import android.os.Handler
import android.os.Looper

fun withDelay(delay: Long, block: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(Runnable(block), delay)
}