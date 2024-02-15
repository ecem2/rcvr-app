package com.adentech.rcvr.extensions

import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.appcompat.widget.AppCompatTextView

fun TextView.setTypeface(@FontRes font: Int) {
    typeface = font(font)
}

fun TextView.strike(isShow: Boolean) {
    paintFlags = if (isShow) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

fun TextView.underline(isShow: Boolean) {
    paintFlags = if (isShow) {
        paintFlags or Paint.UNDERLINE_TEXT_FLAG
    } else {
        paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
    }
}

fun AppCompatTextView.replaceTextBackground(str: String, color: Int) {
    val raw: Spannable = SpannableString(this.text)
    val spans = raw.getSpans(
        0, raw.length,
        BackgroundColorSpan::class.java
    )
    for (span in spans) {
        raw.removeSpan(span)
    }
    var index = TextUtils.indexOf(raw, str)
    while (index >= 0) {
        raw.setSpan(
            BackgroundColorSpan(color),
            index,
            index + str.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        index = TextUtils.indexOf(raw, str, index + str.length)
    }
    this.text = raw
}