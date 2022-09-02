package com.mmt.smartloan.utils

import android.app.Activity
import com.kk.sdkforzip.SdkForZipUtils
import com.mmt.smartloan.plugin.SmartloanPlugin
import io.flutter.Log
import java.io.File

class TimeSDKHelp {
    companion object {
        private var sInstance: TimeSDKHelp? = null

        @Synchronized
        fun getInstance(): TimeSDKHelp {
            if (sInstance == null) {
                sInstance = TimeSDKHelp()
            }
            return sInstance!!
        }
    }

    private lateinit var mSdkForZipUtils: SdkForZipUtils
    private var mResultCallback: ((result: Boolean) -> Unit)? = null
    val TAG = "TimeSDKHelp"
    fun initTimeSDK(activity: Activity) {
        mSdkForZipUtils = SdkForZipUtils(activity, object : SdkForZipUtils.OnTimeFileCallBack {
            override fun onFile(
                file: File,
                md5: String?,
                orderNo: String?,
                isSubmit: Boolean,
                json: String?
            ) {
                val map: MutableMap<String?, Any?> = HashMap()
                map["file"] = file.absolutePath
                map["md5"] = md5
                map["orderNo"] = orderNo
                map["isSubmit"] = isSubmit
                map["json"] = json
                map["result"] = true
                Log.e(TAG, "OnTimeFileCallBack onFile")
                SmartloanPlugin.getInstance()?.channel?.invokeMethod("onTimeFileCallBack", map)
            }

            override fun onFail(orderNo: String?, isSubmit: Boolean, json: String?) {
                val map = HashMap<Any?, Any?>()
                map["orderNo"] = orderNo
                map["isSubmit"] = isSubmit
                map["json"] = json
                map["result"] = false
                Log.e(TAG, "OnTimeFileCallBack onFail")
                mResultCallback?.invoke(false)
                SmartloanPlugin.getInstance()?.channel?.invokeMethod("onTimeFailCallBack", map)
            }
        })
        if (mSdkForZipUtils == null) {
            Log.e(TAG, "timeSdk is not init")
        } else {
            Log.d(TAG, "timeSdk init")
        }
    }

    fun setResultCallback(resultCallback: ((result: Boolean) -> Unit)){
        mResultCallback = resultCallback
    }

    fun collectMessage(json: String) {
        Log.e(TAG, "native collectMessage json: \n $json")
        if (mSdkForZipUtils == null) {
            Log.e(TAG, "sdk is not init")
        }
        mSdkForZipUtils?.apply {
            setJson(json)
        }
    }

    fun onRequestPermission() {
        if (mSdkForZipUtils != null) {
            // 获取权限
            // 权限回调结果需要在项目MainActivity下重写onRequestPermissionsResult
            try {
                mSdkForZipUtils!!.onRequestPermission()
            } catch (e: Exception) {
                android.util.Log.i("", "mSdkForZipUtils requestPermission error" + e.message)
            }
        } else {
            Log.e(TAG, "timeSdk is not init")
        }
    }
}