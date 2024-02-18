package com.adentech.rcvr.data.repository

import com.adentech.rcvr.core.common.Resource
import com.adentech.rcvr.data.model.FileModel
import com.adentech.rcvr.data.model.MainFileModel

interface ImageRepository {

    suspend fun getGalleryImages(): Resource<ArrayList<FileModel>>

    suspend fun getGalleryVideos(): Resource<ArrayList<FileModel>>

    suspend fun getAllTrashFiles(): Resource<MainFileModel>

}