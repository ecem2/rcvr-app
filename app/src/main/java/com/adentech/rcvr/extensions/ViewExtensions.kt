package com.adentech.rcvr.extensions

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes

fun View.color(@ColorRes resource: Int): Int {
    return context.color(resource)
}

fun View.drawable(@DrawableRes resource: Int): Drawable? {
    return context.drawable(resource)
}

fun View.dimenToPx(@DimenRes dimen: Int): Int {
    return context.dimenToPx(dimen)
}

fun View.font(@FontRes resource: Int): Typeface? {
    return context.font(resource)
}