package com.xiaoguang.widget.filepicker

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import com.xiaoguang.widget.filepicker.models.BaseFile
import com.xiaoguang.widget.filepicker.models.FileType
import com.xiaoguang.widget.filepicker.models.sort.SortingTypes
import java.io.File
import java.util.*

/**
 * Created by droidNinja on 29/07/16.
 */

object PickerManager {
    private var maxCount = FilePickerConst.DEFAULT_MAX_COUNT
    private var showImages = true
    var cameraDrawable = R.drawable.ic_camera
    var sortingType = SortingTypes.none

    val selectedPhotos: ArrayList<String> = ArrayList()
    val selectedFiles: ArrayList<String> = ArrayList()

    private val fileTypes: LinkedHashSet<FileType> = LinkedHashSet()

    var theme: Int = R.style.LibAppTheme

    var title: String? = null

    private var showVideos: Boolean = false

    var isShowGif: Boolean = false

    private var showSelectAll = false

    var isDocSupport = true
        get() = field

    var isEnableCamera = true

    var showPic = true

    var showVideo = true

    private const val VOLUME = "/storage/emulated/0/Android/data/"
    private const val EXTERNAL = "/storage/emulated/0/"

    /**
     * The preferred screen orientation this activity would like to run in.
     * From the {@link android.R.attr#screenOrientation} attribute, one of
     * {@link #SCREEN_ORIENTATION_UNSPECIFIED},
     * {@link #SCREEN_ORIENTATION_LANDSCAPE},
     * {@link #SCREEN_ORIENTATION_PORTRAIT},
     * {@link #SCREEN_ORIENTATION_USER},
     * {@link #SCREEN_ORIENTATION_BEHIND},
     * {@link #SCREEN_ORIENTATION_SENSOR},
     * {@link #SCREEN_ORIENTATION_NOSENSOR},
     * {@link #SCREEN_ORIENTATION_SENSOR_LANDSCAPE},
     * {@link #SCREEN_ORIENTATION_SENSOR_PORTRAIT},
     * {@link #SCREEN_ORIENTATION_REVERSE_LANDSCAPE},
     * {@link #SCREEN_ORIENTATION_REVERSE_PORTRAIT},
     * {@link #SCREEN_ORIENTATION_FULL_SENSOR},
     * {@link #SCREEN_ORIENTATION_USER_LANDSCAPE},
     * {@link #SCREEN_ORIENTATION_USER_PORTRAIT},
     * {@link #SCREEN_ORIENTATION_FULL_USER},
     * {@link #SCREEN_ORIENTATION_LOCKED},
     */
    var orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        get() = field

    var isShowFolderView = true

    var providerAuthorities: String? = null

    val currentCount: Int
        get() = selectedPhotos.size + selectedFiles.size

    fun setMaxCount(count: Int) {
        reset()
        this.maxCount = count
    }

    fun getMaxCount(): Int {
        return maxCount
    }

    //过广播的方式来对整个SDcard进行刷新
    fun refreshBroadcast(context: Context) {
        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        scanIntent.data = Uri.fromFile(File(Environment.getExternalStorageDirectory().path))//获得SD卡目录/mnt/sdcard（获取的是手机外置sd卡的路径）
        context.sendBroadcast(scanIntent)

        scanIntent.data = Uri.fromFile(File(Environment.getDataDirectory().path))//获得根目录/data 内部存储路径
        context.sendBroadcast(scanIntent)

        scanIntent.data = Uri.fromFile(File(Environment.getDownloadCacheDirectory().path))//获得缓存目录/cache
        context.sendBroadcast(scanIntent)

        scanIntent.data = Uri.fromFile(File(Environment.getRootDirectory().path))//获得系统目录/system
        context.sendBroadcast(scanIntent)
    }

    //扫描媒体文件重新更新数据库
    fun refreshBroadcast2(context: Context) {
        refreshBroadcast(context)
        val args = Bundle()
        args.putString(VOLUME, EXTERNAL)
        val startScan = Intent()
        startScan.putExtras(args)
        startScan.component = ComponentName("com.android.providers.media", "com.android.providers.media.MediaScannerService")
        context.startService(startScan)
    }

    fun add(path: String?, type: Int) {
        if (path != null && shouldAdd()) {
            if (!selectedPhotos.contains(path)) {
                selectedFiles.add(path)
            } else {
                return
            }
        }
    }

    fun add(paths: ArrayList<String>, type: Int) {
        for (index in paths.indices) {
            add(paths[index], type)
        }
    }

    fun remove(path: String, type: Int) {
        if (selectedFiles.contains(path)) {
            selectedFiles.remove(path)
        }
    }

    fun shouldAdd(): Boolean {
        return if (maxCount == -1) true else currentCount < maxCount
    }

    fun getSelectedFilePaths(files: ArrayList<BaseFile>): ArrayList<String> {
        val paths = ArrayList<String>()
        for (index in files.indices) {
            paths.add(files[index].path)
        }
        return paths
    }

    fun reset() {
        selectedFiles.clear()
        selectedPhotos.clear()
        fileTypes.clear()
        maxCount = -1
    }

    fun clearSelections() {
        selectedPhotos.clear()
        selectedFiles.clear()
    }

    fun deleteMedia(paths: ArrayList<String>) {
        selectedPhotos.removeAll(paths)
    }

    fun showVideo(): Boolean {
        return showVideos
    }

    fun setShowVideos(showVideos: Boolean) {
        this.showVideos = showVideos
    }

    fun showImages(): Boolean {
        return showImages
    }

    fun setShowImages(showImages: Boolean) {
        this.showImages = showImages
    }

    fun addFileType(fileType: FileType) {
        fileTypes.add(fileType)
    }

    fun addDocTypes() {
        val pdfs = arrayOf("pdf")
        fileTypes.add(FileType(FilePickerConst.PDF, pdfs, R.drawable.icon_file_pdf))

        val docs = arrayOf("doc", "docx", "dot", "dotx")
        fileTypes.add(FileType(FilePickerConst.DOC, docs, R.drawable.icon_file_doc))

        val ppts = arrayOf("ppt", "pptx")
        fileTypes.add(FileType(FilePickerConst.PPT, ppts, R.drawable.icon_file_ppt))

        val xlss = arrayOf("xls", "xlsx")
        fileTypes.add(FileType(FilePickerConst.XLS, xlss, R.drawable.icon_file_xls))

        val txts = arrayOf("txt")
        fileTypes.add(FileType(FilePickerConst.TXT, txts, R.drawable.icon_file_unknown))
    }

    fun getFileTypes(): ArrayList<FileType> {
        return ArrayList(fileTypes)
    }

    fun hasSelectAll(): Boolean {
        return maxCount == -1 && showSelectAll
    }

    fun enableSelectAll(showSelectAll: Boolean) {
        this.showSelectAll = showSelectAll
    }

}
