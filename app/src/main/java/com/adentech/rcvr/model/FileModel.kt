package com.adentech.rcvr.model

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class FileModel(
    var fileName: String?,
    var fileExtension: String?,
    var location: FileLocation,
    var isSelected: Boolean?,
    var creationDate: Long?,
    var fileSize: String?,
    var isDeleted: Boolean?,
    var imageUri: Uri? ,
    var videoUri: Uri? = null,
    var audioUri: Uri? = null,
    var isPreviewMode: Boolean = false,
    var isRewarded: Boolean?
) : Parcelable {
}

enum class FileLocation {
    GALLERY, NO_MEDIA
}