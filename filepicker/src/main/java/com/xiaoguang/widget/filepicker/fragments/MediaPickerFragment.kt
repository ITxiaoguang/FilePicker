package com.xiaoguang.widget.filepicker.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import com.xiaoguang.widget.filepicker.FilePickerConst
import com.xiaoguang.widget.filepicker.PickerManager
import com.xiaoguang.widget.filepicker.adapters.SectionsPagerAdapter
import com.xiaoguang.widget.filepicker.R

class MediaPickerFragment : BaseFragment() {

    lateinit var tabLayout: TabLayout

    lateinit var viewPager: ViewPager

    private var mListener: MediaPickerFragmentListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MediaPickerFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context?.toString() + " must implement MediaPickerFragment")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface MediaPickerFragmentListener

    override fun getLayoutId(): Int {
        return R.layout.fragment_media_picker;
    }

    override fun initView(view: View) {
        tabLayout = view.findViewById(R.id.tabs)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout.tabMode = TabLayout.MODE_FIXED

        val adapter = SectionsPagerAdapter(childFragmentManager)

        if (PickerManager.showImages()) {
            if (PickerManager.isShowFolderView)
                adapter.addFragment(MediaFolderPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_IMAGE), getString(R.string.images))
            else
                adapter.addFragment(MediaDetailPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_IMAGE), getString(R.string.images))
        } else
            tabLayout.visibility = View.GONE

        if (PickerManager.showVideo()) {
            if (PickerManager.isShowFolderView)
                adapter.addFragment(MediaFolderPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_VIDEO), getString(R.string.video))
            else
                adapter.addFragment(MediaDetailPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_VIDEO), getString(R.string.video))
        } else
            tabLayout.visibility = View.GONE

        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun lazyLoad() {
    }

    companion object {

        fun newInstance(): MediaPickerFragment {
            return MediaPickerFragment()
        }
    }
}// Required empty public constructor
