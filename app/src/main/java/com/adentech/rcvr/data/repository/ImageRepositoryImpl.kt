package com.adentech.rcvr.data.repository


import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.adentech.rcvr.core.common.Resource
import com.adentech.rcvr.data.model.FileLocation
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.data.model.MainFileModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context
) : ImageRepository {

    override suspend fun getGalleryImages(): Resource<ArrayList<FileModel>> =
        withContext(Dispatchers.IO) {
            try {
                Resource.loading(null)
                val allGalleryImages: ArrayList<FileModel> = ArrayList()

                val imageProjection = arrayOf(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA
                )
                val imageSortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
                val cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageProjection,
                    null,
                    null,
                    imageSortOrder
                )

                cursor.use {
                    it?.let {
                        val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                        val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                        val imgPath = it.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
                        val dateColumn =
                            it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                        while (it.moveToNext()) {
                            val id = it.getLong(idColumn)
                            val size = it.getString(sizeColumn)
                            val date = it.getLong(dateColumn)
                            val contentUri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                            val filePath = it.getString(imgPath)

                            Log.d("Salimmm", "filePath $filePath")
                            val fileSize = if (!size.isNullOrEmpty()) {
                                (size.toLong() / 1024).toString() + " kb"
                            } else {
                                "0 mb"
                            }
                            allGalleryImages.add(
                                FileModel(
                                    fileName = "",
                                    fileExtension = "",
                                    location = FileLocation.GALLERY,
                                    isSelected = false,
                                    creationDate = date,
                                    fileSize = fileSize,
                                    isDeleted = false,
                                    imageUri = contentUri,
                                    videoUri = contentUri,
                                    audioUri = contentUri,
                                    isRewarded = false
                                )
                            )
                        }
                    } ?: kotlin.run {
                        Log.e("TAG", "Cursor is null!")
                    }
                }

                Resource.success(allGalleryImages)
            } catch (e: Exception) {
                Resource.error("No data! ${e.localizedMessage}", null)
            }
        }

    override suspend fun getGalleryVideos(): Resource<ArrayList<FileModel>> =
        withContext(Dispatchers.IO) {
            try {
                Resource.loading(null)
                val allGalleryVideos: ArrayList<FileModel> = ArrayList()

                val videoProjection = arrayOf(
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_TAKEN,
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DATA
                )
                val videoSortOrder = "${MediaStore.Video.Media.DATE_TAKEN} DESC"
                val cursor = context.contentResolver.query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    videoProjection,
                    null,
                    null,
                    videoSortOrder
                )

                cursor.use {
                    it?.let {
                        val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                        val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                        val dateColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
                        while (it.moveToNext()) {
                            val id = it.getLong(idColumn)
                            val size = it.getString(sizeColumn)
                            val date = it.getLong(dateColumn)
                            val contentUri = ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                id
                            )

                            val fileSize = if (!size.isNullOrEmpty()) {
                                (size.toLong() / 1024).toString() + " kb"
                            } else {
                                "0 mb"
                            }
                            allGalleryVideos.add(
                                FileModel(
                                    fileName = "",
                                    fileExtension = "",
                                    location = FileLocation.GALLERY,
                                    isSelected = false,
                                    creationDate = date,
                                    fileSize = fileSize,
                                    isDeleted = false,
                                    imageUri = contentUri,
                                    videoUri = contentUri,
                                    audioUri = contentUri,
                                    isRewarded = false
                                )
                            )
                        }
                    } ?: kotlin.run {
                        Log.e("TAG", "Cursor is null!")
                    }
                }

                Resource.success(allGalleryVideos)
            } catch (e: Exception) {
                Resource.error("No data! ${e.localizedMessage}", null)
            }
        }

    override suspend fun getAllTrashFiles(): Resource<MainFileModel> =
        withContext(Dispatchers.IO) {
            try {
                Resource.loading(null)
                var trashedFiles: MainFileModel? = null
                val audioList: ArrayList<FileModel> = ArrayList()
                val videoList: ArrayList<FileModel> = ArrayList()
                val imageList: ArrayList<FileModel> = ArrayList()
                val fileList: ArrayList<FileModel> = ArrayList()

//                val externalStorageUri = if (Build.MANUFACTURER.equals("Oppo", ignoreCase = true)) {
//                    val externalStorage = Environment.getExternalStorageDirectory()
//                    Uri.fromFile(File(externalStorage, "Android/data/com/cache"))
//                } else {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
//                    } else {
//                        MediaStore.Files.getContentUri("external")
//                    }
//                }

                val externalStorageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Files.getContentUri("external")
                }

                val projection = arrayOf(
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.DATE_TAKEN,
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.RELATIVE_PATH,
                    MediaStore.Files.FileColumns.DATA
                )


                val keywordsForFile = listOf(
                    ".globalTrash", ".trash", ".Trash", "Trash", "trash", "recycle",
                    "bin", "Recycle", "RecycleBin", ".trashed", ".Trashed", ".noMedia", "nomedia",
                    "noMedia", "NOMEDIA", ".nomedia", "cache"
                )

                val selectionBuilder = StringBuilder()
                val selectionArgs = ArrayList<String>()
                for ((index, keyword) in keywordsForFile.withIndex()) {
                    if (index > 0) {
                        selectionBuilder.append(" OR ")
                    }
                    selectionBuilder.append("${MediaStore.Files.FileColumns.RELATIVE_PATH} GLOB ?")
                    selectionArgs.add("*$keyword*")
                }
                val imageSortOrder = "${MediaStore.Files.FileColumns.DATE_TAKEN} DESC"

                context.contentResolver.query(
                    externalStorageUri,
                    projection,
                    selectionBuilder.toString(),
                    selectionArgs.toTypedArray(),
                    imageSortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                    val dateColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                    val fileNameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)

                    while (cursor.moveToNext()) {
                        val fileId = cursor.getLong(idColumn)
                        val size = cursor.getString(sizeColumn)
                        val date = cursor.getLong(dateColumn)
                        val fileName = cursor.getString(fileNameColumn)
                        val fileUri = Uri.withAppendedPath(externalStorageUri, fileId.toString())
                        val documentFile = DocumentFile.fromSingleUri(context, fileUri)
                        var imageUri: Uri? = null
                        val videoUri: Uri? = null
                        val audioUri: Uri? = null
                        if (documentFile != null) {
                            if (documentFile.uri.toString() != "") {
                                imageUri = documentFile.uri
                            }
                        }

//                        val imageUri =
//                            if (documentFile?.let { isImageFile(it) } == true) documentFile.uri else null
                        val fileSize = if (!size.isNullOrEmpty()) {
                            (size.toLong() / 1024).toString() + " kb"
                        } else {
                            "0 mb"
                        }


                        val separator = "."
                        val lastWord = lastWordAfterSeparator(fileName, separator)
                        val nameBeforeDot = getFirstWordBeforeDot(fileName)

                        if (documentFile != null && documentFile.exists() && documentFile.type != null) {
                            Log.d("lastWord", "${lastWord}")
                            if (documentFile.type?.contains("application") == true) {
                                fileList.add(
                                    FileModel(
                                        fileName = nameBeforeDot,
                                        fileExtension = lastWord,
                                        location = FileLocation.NO_MEDIA,
                                        isSelected = false,
                                        creationDate = date,
                                        fileSize = fileSize,
                                        isDeleted = true,
                                        imageUri = imageUri,
                                        videoUri = videoUri,
                                        audioUri = audioUri,
                                        isRewarded = false
                                    )
                                )
                            } else if (lastWord.contains("mp3") || lastWord.contains("mpeg") || lastWord.contains(
                                    "audio"
                                )
                            ) {
                                audioList.add(
                                    FileModel(
                                        fileName = nameBeforeDot,
                                        fileExtension = lastWord,
                                        location = FileLocation.NO_MEDIA,
                                        isSelected = false,
                                        creationDate = date,
                                        fileSize = fileSize,
                                        isDeleted = true,
                                        imageUri = imageUri,
                                        audioUri = audioUri,
                                        isRewarded = false
                                    )
                                )
                            } else if (lastWord.contains("mp4") || lastWord.contains("wav")) {

                                // todo LISTEDE IMAGEURI VARSA EKLEME YOKSA EKLE HEPSINE
                                if (imageUri != null) {
                                    videoList.add(
                                        FileModel(
                                            fileName = nameBeforeDot,
                                            fileExtension = lastWord,
                                            location = FileLocation.NO_MEDIA,
                                            isSelected = false,
                                            creationDate = date,
                                            fileSize = fileSize,
                                            isDeleted = true,
                                            imageUri = imageUri,
                                            videoUri = videoUri,
                                            isRewarded = false
                                        )
                                    )
                                }

                            } else if (lastWord.contains("png") ||
                                lastWord.contains("jpg") ||
                                lastWord.contains("webp") ||
                                lastWord.contains("jpeg") ||
                                lastWord.contains("gif")
                            ) {
                                if (imageUri != null) {
                                    imageList.add(
                                        FileModel(
                                            fileName = nameBeforeDot,
                                            fileExtension = lastWord,
                                            location = FileLocation.NO_MEDIA,
                                            isSelected = false,
                                            creationDate = date,
                                            fileSize = fileSize,
                                            isDeleted = true,
                                            imageUri = imageUri,
                                            isRewarded = false
                                        )
                                    )
                                }
                            }
                        }
                    }

                    trashedFiles = MainFileModel(
                        fileList,
                        imageList,
                        audioList,
                        videoList
                    )

                }
                Resource.success(trashedFiles)
            } catch (e: Exception) {
                Resource.error("No data! ${e.localizedMessage}", null)
            }
        }

    private fun lastWordAfterSeparator(word: String, separator: String): String {
        val index = word.lastIndexOf(separator)

        return if (index != -1) {
            val words = word.substring(index + separator.length).trim().split("\\s+".toRegex())
            if (words.isNotEmpty()) words.last() else ""
        } else {
            ""
        }
    }

    private fun getFirstWordBeforeDot(word: String): String {
        val firstDotIndex = word.indexOf('.')

        return if (firstDotIndex != -1) {
            word.substring(0, firstDotIndex)
        } else {
            ""
        }
    }
}