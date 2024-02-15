package com.adentech.rcvr.model

import android.os.Parcelable
import com.google.errorprone.annotations.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class MainFileModel(
    val files: ArrayList<FileModel>,
    val images: ArrayList<FileModel>,
    val audios: ArrayList<FileModel>,
    val videos: ArrayList<FileModel>
): Parcelable