package com.mmt.smartloan.utils

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatButton

class DraggingButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    AppCompatButton(context, attrs) {
    private var lastX = 0f
    private var lastY = 0f
    private var beginX = 0f
    private var beginY = 0f
    private var screenWidth = 720f
    private var screenHeight = 1280f

    private fun initData(context: Context) {
        val wm: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        wm.getDefaultDisplay().getMetrics(dm)
        screenWidth = dm.widthPixels.toFloat()
        screenHeight = dm.heightPixels.toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX // 触摸点与屏幕左边的距离
                lastY = event.rawY // 触摸点与屏幕上边的距离
                beginX = lastX
                beginY = lastY
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX // x轴拖动的绝对距离
                val dy = event.rawY - lastY // y轴拖动的绝对距离

                // getLeft(): 子View的左边界到父View的左边界的距离, getRight():子View的右边界到父View的左边界的距离
                // 如下几个数据表示view应该在布局中的位置
                var left = left + dx
                var top = top + dy
                var right = right + dx
                var bottom = bottom + dy
                if (left < 0) {
                    left = 0f
                    right = left + width
                }
                if (right > screenWidth) {
                    right = screenWidth
                    left = right - width
                }
                if (top < 0) {
                    top = 0f
                    bottom = top + height
                }
                if (bottom > screenHeight) {
                    bottom = screenHeight
                    top = bottom - height
                }
                layout(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_UP ->                 // 解决拖拽的时候松手点击事件触发
                return if (Math.abs(lastX - beginX) < 10f && Math.abs(lastY - beginY) < 10f) {
                    super.onTouchEvent(event)
                } else {
                    isPressed = false
                    true
                }
            else -> {}
        }
        return super.onTouchEvent(event)
    }

    init {
        initData(context)
    }
}