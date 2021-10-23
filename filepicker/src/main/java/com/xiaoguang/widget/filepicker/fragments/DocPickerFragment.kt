package com.xiaoguang.widget.filepicker.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

import com.xiaoguang.widget.filepicker.FilePickerConst
import com.xiaoguang.widget.filepicker.PickerManager
import com.xiaoguang.widget.filepicker.R
import com.xiaoguang.widget.filepicker.adapters.SectionsPagerAdapter
import com.xiaoguang.widget.filepicker.utils.TabLayoutHelper

/**
 * tab +ViewPager
 */
class DocPickerFragment : BaseFragment(), ViewPager.OnPageChangeListener {

    lateinit var tabLayout: TabLayout

    lateinit var viewPager: ViewPager

    private var mListener: DocPickerFragmentListener? = null
    private var adapter: SectionsPagerAdapter? = null

    interface DocPickerFragmentListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DocPickerFragmentListener) {
            mListener = context
        } else {
            throw RuntimeException(context?.toString() + " must implement DocPickerFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_doc_picker
    }

    override fun initView(view: View) {
        setViews(view)
        setUpViewPager()
    }

    override fun lazyLoad() {
    }

    private fun setViews(view: View) {
        tabLayout = view.findViewById(R.id.tabs)
        viewPager = view.findViewById(R.id.viewPager)

        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        viewPager.addOnPageChangeListener(this)
    }

    private fun setUpViewPager() {
        adapter = SectionsPagerAdapter(childFragmentManager)
        val supportedTypes = PickerManager.getFileTypes()
        //添加fragment
        if (PickerManager.showPic) {
            adapter!!.addFragment(MediaFolderPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_IMAGE), getString(R.string.images))
        }
        if (PickerManager.showVideo) {
            adapter!!.addFragment(MediaFolderPickerFragment.newInstance(FilePickerConst.MEDIA_TYPE_VIDEO), getString(R.string.video))
        }
        for (index in supportedTypes.indices) {
            adapter!!.addFragment(DocFragment.newInstance(supportedTypes[index]), supportedTypes[index].title)
        }
//        viewPager.offscreenPageLimit = supportedTypes.size
        viewPager.offscreenPageLimit = 9
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        val mTabLayoutHelper = TabLayoutHelper(tabLayout, viewPager)
        mTabLayoutHelper.isAutoAdjustTabModeEnabled = true
    }

    companion object {

        private val TAG = DocPickerFragment::class.java.simpleName

        fun newInstance(): DocPickerFragment {
            return DocPickerFragment()
        }
    }

    override fun onPageSelected(position: Int) {
        //当界面被选择
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }


}// Required empty public constructor
