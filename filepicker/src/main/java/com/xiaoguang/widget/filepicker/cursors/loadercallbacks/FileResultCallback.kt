package com.xiaoguang.widget.filepicker.cursors.loadercallbacks

interface FileResultCallback<T> {
    fun onResultCallback(files: List<T>)
}