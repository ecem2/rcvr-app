package com.adentech.rcvr.scan


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adentech.rcvr.common.Resource
import com.adentech.rcvr.viewmodel.BaseViewModel
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.model.MainFileModel
import com.adentech.rcvr.preferences.Preferences
import com.adentech.rcvr.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: ImageRepository,
    val preferences: Preferences
) : BaseViewModel() {

    private val _allTrashedFilesLists: MutableLiveData<Resource<MainFileModel>> = MutableLiveData()
    val allTrashedFilesList: LiveData<Resource<MainFileModel>> get() = _allTrashedFilesLists

    private val _imageList = MutableLiveData<Resource<ArrayList<FileModel>>>()
    val imageList: LiveData<Resource<ArrayList<FileModel>>> = _imageList
    private val _videoList = MutableLiveData<Resource<ArrayList<FileModel>>>()
    val videoList: LiveData<Resource<ArrayList<FileModel>>> = _videoList


    init {
        _allTrashedFilesLists.postValue(Resource.loading(null))
        _imageList.postValue(Resource.loading(null))
        _videoList.postValue(Resource.loading(null))
    }

    fun getAllTrashedFiles() = viewModelScope.launch {
        val trashedFiles = repository.getAllTrashFiles()
        if (trashedFiles.message != null) {
            _allTrashedFilesLists.postValue(Resource.error(trashedFiles.message.toString(), null))
        } else {
            _allTrashedFilesLists.postValue(trashedFiles)
        }
    }

    fun getAllGalleryImages() = viewModelScope.launch {
        val images = repository.getGalleryImages()
        if (images.data.isNullOrEmpty() || images.data.size == 0) {
            _imageList.postValue(Resource.error(images.message.toString(), null))
        } else {
            _imageList.postValue(images)
        }
    }
    fun getAllGalleryVideos() = viewModelScope.launch {
        val videos = repository.getGalleryVideos()
        if (videos.data.isNullOrEmpty() || videos.data.size == 0) {
            _videoList.postValue(Resource.error(videos.message.toString(), null))
        } else {
            _videoList.postValue(videos)
        }
    }

    fun deleteImage(file: FileModel) = viewModelScope.launch {
        val currentList = _imageList.value?.data
        if (currentList != null) {
            currentList.remove(file)
            _imageList.postValue(Resource.success(currentList))
            _imageList.postValue(repository.getGalleryImages())
        }
    }
    fun deleteVideo(file: FileModel) = viewModelScope.launch {
        val currentList = _videoList.value?.data
        if (currentList != null) {
            currentList.remove(file)
            _videoList.postValue(Resource.success(currentList))
            _videoList.postValue(repository.getGalleryVideos())
        }
    }

}