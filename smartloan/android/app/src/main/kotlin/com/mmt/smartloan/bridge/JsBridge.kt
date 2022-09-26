package com.mmt.smartloan.bridge

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.mmt.smartloan.utils.DeviceUtils
import com.mmt.smartloan.utils.TimeSDKHelp

/**
 * dec:
 * createBy yjzhao
 * createTime 16/5/14 11:08
 */
class JsBridge(private val context: AppCompatActivity, private val webView: WebView) :
    WebViewClient() {

    private var mMainLoadUrl: String? = null
     val mainUrl = "http://8.134.38.88:3003"
    private val jsInjector: WebViewInjector = WebViewInjector(this, context)

    init {
        setting()
    }

    @SuppressLint("JavascriptInterface")
    private fun setting() {
        //设置加载到webView的页面是否可以使用chrome上调试(调试阶段)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val webSettings = webView.settings
        //设置可与js交互
        webSettings.javaScriptEnabled = true
        // 可以读取文件缓存(manifest生效)
        webSettings.allowFileAccess = true
        // 设置可以使用localStorage
        webSettings.domStorageEnabled = true
        // 应用可以有数据库
        webSettings.databaseEnabled = true
        val dbPath = context.applicationContext.cacheDir.absolutePath;
        // 启用地理定位
        webSettings.setGeolocationEnabled(true)
        // 设置编码格式
        webSettings.defaultTextEncodingName = "utf-8"
        // 设置了这个，页面中就不会出现两边白边了
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        // 默认使用缓存
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.safeBrowsingEnabled = true
        }
        webSettings.allowFileAccess = true
        webSettings.setAppCacheEnabled(true)
        webSettings.setAppCachePath(dbPath)
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8)
        webSettings.setAllowFileAccessFromFileURLs(true);
        webView.webViewClient = this
        webView.addJavascriptInterface(jsInjector, "FKSDKJsFramework")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                jsInjector.onShowFileChooser(filePathCallback)
                return true
            }
        }
    }

    fun loadUrl(url: String?) {
        webView.post {
            url?.apply {
                if(mMainLoadUrl == null) mMainLoadUrl = url
                webView.loadUrl(url)
            }
        }
    }

    val hasUrl = listOf(
        "id_card_authentication",
        "emergency_contacts",
        "personal_infomation",
        "review_tips",
        "add_bankcard",
        "confirm_borrow",
        "additionalInfomation"
    )

    fun goBack():Boolean{
        if(mMainLoadUrl == mainUrl) {
            val viewUrl = webView.url
            viewUrl?.apply {
                val str = hasUrl.firstOrNull {
                    contains(it)
                }
                if (str != null) {
                    loadUrl(mMainLoadUrl)
                    return true
                }
                val value = this.replace("/#/", "")
                if (value == mainUrl) {
                    DeviceUtils.finishAll()
                    return false
                }
            }

            if (webView.canGoBack()) {
                webView.goBack()
                return true
            } else {
                DeviceUtils.finishAll()
                return false
            }
        }
        return false
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun invokeJsMethod(jsMethod: String, callback: ValueCallback<String>) {
        webView.evaluateJavascript(jsMethod, callback);
    }

    fun clearHistory() {
        webView.clearHistory()
        webView.clearCache(true)
        webView.clearFormData()
        loadUrl(mMainLoadUrl)
    }

    fun destroy() {
        val parentView = webView.rootView as ViewGroup
        parentView.removeView(webView)
        webView.clearCache(true)
        webView.clearHistory()
        webView.destroy()
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        return super.shouldOverrideUrlLoading(view, request)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        return super.shouldInterceptRequest(view, request)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == WebViewInjector.SelectContract && resultCode == Activity.RESULT_OK) {
            data?.data?.let { jsInjector.onContractSelected(it) }
        } else if (requestCode == WebViewInjector.LIVE_NESS) {
            jsInjector.onLiveNessBack(resultCode == Activity.RESULT_OK)
        } else if (requestCode == WebViewInjector.CAMERA_PICK) {
            jsInjector.onCameraResult()
        }
    }
}