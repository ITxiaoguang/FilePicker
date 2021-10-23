package com.xiaoguang.widget.filepicker.fragments

import android.content.ContentResolver
import android.database.Cursor
import android.os.AsyncTask
import android.provider.BaseColumns
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import com.xiaoguang.widget.filepicker.cursors.loadercallbacks.FileResultCallback
import com.xiaoguang.widget.filepicker.models.Document
import com.xiaoguang.widget.filepicker.models.FileType
import java.io.File
import java.util.*

class FileScannerTask(val contentResolver: ContentResolver,
                      private val fileType: FileType?,
                      private val resultCallback: FileResultCallback<Document>?) :
        AsyncTask<Void?, Void?, List<Document>>() {

    val DOC_PROJECTION = arrayOf(MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.TITLE)

    override fun doInBackground(vararg voids: Void?): List<Document> {
        var documents: List<Document> = ArrayList()
        if (fileType == null) {
            return documents
        }
        var selection = ""

        for (extension in fileType.extensions) {
            var mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (!TextUtils.isEmpty(mimeType)) {
                if (TextUtils.isEmpty(selection)) {
                    selection += MediaStore.Files.FileColumns.MIME_TYPE + " LIKE '" + mimeType + "'"
                } else {
                    selection += " OR " + MediaStore.Files.FileColumns.MIME_TYPE + " LIKE '" + mimeType + "'"
                }
            } else {
                //Log.e("test", "未匹配：" + extension)
            }
        }

        if (!TextUtils.isEmpty(selection)) {
            Log.e("test", "selection：$selection")
        }

        val cursor = contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                DOC_PROJECTION,
                selection,
                null,
                MediaStore.Files.FileColumns.DATE_ADDED + " DESC")
        if (cursor != null) {
            documents = getDocumentFromCursor(cursor)
            cursor.close()
        }
        return documents
    }

    override fun onPostExecute(documents: List<Document>) {
        resultCallback?.onResultCallback(documents)
    }

    private fun getDocumentFromCursor(data: Cursor): List<Document> {
        val documents: MutableList<Document> = ArrayList()
        while (data.moveToNext()) {
            val imageId = data.getInt(data.getColumnIndexOrThrow(BaseColumns._ID))
            val path = data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            val title = data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE))
            if (path != null) {
                val file = File(path)
                if (fileType != null && !file.isDirectory && file.exists()) {
                    val document = Document(imageId, title, path)
                    document.fileType = fileType
                    val mimeType = data
                            .getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
                    if (mimeType != null && !TextUtils.isEmpty(mimeType)) {
                        document.mimeType = mimeType
                    } else {
                        document.mimeType = ""
                    }
                    document.size = data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE))
                    if (!documents.contains(document)) {
                        documents.add(document)
                    }
                }
            }
        }
        return documents
    }
}