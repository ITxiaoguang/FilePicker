package com.xiaoguang.widget.filepicker

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

//import com.netease.nim.uikit.common.activity.UI

/**
 * Created by droidNinja on 22/07/17.
 */

abstract class BaseFilePickerActivity : AppCompatActivity() {

    protected fun onCreate(savedInstanceState: Bundle?, @LayoutRes layout: Int) {
        super.onCreate(savedInstanceState)
       // setTheme(PickerManager.theme)
        setContentView(layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // fixme 这里有bug
//        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //set orientation
        requestedOrientation = PickerManager.orientation
        initView()
    }

    protected abstract fun initView()
}
