package com.adentech.rcvr.extensions

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

fun Context.dimenToPx(@DimenRes dimen: Int): Int {
    return resources.dimenToPx(dimen)
}

fun Context.font(@FontRes resource: Int): Typeface? {
    return ResourcesCompat.getFont(this, resource)
}

fun Context.color(@ColorRes resource: Int): Int {
    return ContextCompat.getColor(this, resource)
}

fun Context.drawable(@DrawableRes resource: Int): Drawable? {
    return ContextCompat.getDrawable(this, resource)
}
