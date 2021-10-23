package com.xiaoguang.widget.filepicker.utils

import android.content.ContentResolver
import android.os.Bundle
import com.xiaoguang.widget.filepicker.cursors.DocScannerTask
import com.xiaoguang.widget.filepicker.cursors.PhotoScannerTask
import com.xiaoguang.widget.filepicker.cursors.loadercallbacks.FileMapResultCallback
import com.xiaoguang.widget.filepicker.cursors.loadercallbacks.FileResultCallback
import com.xiaoguang.widget.filepicker.models.Document
import com.xiaoguang.widget.filepicker.models.FileType
import com.xiaoguang.widget.filepicker.models.PhotoDirectory
import java.util.*

object MediaStoreHelper {

    fun getDirs(contentResolver: ContentResolver, args: Bundle, resultCallback: FileResultCallback<PhotoDirectory>) {
        PhotoScannerTask(contentResolver,args,resultCallback).execute()
    }

    fun getDocs(contentResolver: ContentResolver,
                fileTypes: List<FileType>,
                comparator: Comparator<Document>?,
                fileResultCallback: FileMapResultCallback) {
        DocScannerTask(contentResolver, fileTypes, comparator, fileResultCallback).execute()
    }
}