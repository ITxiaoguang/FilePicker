package com.xiaoguang.widget.filepicker.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import androidx.core.os.EnvironmentCompat
import com.xiaoguang.widget.filepicker.FilePickerConst
import java.io.File

/**
 * Created by droidNinja on 08/03/17.
 */

object FileUtils {

    fun getFileType(path: String): FilePickerConst.FILE_TYPE {
        val fileExtension = FilePickerUtils.getFileExtension(File(path))
        if (TextUtils.isEmpty(fileExtension))
            return FilePickerConst.FILE_TYPE.UNKNOWN

        if (isExcelFile(path))
            return FilePickerConst.FILE_TYPE.EXCEL
        if (isDocFile(path))
            return FilePickerConst.FILE_TYPE.WORD
        if (isPPTFile(path))
            return FilePickerConst.FILE_TYPE.PPT
        if (isPDFFile(path))
            return FilePickerConst.FILE_TYPE.PDF
        return if (isTxtFile(path))
            FilePickerConst.FILE_TYPE.TXT
        else
            FilePickerConst.FILE_TYPE.UNKNOWN
    }

    fun isExcelFile(path: String): Boolean {
        val types = arrayOf("xls", "xlsx")
        return FilePickerUtils.contains(types, path)
    }

    fun isDocFile(path: String): Boolean {
        val types = arrayOf("doc", "docx", "dot", "dotx")
        return FilePickerUtils.contains(types, path)
    }

    fun isPPTFile(path: String): Boolean {
        val types = arrayOf("ppt", "pptx")
        return FilePickerUtils.contains(types, path)
    }

    fun isPDFFile(path: String): Boolean {
        val types = arrayOf("pdf")
        return FilePickerUtils.contains(types, path)
    }

    fun isTxtFile(path: String): Boolean {
        val types = arrayOf("txt")
        return FilePickerUtils.contains(types, path)
    }

    // 获取文件扩展名
    fun getExtensionName(filename: String?): String? {
        if (filename != null && filename.length > 0) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length - 1) {
                return filename.substring(dot + 1)
            }
        }
        return ""
    }

    /////////////  获取文件地址  ///////////////////

    const val FILE_CACHE_PATH = "filepicker-cache"
    var AGENTWEB_FILE_PATH: String? = null

    @TargetApi(19)
    fun getFileAbsolutePath(context: Activity?, fileUri: Uri?): String? {
        if (context == null || fileUri == null) {
            return null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                context,
                fileUri
            )
        ) {
            if (isExternalStorageDocument(fileUri)) {
                val docId = DocumentsContract.getDocumentId(fileUri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(fileUri)) {
                val id = DocumentsContract.getDocumentId(fileUri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(fileUri)) {
                val docId = DocumentsContract.getDocumentId(fileUri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            } else {
            }
        } // MediaStore (and general)
        else if (fileUri.authority.equals(
                context.packageName + ".AgentWebFileProvider",
                ignoreCase = true
            )
        ) {
            val path = fileUri.path
            val index = path!!.lastIndexOf("/")
            return getAgentWebFilePath(context) + File.separator + path.substring(
                index + 1,
                path.length
            )
        } else if ("content".equals(fileUri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(fileUri)) {
                fileUri.lastPathSegment
            } else getDataColumn(context, fileUri, null, null)
        } else if ("file".equals(fileUri.scheme, ignoreCase = true)) {
            return fileUri.path
        }
        return null
    }

    fun getAgentWebFilePath(context: Context): String? {
        if (!TextUtils.isEmpty(AGENTWEB_FILE_PATH)) {
            return AGENTWEB_FILE_PATH
        }
        val dir = getDiskExternalCacheDir(context)
        val mFile = File(dir, FILE_CACHE_PATH)
        try {
            if (!mFile.exists()) {
                mFile.mkdirs()
            }
        } catch (throwable: Throwable) {
        }
        return mFile.absolutePath.also { AGENTWEB_FILE_PATH = it }
    }

    fun getDiskExternalCacheDir(context: Context): String? {
        val mFile = context.externalCacheDir
        return if (Environment.MEDIA_MOUNTED == EnvironmentCompat.getStorageState(mFile!!)) {
            mFile.absolutePath
        } else null
    }

    fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

}
