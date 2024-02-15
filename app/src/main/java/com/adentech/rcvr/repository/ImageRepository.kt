package com.adentech.rcvr.repository

import com.adentech.rcvr.common.Resource
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.model.MainFileModel

interface ImageRepository {

    suspend fun getGalleryImages(): Resource<ArrayList<FileModel>>

    suspend fun getGalleryVideos(): Resource<ArrayList<FileModel>>

    suspend fun getAllTrashFiles(): Resource<MainFileModel>

}