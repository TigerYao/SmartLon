package com.mmt.smartloan

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.mmt.smartloan.bridge.JsBridge
import com.mmt.smartloan.utils.DeviceUtils
import com.mmt.smartloan.utils.DraggingButton
import com.mmt.smartloan.utils.TimeSDKHelp

class WebActivity : AppCompatActivity() {
    private lateinit var webview: WebView
    private lateinit var jsBridge: JsBridge
    private lateinit var dragBtn: DraggingButton
    override fun onCreate(savedInstanceState: Bundle?) {
        DeviceUtils.addActivity(this)
        super.onCreate(savedInstanceState)
        // 无title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getContentLayout())
        TimeSDKHelp.getInstance().initTimeSDK(this)
        initPage()
    }

    private fun getContentLayout() = R.layout.activity_web
    private fun initPage() {
        webview = findViewById(R.id.webview)
        dragBtn = findViewById(R.id.dragBtn)
        val pageUrl = intent.getStringExtra("url")
        jsBridge = JsBridge(this, webview)
        jsBridge.loadUrl(pageUrl)
        dragBtn.setOnClickListener {
            jsBridge.clearHistory()
        }
        dragBtn.isVisible = pageUrl == jsBridge.mainUrl
    }

    override fun onResume() {
        super.onResume()
        webview.onResume()
    }

    override fun onPause() {
        super.onPause()
        webview.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        jsBridge.destroy()
        DeviceUtils.removeActivity(this)
    }



    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(dragBtn.isVisible) {
            if (keyCode == KeyEvent.KEYCODE_BACK && jsBridge.goBack()) {
                return true
            }
            if ((keyCode == KeyEvent.KEYCODE_BACK) && (event?.repeatCount == 0)) {
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        jsBridge.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        TimeSDKHelp.getInstance().onRequestPermission()
    }

    override fun onBackPressed() {
        if(dragBtn.isVisible) {
            jsBridge.goBack()
        }else if(webview.canGoBack()){
            webview.goBack()
        }else{
            super.onBackPressed()
        }
    }

}