package com.adentech.rcvr.utils

import java.io.File

class FilesComparator : Comparator<File> {
    override fun compare(file1: File, file2: File): Int {
        if (file1.lastModified() > file2.lastModified()) {
            return 1
        }
        return if (file1.lastModified() < file2.lastModified()) {
            -1
        } else 0
    }
}