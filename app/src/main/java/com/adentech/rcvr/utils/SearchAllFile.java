package com.adentech.rcvr.utils;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import com.adentech.rcvr.data.model.FileLocation;
import com.adentech.rcvr.data.model.FileModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

public class SearchAllFile extends AsyncTask<Void, Integer, Void> {

    Context mainContext;
    ArrayList<String> phoneStorageVolums;
    Activity uiActivity;

    public SearchAllFile(ArrayList<String> volums, Activity activityh, Context context) {
        this.phoneStorageVolums = volums;
        this.uiActivity = activityh;
        this.mainContext = context;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    protected Void doInBackground(Void... params) {
        fetchGalleryImages(uiActivity);
        for (int i = 0; i < this.phoneStorageVolums.size(); i++) {
            searchFiles((String) this.phoneStorageVolums.get(i), false);
        }

        return null;
    }


    protected void onPostExecute(Void result) {


   /*     for (int i = 0; i < files.length; i++) {
            String[] breakedPath = files[i].getAbsolutePath().split("/");
            String parentName = breakedPath[breakedPath.length - 2];
            if (ImagesFilesCollector.organizedByFolder.containsKey("parentName")) {
                ((ArrayList) ImagesFilesCollector.organizedByFolder.get("parentName")).add(files[i]);
            } else {
                ArrayList newArray = new ArrayList();
                newArray.add(files[i]);
                ImagesFilesCollector.hashMapKeys.add("parentName");
                ImagesFilesCollector.organizedByFolder.put("parentName", newArray);
            }
        }*/
    }

    boolean isImageEmpty(Bitmap bmp) {
        int i1 = bmp.getPixel(0, 0);
        int i2 = bmp.getPixel(0, bmp.getHeight() - 1);
        int i3 = bmp.getPixel(bmp.getWidth() - 1, 0);
        int i4 = bmp.getPixel(bmp.getWidth() - 1, bmp.getHeight() - 1);
        if (i1 == i2 && i1 == i3 && i1 == i4) {
            return false;
        }
        return true;
    }

    File[] filesSorterByDate(File[] files) {
        ArrayList<File> tempArray = new ArrayList();
        for (File add : files) {
            tempArray.add(add);
        }
        try {
            Collections.sort(tempArray, new FilesComparator());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    public void searchFiles(String storageDirectory, boolean isHidden) {
        File[] thisFolderFiles = new File(storageDirectory).listFiles();
        if ((storageDirectory.contains("PhotoRecovery_CD") && !storageDirectory.contains("nomedia")))
            return;
        if (thisFolderFiles != null) {
            File nomedia = new File(new StringBuilder(String.valueOf(storageDirectory)).append("/.nomedia").toString());
            if (nomedia != null && nomedia.exists()) {
                isHidden = true;
            }

            for (File tempFile : filesSorterByDate(thisFolderFiles)) {
                if (tempFile.isFile()) {
                    String name = tempFile.getName();
                    String fileType = getFileType(tempFile);
                    if ((name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")) && ((Pattern.compile("(.*)((\\.(jpg||jpeg||png))$)", 2).matcher(name).matches() || isHidden) && !ImagesFilesCollector.foundImagesList.contains(tempFile))) {
                        Log.d("SearchAllFile", "Found Image: " + name + " | Path: " + tempFile.getPath());

                        ImagesFilesCollector.foundImagesList.add(new FileModel(
                                tempFile.getName(),
                                tempFile.getPath(),
                                FileLocation.NO_MEDIA,
                                false,
                                tempFile.lastModified(),
                                String.valueOf(tempFile.length() / 1024),
                                true,
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                false,
                                false
                        ));
                    } else if (fileType.contains("doc") || fileType.contains("pdf")
                            || fileType.contains("xlsx") || fileType.contains("zip")
                            || fileType.contains("docx") || fileType.contains("rar")
                            || fileType.contains("apk")
                    ) {
                        String separator = ".";
                        String lastWord = lastWordAfterSeparator(name, separator);
                        String nameBeforeDot = getFirstWordBeforeDot(name);

                        ImagesFilesCollector.foundFilesList.add(new FileModel(
                                nameBeforeDot,
                                lastWord,
                                FileLocation.NO_MEDIA,
                                false,
                                tempFile.lastModified(),
                                String.valueOf(tempFile.length() / 1024),
                                true,
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                false,
                                false
                        ));
                    } else if (fileType.contains("mp4") || fileType.contains("wav")) {
                        Log.d("SearchAllFile", "Found Video: " + name + " | Path: " + tempFile.getPath());

                        String nameBeforeDot = getFirstWordBeforeDot(name);
                        ImagesFilesCollector.foundVideoList.add(new FileModel(
                                nameBeforeDot,
                                fileType,
                                FileLocation.NO_MEDIA,
                                false,
                                tempFile.lastModified(),
                                String.valueOf(tempFile.length() / 1024),
                                true,
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                false,
                                false
                        ));
                    } else if (fileType.contains("mp3") || fileType.contains("wav")) {
                        String nameBeforeDot = getFirstWordBeforeDot(name);
                        boolean isAudioFile = fileType.contains("mp3");
                        ImagesFilesCollector.foundAudioList.add(new FileModel(
                                nameBeforeDot,
                                fileType,
                                FileLocation.NO_MEDIA,
                                false,
                                tempFile.lastModified(),
                                String.valueOf(tempFile.length() / 1024),
                                true,
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                Uri.fromFile(tempFile),
                                false,
                                isAudioFile
                        ));
                    }
                }
                if (tempFile.isDirectory()) {
                    if (Pattern.compile("(^\\.)(.*)", 2).matcher(tempFile.getName()).matches() || isHidden) {
                        searchFiles(tempFile.getAbsolutePath(), true);
                    } else {
                        searchFiles(tempFile.getAbsolutePath(), isHidden);
                    }
                }
            }
        }
    }

    public static String getFileType(File file) {
        // Using the getName() method to get the file name and then extracting the extension
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        } else {
            return "File not found";
        }
    }

    private void fetchGalleryImages(Activity context) {
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;//order data by date

        Cursor imagecursor = context.managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy + " DESC");

        for (int i = 0; i < imagecursor.getCount(); i++) {
            imagecursor.moveToPosition(i);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);
            File f = new File(imagecursor.getString(dataColumnIndex));
            if (f.exists()) {
                //ImagesFilesCollector.galleryFiles.add(new FileModel(f, FileLocation.GALLERY));
            }
        }
    }

    private static String lastWordAfterSeparator(String word, String separator) {
        int index = word.lastIndexOf(separator);

        if (index != -1) {
            String[] words = word.substring(index + separator.length()).trim().split("\\s+");
            if (words.length > 0) {
                return words[words.length - 1];
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    private static String getFirstWordBeforeDot(String word) {
        int firstDotIndex = word.indexOf('.');

        if (firstDotIndex != -1) {
            return word.substring(0, firstDotIndex);
        } else {
            return "";
        }
    }
}