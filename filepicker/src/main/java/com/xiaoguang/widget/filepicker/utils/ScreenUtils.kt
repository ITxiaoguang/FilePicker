package com.xiaoguang.widget.filepicker.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

/**
 *
 * hxg 2021/10/23 15:58 qq:929842234
 */
object ScreenUtils {
    /**
     * 适用于activity
     *
     * @param activity activity
     * @param isShow   true：显示 false：隐藏
     */
    fun showKeyboard(activity: Activity?, isShow: Boolean) {
        if (activity == null) {
            return
        }
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            ?: return
        if (isShow) {
            if (activity.currentFocus == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            } else {
                imm.showSoftInput(activity.currentFocus, 0)
            }
        } else {
            if (activity.currentFocus != null) {
                imm.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }
}