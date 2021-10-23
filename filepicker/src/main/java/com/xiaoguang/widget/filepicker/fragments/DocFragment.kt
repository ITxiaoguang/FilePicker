package com.xiaoguang.widget.filepicker.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
//import com.hande.common.core.ImService

import com.xiaoguang.widget.filepicker.FilePickerConst
import com.xiaoguang.widget.filepicker.PickerManager
import com.xiaoguang.widget.filepicker.adapters.FileAdapterListener
import com.xiaoguang.widget.filepicker.adapters.FileListAdapter
import com.xiaoguang.widget.filepicker.cursors.loadercallbacks.FileResultCallback
import com.xiaoguang.widget.filepicker.models.Document
import com.xiaoguang.widget.filepicker.models.FileType
//import io.github.prototypez.appjoint.AppJoint
import com.xiaoguang.widget.filepicker.R
/**
 * 文件列表 自定义的
 */

class DocFragment : BaseFragment(), FileAdapterListener {
    lateinit var recyclerView: RecyclerView

    lateinit var emptyView: TextView

    private var progressBar: ProgressBar? = null
    private var mListener: DocFragmentListener? = null
    private var selectAllItem: MenuItem? = null
    private var fileListAdapter: FileListAdapter? = null

    val fileType: FileType?
        get() = arguments?.getParcelable(BaseFragment.Companion.FILE_TYPE)


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DocFragmentListener) {
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


    override fun onItemSelected() {
        mListener?.onItemSelected()
        fileListAdapter?.let { adapter ->
            selectAllItem?.let { menuItem ->
                if (adapter.itemCount == adapter.selectedItemCount) {
                    menuItem.setIcon(R.drawable.ic_select_all)
                    menuItem.isChecked = true
                }
            }
        }
    }

    interface DocFragmentListener {
        fun onItemSelected()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_photo_picker
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerview)
        emptyView = view.findViewById(R.id.empty_view)
        progressBar = view.findViewById(R.id.progress_bar)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.visibility = View.GONE
    }

    override fun lazyLoad() {
        getDataFromMedia()
    }

    private fun getDataFromMedia() {
        context?.let {
            MediaStoreHelper.getDocsByFileType(it.contentResolver,
                    fileType,
                    object : FileResultCallback<Document> {
                        override fun onResultCallback(files: List<Document>) {
                            progressBar?.visibility = View.GONE
                            updateList(files.toMutableList())
                        }
                    }
            )
        }
    }

    fun updateList(dirs: MutableList<Document>) {
        var list: MutableList<Document> = ArrayList()//公告标题
        view?.let {
            if (dirs.isNotEmpty()) {
                if (fileType?.title.equals(context!!.resources.getString(R.string.voice))) {
                    list.clear()
                    for (d in dirs) {
                        if (d.path.contains(".mp3") || d.path.contains(".aac")) {
                            list.add(d);
                        }
                    }
                    dirs.clear()
                    dirs.addAll(list)
                }
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE

                context?.let {
                    fileListAdapter = recyclerView.adapter as? FileListAdapter
                    if (fileListAdapter == null) {
                        fileListAdapter = FileListAdapter(it, dirs, PickerManager.selectedFiles,
                                this)

                        recyclerView.adapter = fileListAdapter
                    } else {
                        fileListAdapter?.setData(dirs)
                        fileListAdapter?.notifyDataSetChanged()
                    }
                    onItemSelected()
                }
            } else {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.doc_picker_menu, menu)
        selectAllItem = menu?.findItem(R.id.action_select)
        if (PickerManager.hasSelectAll()) {
            selectAllItem?.isVisible = true
            onItemSelected()
        } else {
            selectAllItem?.isVisible = false
        }

        val search = menu?.findItem(R.id.search)
        val searchView = search?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                fileListAdapter?.filter?.filter(newText)
                return true
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item?.itemId
        if (itemId == R.id.action_select) {
            fileListAdapter?.let { adapter ->
                selectAllItem?.let { menuItem ->
                    if (menuItem.isChecked) {
                        adapter.clearSelection()
                        PickerManager.clearSelections()

                        menuItem.setIcon(R.drawable.ic_deselect_all)
                    } else {
                        adapter.selectAll()
                        PickerManager
                                .add(adapter.selectedPaths, FilePickerConst.FILE_TYPE_DOCUMENT)
                        menuItem.setIcon(R.drawable.ic_select_all)
                    }

                    menuItem.isChecked = !menuItem.isChecked
                    mListener?.onItemSelected()
                }
            }
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private val TAG = DocFragment::class.java.simpleName

        fun newInstance(fileType: FileType): DocFragment {
            val photoPickerFragment = DocFragment()
            val bun = Bundle()
            bun.putParcelable(BaseFragment.Companion.FILE_TYPE, fileType)
            photoPickerFragment.arguments = bun
            return photoPickerFragment
        }
    }
}// Required empty public constructor
