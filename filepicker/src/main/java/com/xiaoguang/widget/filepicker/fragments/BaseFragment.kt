package com.xiaoguang.widget.filepicker.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass.
 */
abstract class BaseFragment : Fragment() {
    companion object {

        val FILE_TYPE = "FILE_TYPE"
    }

    /**
     * 视图是否初始化完毕
     */
    private var isViewPrepare = false

    /**
     * 数据是否加载过了
     */
    private var hasLoadData = false
    /**
     * 视图是否可见
     */
    protected var isViewVisible = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(getLayoutId(), container, false)
        initView(rootView)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewPrepare = true
        lazyLoadDataIfPrepared()
    }

    protected fun lazyLoadDataIfPrepared() {
        if (isViewPrepare &&
                isViewVisible &&
                !hasLoadData) {
            hasLoadData = true
            lazyLoad()
        }
    }

    /**
     * hide/show 方式懒加载控制
     */
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            isViewVisible = true
            lazyLoadDataIfPrepared()
        } else {
            isViewVisible = false
        }
    }

    /**
     * viewPager方式懒加载控制
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (userVisibleHint) {
            isViewVisible = true
            lazyLoadDataIfPrepared()
        } else {
            isViewVisible = false
        }
    }

    /**
     * 加载布局
     */
    @LayoutRes
    abstract fun getLayoutId(): Int

    abstract fun initView(view: View)

    /**
     * 懒加载
     */
    abstract fun lazyLoad()

    override fun onDestroy() {
        isViewPrepare = false
        isViewVisible = false
        hasLoadData = false
        super.onDestroy()
    }
}// Required empty public constructor
