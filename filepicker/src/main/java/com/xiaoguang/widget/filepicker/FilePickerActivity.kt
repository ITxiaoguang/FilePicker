package com.xiaoguang.widget.filepicker

//import com.netease.nim.uikit.common.ToastHelper
//import com.netease.nim.uikit.common.util.file.AgentWebUtils
//import com.netease.nim.uikit.common.util.file.FileUtil
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.xiaoguang.widget.filepicker.fragments.DocFragment
import com.xiaoguang.widget.filepicker.fragments.DocPickerFragment
import com.xiaoguang.widget.filepicker.fragments.MediaPickerFragment
import com.xiaoguang.widget.filepicker.fragments.PhotoPickerFragmentListener
import com.xiaoguang.widget.filepicker.utils.FileUtils
import com.xiaoguang.widget.filepicker.utils.FragmentUtil
import com.xiaoguang.widget.filepicker.utils.ScreenUtils
import java.util.*


class FilePickerActivity : BaseFilePickerActivity(), PhotoPickerFragmentListener,
    DocFragment.DocFragmentListener, DocPickerFragment.DocPickerFragmentListener,
    MediaPickerFragment.MediaPickerFragmentListener {
    private var type: Int = 0

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_file_picker)
    }

    override fun initView() {
        val intent = intent
        if (intent != null) {
            var selectedPaths: ArrayList<String>? =
                intent.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA)
            type =
                intent.getIntExtra(FilePickerConst.EXTRA_PICKER_TYPE, FilePickerConst.MEDIA_PICKER)

            if (selectedPaths != null) {

                if (PickerManager.getMaxCount() == 1) {
                    selectedPaths.clear()
                }

                PickerManager.clearSelections()
//                if (type == FilePickerConst.MEDIA_PICKER) {
//                    PickerManager.add(selectedPaths, FilePickerConst.FILE_TYPE_MEDIA)
//                } else {
//                    PickerManager.add(selectedPaths, FilePickerConst.FILE_TYPE_DOCUMENT)
//                }
            }

            setToolbarTitle(PickerManager.currentCount)
            openSpecificFragment(type)

//            val cvTryIt = findView<CardView>(R.id.cv_try_it)
            val cvTryIt = findViewById<CardView>(R.id.cv_try_it)
            cvTryIt.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                this.startActivityForResult(
                    Intent.createChooser(intent, "选择文件"),
                    FilePickerConst.REQUEST_CODE_ALL_TYPE
                )
            }
        }
    }

    private fun setToolbarTitle(count: Int) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            val maxCount = PickerManager.getMaxCount()
            if (maxCount == -1 && count > 0) {
                actionBar.title = String.format(getString(R.string.attachments_num), count)
            } else if (maxCount > 0 && count > 0) {
                actionBar.title =
                    String.format(getString(R.string.attachments_title_text), count, maxCount)
            } else if (!TextUtils.isEmpty(PickerManager.title)) {
                actionBar.title = PickerManager.title
            } else {
                if (type == FilePickerConst.MEDIA_PICKER) {
                    actionBar.setTitle(R.string.select_photo_text)
                } else {
                    actionBar.setTitle(R.string.select_doc_text)
                }
            }
        }
    }

    private fun openSpecificFragment(type: Int) {
        if (type == FilePickerConst.MEDIA_PICKER) {
            val photoFragment = MediaPickerFragment.newInstance()
            FragmentUtil.replaceFragment(this, R.id.container, photoFragment)
        } else {
            if (PickerManager.isDocSupport) PickerManager.addDocTypes()

            val photoFragment = DocPickerFragment.newInstance()
            FragmentUtil.replaceFragment(this, R.id.container, photoFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.picker_menu, menu)
        val menuItem = menu.findItem(R.id.action_done)
        if (menuItem != null) {
            menuItem.isVisible = PickerManager.getMaxCount() != 1
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val i = item.itemId
        if (i == R.id.action_done) {
            ScreenUtils.showKeyboard(this, false);
            returnData(PickerManager.selectedFiles)
            return true
        } else if (i == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        PickerManager.reset()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FilePickerConst.REQUEST_CODE_MEDIA_DETAIL ->
                if (resultCode == Activity.RESULT_OK) {
                    returnData(PickerManager.selectedFiles)
                } else {
                    setToolbarTitle(PickerManager.currentCount)
                }
            FilePickerConst.REQUEST_CODE_ALL_TYPE -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val paths: ArrayList<String> = ArrayList(1)

                        val intent = Intent()
                        val path = FileUtils.getFileAbsolutePath(this, data!!.data) ?: return
                        val fileType = FileUtils.getExtensionName(path)!!.toLowerCase()

                        val fileTypes = PickerManager.getFileTypes()
                        var hasType = false
                        for (f in fileTypes) {
                            if (f.extensions.contains(fileType)) {
                                hasType = true
                                break
                            }
                        }
                        if (!hasType) {
                            if (PickerManager.showPic) {//开启图片选择
                                val pics = arrayOfNulls<String>(6)
                                pics[0] = "jpg"
                                pics[1] = "jpeg"
                                pics[2] = "webp"
                                pics[3] = "gif"
                                pics[4] = "bmp"
                                pics[5] = "png"
                                if (pics.contains(fileType)) {
                                    hasType = true
                                }
                            }
                            if (PickerManager.showVideo) {//开启视频选择
                                val videos = arrayOfNulls<String>(5)
                                videos[0] = "mp4"
                                videos[1] = "3gp"
                                videos[2] = "mov"
                                videos[3] = "avi"
                                videos[4] = "mpg"
                                if (videos.contains(fileType)) {
                                    hasType = true
                                }
                            }
                        }

                        if (!hasType) {
                            Toast.makeText(this, R.string.not_support_file_form, Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                        paths.add(path)
                        intent.putStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS, paths)
                        setResult(RESULT_OK, intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    finish()
                }
            }
        }
    }

    private fun returnData(paths: ArrayList<String>) {
        val intent = Intent()
        if (type == FilePickerConst.MEDIA_PICKER) {
            intent.putStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA, paths)
        } else {
            intent.putStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS, paths)
        }

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onItemSelected() {
        val currentCount = PickerManager.currentCount
        setToolbarTitle(currentCount)

        if (PickerManager.getMaxCount() == 1 && currentCount == 1) {
            returnData(PickerManager.selectedFiles)
        }
    }

    companion object {

        private val TAG = FilePickerActivity::class.java.simpleName
    }
}
