package com.adentech.rcvr.common

import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView

object ImageManager {
    fun setImageUrl(url: Uri, imageView: AppCompatImageView) {
        Glide.with(imageView)
            .load(url)
            .into(imageView)
    }

    fun setRoundedImageUrl(url: Uri, imageView: RoundedImageView) {
        Glide.with(imageView)
            .load(url)
            .into(imageView)
    }
}