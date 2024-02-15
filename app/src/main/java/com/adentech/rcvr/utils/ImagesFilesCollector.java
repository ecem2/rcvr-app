package com.adentech.rcvr.utils;


import androidx.lifecycle.MutableLiveData;

import com.adentech.rcvr.model.FileModel;
import com.adentech.rcvr.model.MainFileModel;

import java.util.ArrayList;

public class ImagesFilesCollector {
    public static ArrayList<FileModel> foundFilesList;
    public static ArrayList<FileModel> foundImagesList;
    public static ArrayList<FileModel> foundAudioList;
    public static ArrayList<FileModel> foundVideoList;

    public static MutableLiveData<ArrayList<MainFileModel>> liveDataAllFiles = new MutableLiveData<>();

    static {
        foundFilesList = new ArrayList();
        foundImagesList = new ArrayList();
        foundAudioList = new ArrayList();
        foundVideoList = new ArrayList();
    }
}
