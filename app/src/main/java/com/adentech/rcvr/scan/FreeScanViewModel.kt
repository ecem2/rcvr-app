package com.adentech.rcvr.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adentech.rcvr.common.Resource
import com.adentech.rcvr.viewmodel.BaseViewModel
import com.adentech.rcvr.model.FileModel
import com.adentech.rcvr.preferences.Preferences
import com.adentech.rcvr.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FreeScanViewModel @Inject constructor(
    val preferences: Preferences,
    private val repository: ImageRepository
): BaseViewModel() {

    var isProgressDone: Boolean = false

    private val _imageList = MutableLiveData<Resource<ArrayList<FileModel>>>()
    val imageList: LiveData<Resource<ArrayList<FileModel>>> = _imageList

    init {
        _imageList.postValue(Resource.loading(null))
    }

    fun getAllGalleryImages() = viewModelScope.launch {
        val images = repository.getGalleryImages()
        if (images.data.isNullOrEmpty() || images.data.size == 0) {
            _imageList.postValue(Resource.error(images.message.toString(), null))
        } else {
            _imageList.postValue(images)
        }
    }
}