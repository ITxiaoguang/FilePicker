package com.xiaoguang.widget.filepicker.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

import com.xiaoguang.widget.filepicker.FilePickerConst
import com.xiaoguang.widget.filepicker.MediaDetailsActivity
import com.xiaoguang.widget.filepicker.PickerManager
import com.xiaoguang.widget.filepicker.adapters.FolderGridAdapter
import com.xiaoguang.widget.filepicker.cursors.loadercallbacks.FileResultCallback
import com.xiaoguang.widget.filepicker.models.PhotoDirectory
import com.xiaoguang.widget.filepicker.utils.AndroidLifecycleUtils
import com.xiaoguang.widget.filepicker.utils.GridSpacingItemDecoration
import com.xiaoguang.widget.filepicker.utils.ImageCaptureManager
import java.io.IOException
import com.xiaoguang.widget.filepicker.R

class MediaFolderPickerFragment : BaseFragment(), FolderGridAdapter.FolderGridAdapterListener {
    lateinit var recyclerView: RecyclerView

    lateinit var emptyView: TextView

    private var mListener: PhotoPickerFragmentListener? = null
    private var photoGridAdapter: FolderGridAdapter? = null
    private var imageCaptureManager: ImageCaptureManager? = null
    private lateinit var mGlideRequestManager: RequestManager
    private var fileType: Int = 0
    private var data: MutableList<PhotoDirectory>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PhotoPickerFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(
                    context?.toString() + " must implement PhotoPickerFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGlideRequestManager = Glide.with(this)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_media_folder_picker
    }

    override fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerview)
        emptyView = view.findViewById(R.id.empty_view)
        arguments?.let {
            fileType = it.getInt(BaseFragment.FILE_TYPE)
            activity?.let {
                imageCaptureManager = ImageCaptureManager(it)
            }
            val layoutManager = GridLayoutManager(activity, 2)

            val spanCount = 2 // 2 columns
            val spacing = 5 // 5px
            val includeEdge = false
            recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
            recyclerView.layoutManager = layoutManager
            recyclerView.itemAnimator = DefaultItemAnimator()

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // Log.d(">>> Picker >>>", "dy = " + dy);
                    if (Math.abs(dy) > SCROLL_THRESHOLD) {
                        mGlideRequestManager.pauseRequests()
                    } else {
                        resumeRequestsIfNotDestroyed()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        resumeRequestsIfNotDestroyed()
                    }
                }
            })
            getDataFromMedia()
        }
    }

    override fun lazyLoad() {

    }

    private fun getDataFromMedia() {
        val mediaStoreArgs = Bundle()
        mediaStoreArgs.putBoolean(FilePickerConst.EXTRA_SHOW_GIF,
                PickerManager.isShowGif)
        mediaStoreArgs.putInt(FilePickerConst.EXTRA_FILE_TYPE, fileType)

        context?.let {
            MediaStoreHelper.getDirs(it.contentResolver, mediaStoreArgs,
                    object : FileResultCallback<PhotoDirectory> {
                        override fun onResultCallback(files: List<PhotoDirectory>) {
                            if(isAdded) {
                                updateList(files.toMutableList())
                            }
                        }
                    })
        }
    }

    private fun updateList(dirs: MutableList<PhotoDirectory>) {
        view?.let {
            Log.i("updateList", "" + dirs.size)
            data = dirs
            if (dirs.isNotEmpty()) {
                emptyView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                emptyView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                return
            }

            val photoDirectory = PhotoDirectory()
            photoDirectory.bucketId = FilePickerConst.ALL_PHOTOS_BUCKET_ID

            if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
                photoDirectory.name = getString(R.string.all_videos)
            } else if (fileType == FilePickerConst.MEDIA_TYPE_IMAGE) {
                photoDirectory.name = getString(R.string.all_photos)
            } else {
                photoDirectory.name = getString(R.string.all_files)
            }

            if (dirs.size > 0 && dirs[0].medias.size > 0) {
                photoDirectory.dateAdded = dirs[0].dateAdded
                photoDirectory.coverPath = dirs[0].medias[0].path
            }

            for (i in dirs.indices) {
                photoDirectory.addPhotos(dirs[i].medias)
            }

            dirs.add(0, photoDirectory)

            if (photoGridAdapter == null) {
                context?.let {
                    photoGridAdapter = FolderGridAdapter(it, mGlideRequestManager, dirs, mutableListOf(), fileType == FilePickerConst.MEDIA_TYPE_IMAGE && PickerManager.isEnableCamera)
                    recyclerView.adapter = photoGridAdapter
                    photoGridAdapter?.setFolderGridAdapterListener(this)
                }
            } else {
                photoGridAdapter?.setData(dirs)
                photoGridAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onCameraClicked() {
        try {
            context?.let {
                val intent = imageCaptureManager?.dispatchTakePictureIntent()
                if (intent != null) {
                    startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO)
                } else {
                    Toast.makeText(it, R.string.camera_settings, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun onFolderClicked(photoDirectory: PhotoDirectory) {
        val intent = Intent(activity, MediaDetailsActivity::class.java)
        intent.putExtra(PhotoDirectory::class.java.simpleName, photoDirectory)
        intent.putExtra(FilePickerConst.EXTRA_FILE_TYPE, fileType)
        activity?.startActivityForResult(intent, FilePickerConst.REQUEST_CODE_MEDIA_DETAIL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ImageCaptureManager.REQUEST_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val imagePath = imageCaptureManager?.notifyMediaStoreDatabase()
                if (imagePath != null && PickerManager.getMaxCount() == 1) {
                    PickerManager.add(imagePath, FilePickerConst.FILE_TYPE_MEDIA)
                    mListener?.onItemSelected()
                } else {
                    Handler().postDelayed({ getDataFromMedia() }, 1000)
                }
            }
        }
    }

    private fun resumeRequestsIfNotDestroyed() {
        if (!AndroidLifecycleUtils.canLoadImage(this)) {
            return
        }

        mGlideRequestManager.resumeRequests()
    }

    companion object {

        private val TAG = MediaFolderPickerFragment::class.java.simpleName
        private val SCROLL_THRESHOLD = 30
        private val PERMISSION_WRITE_EXTERNAL_STORAGE_RC = 908

        fun newInstance(fileType: Int): MediaFolderPickerFragment {
            val photoPickerFragment = MediaFolderPickerFragment()
            val bun = Bundle()
            bun.putInt(BaseFragment.FILE_TYPE, fileType)
            photoPickerFragment.arguments = bun
            return photoPickerFragment
        }
    }
}// Required empty public constructor
