package com.adentech.rcvr.binding

import android.net.Uri
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.adentech.rcvr.common.ImageManager
import com.makeramen.roundedimageview.RoundedImageView

@BindingAdapter("imageUrl")
fun AppCompatImageView.setImageUrl(url: Uri) {
    ImageManager.setImageUrl(url, this)
}

@BindingAdapter("roundedImageUrl")
fun RoundedImageView.setImageUrl(url: Uri) {
    ImageManager.setRoundedImageUrl(url, this)
}