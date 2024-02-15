package com.adentech.rcvr.utils


import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Scanner

class FilesFetcher(val context: Context) {

    fun getStorageVolumes(): ArrayList<String> {
        val volumesList: ArrayList<String> = ArrayList()
        val removableStorage: String? = getRemovableVolumeFromServices()
        val storageList: ArrayList<String> = getVolumesFromFileSystem()
        val externalStorage = Environment.getExternalStorageDirectory().path
        val internalStorage = Environment.getDataDirectory().absolutePath

        if (removableStorage != null) {
            volumesList.add(removableStorage)
        }
        if (!(internalStorage == null || volumesList.contains(internalStorage))) {
            volumesList.add(internalStorage)
        }
        if (!(externalStorage == null || volumesList.contains(externalStorage))) {
            volumesList.add(externalStorage)
        }
        if (storageList.isNotEmpty()) {
            for (i in storageList.indices) {
                val tempString = storageList[i]
                if (!volumesList.contains(tempString)) {

                    Log.d("ecemmm", "$storageList")
                    volumesList.add(tempString)
                }
            }
        }else{
            Log.d("ecemmm", "aaaaaaaaaa")

        }
        return volumesList
    }

    fun getRemovableVolumeFromServices(): String? {
        val mStorageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        try {
            val result = mStorageManager.javaClass.getMethod("getVolumeList", *arrayOfNulls(0))
                .invoke(mStorageManager, *arrayOfNulls(0)) as Array<Any>
            val length = java.lang.reflect.Array.getLength(result)
            for (i in 0 until length) {
                val storageVolumeElement = java.lang.reflect.Array.get(result, i)
                val getpathmethod =
                    storageVolumeElement.javaClass.getDeclaredMethod("getPath", *arrayOfNulls(0))
                val path = getpathmethod.invoke(storageVolumeElement, *arrayOfNulls(0)) as String
                if (storageVolumeElement.javaClass.getDeclaredMethod(
                        "isRemovable",
                        *arrayOfNulls<Class<*>>(0)
                    ).invoke(
                        storageVolumeElement,
                        *arrayOfNulls<Any>(0)
                    ) as Boolean && mountsChecker(path)
                ) {
                    return path
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun getVolumesFromFileSystem(): ArrayList<String> {
        val mountsFileHandle = File("/system/etc/vold.fstab")
        val mountsArray: HashSet<String> = HashSet()
        return try {
            val mountsReader = FileInputStream(mountsFileHandle)
            val mountsScanner = Scanner(mountsReader)
            while (mountsScanner.hasNext()) {
                val nextLine = mountsScanner.nextLine()
                if (nextLine.startsWith("dev_mount") || nextLine.startsWith("fuse_mount")) {
                    mountsArray.add(
                        nextLine.replace("\t", " ").split(" ".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()[2])
                }
            }
            mountsScanner.close()
            mountsReader.close()
            if (!Environment.isExternalStorageRemovable()) {
                mountsArray.remove(Environment.getExternalStorageDirectory().path)
            }
            val finalMountsContainer: ArrayList<String> = ArrayList()
            val iterator: Iterator<*> = mountsArray.iterator()
            while (iterator.hasNext()) {
                val nextMount = iterator.next() as String
                if (mountsChecker(nextMount)) {
                    finalMountsContainer.add(nextMount)
                }
            }
            finalMountsContainer
        } catch (e: java.lang.Exception) {
            Log.d("ecemmmm","qwqwrqwreqre ${e.localizedMessage}")
            e.printStackTrace()
            arrayListOf()
        }
    }

    fun mountsChecker(volumePath: String): Boolean {
        return try {
            val mountsReader = FileInputStream(File("/proc/mounts"))
            val mountsScanner = Scanner(mountsReader)
            while (mountsScanner.hasNextLine()) {
                if (mountsScanner.nextLine().contains(volumePath)) {
                    try {
                        mountsScanner.close()
                        mountsReader.close()
                    } catch (e: IOException) {
                    }
                    return true
                }
            }
            false
        } catch (e2: FileNotFoundException) {
            e2.printStackTrace()
            false
        }
    }
}

//public class FilesFetcher {
//    public static ArrayList<String> getStorageVolums(Context context) {
//        ArrayList<String> VolumsList = new ArrayList();
//        String removableStorage = getRemovableVolumFromServices(context);
//        ArrayList<String> storagelist = getVolumsFromFileSystem();
//        if (storagelist != null) {
//            for (String num : storagelist) {
//            }
//        }
//
//        String externalStorage = Environment.getExternalStorageDirectory().getPath();
//        String internalStorage = Environment.getDataDirectory().getAbsolutePath();
//        if (removableStorage != null) {
//            VolumsList.add(removableStorage);
//        }
//        if (!(internalStorage == null || VolumsList.contains(internalStorage))) {
//            VolumsList.add(internalStorage);
//        }
//        if (!(externalStorage == null || VolumsList.contains(externalStorage))) {
//            VolumsList.add(externalStorage);
//        }
//        if (storagelist != null) {
//            for (int i = 0; i < storagelist.size(); i++) {
//                String tempString = (String) storagelist.get(i);
//                if (!VolumsList.contains(tempString)) {
//                    VolumsList.add(tempString);
//                }
//            }
//        }
//        if (VolumsList.isEmpty()) {
//            return null;
//        }
//        return VolumsList;
//    }
//
//    public static String getRemovableVolumFromServices(Context context) {
//        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
//        try {
//            Object[] result = (Object[]) mStorageManager.getClass().getMethod("getVolumeList", new Class[0]).invoke(mStorageManager, new Object[0]);
//            int length = Array.getLength(result);
//            for (int i = 0; i < length; i++) {
//                Object storageVolumeElement = Array.get(result, i);
//                Method getpathmethod = storageVolumeElement.getClass().getDeclaredMethod("getPath", new Class[0]);
//                String path = (String) getpathmethod.invoke(storageVolumeElement, new Object[0]);
//                if (((Boolean) storageVolumeElement.getClass().getDeclaredMethod(
//                        "isRemovable",
//                        new Class[0]
//                ).invoke(
//                        storageVolumeElement,
//                        new Object[0])
//                ).booleanValue() && mountsChecker(path)) {
//                    return path;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//
//    public static ArrayList<String> getVolumsFromFileSystem() {
//        File mountsFileHandle = new File("/system/etc/vold.fstab");
//        HashSet<String> mountsArray = new HashSet();
//        try {
//            FileInputStream mountsReader = new FileInputStream(mountsFileHandle);
//            Scanner mountsScanner = new Scanner(mountsReader);
//            while (mountsScanner.hasNext()) {
//                String nextLine = mountsScanner.nextLine();
//                if (nextLine.startsWith("dev_mount") || nextLine.startsWith("fuse_mount")) {
//                    mountsArray.add(nextLine.replace("\t", " ").split(" ")[2]);
//                }
//            }
//            mountsScanner.close();
//            mountsReader.close();
//            if (!Environment.isExternalStorageRemovable()) {
//                mountsArray.remove(Environment.getExternalStorageDirectory().getPath());
//            }
//            ArrayList<String> finalMountsContainer = new ArrayList();
//            Iterator iterator = mountsArray.iterator();
//            while (iterator.hasNext()) {
//                String nextMount = (String) iterator.next();
//                if (mountsChecker(nextMount)) {
//                    finalMountsContainer.add(nextMount);
//                }
//            }
//            if (finalMountsContainer.isEmpty()) {
//                return null;
//            }
//            return finalMountsContainer;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public static boolean mountsChecker(String volumPath) {
//        try {
//            FileInputStream mountsReader = new FileInputStream(new File("/proc/mounts"));
//            Scanner mountsScanner = new Scanner(mountsReader);
//            while (mountsScanner.hasNextLine()) {
//                if (mountsScanner.nextLine().contains(volumPath)) {
//                    try {
//                        mountsScanner.close();
//                        mountsReader.close();
//                    } catch (IOException e) {
//                    }
//                    return true;
//                }
//            }
//            return false;
//        } catch (FileNotFoundException e2) {
//            e2.printStackTrace();
//            return false;
//        }
//    }
//}