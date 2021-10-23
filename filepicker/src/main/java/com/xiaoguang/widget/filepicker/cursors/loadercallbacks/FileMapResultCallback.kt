package com.xiaoguang.widget.filepicker.cursors.loadercallbacks

import com.xiaoguang.widget.filepicker.models.Document
import com.xiaoguang.widget.filepicker.models.FileType

/**
 * Created by gabriel on 10/2/17.
 */

interface FileMapResultCallback {
    fun onResultCallback(files: Map<FileType, List<Document>>)
}

