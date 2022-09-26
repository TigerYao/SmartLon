package com.mmt.smartloan.plugin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.blankj.utilcode.util.GsonUtils
import com.kk.sdkforzip.SdkForZipUtils
import com.mmt.smartloan.R
import com.mmt.smartloan.WebActivity
import com.mmt.smartloan.utils.DeviceUtils
import com.mmt.smartloan.utils.DialogUtils
import com.mmt.smartloan.utils.TimeSDKHelp
import io.flutter.Log
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.File

/** SmartloanPlugin  */
class SmartloanPlugin : MethodChannel.MethodCallHandler {

    companion object {
        private var sInstance: SmartloanPlugin? = null
        @Synchronized
        fun getInstance(): SmartloanPlugin? {
            if (sInstance == null) {
                sInstance = SmartloanPlugin()
            }
            return sInstance
        }
    }

    var token: String = ""
    private val TAG = "SmartloanPlugin"
    var channel: MethodChannel? = null
    private var mSdkForZipUtils: SdkForZipUtils? = null
    private var mCall: MethodCall? = null
    private var mResult: MethodChannel.Result? = null
    private lateinit var mContext: Activity

    fun registerWith(flutterEngine: FlutterEngine, activity: Activity) {
        channel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger, "smartloan_plugin"
        )
        channel?.setMethodCallHandler(this)
        mContext = activity
    }

    fun logout(){
        channel?.invokeMethod("logout", null)
    }

    fun resetToken(content: String){
        channel?.invokeMethod("resetToken", content)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        mCall = call
        mResult = result
        Log.d(TAG, "onMethodCall: " + call.method +".....${call.arguments.toString()}")
        when(call.method){
            "startWebActivity" ->  call.argument<String>("url")?.let { startWebActivity(it) }
            "deviceInfo" -> result.success(DeviceUtils.getInstance(mContext)?.getData())
            "loginSuccess" -> saveJson(call.arguments.toString())
            "showUpdateDialog" -> DialogUtils(call.arguments.toString()).showUpdateDialog()
            "onTimeUpload" -> TimeSDKHelp.getInstance().mResultCallback?.invoke(call.arguments as Boolean)
            else -> result.notImplemented()
        }
    }

    private fun saveJson(args:String){
        Log.i("plugs", "saveJson...$args")
        val result = GsonUtils.fromJson(args, Map::class.java)
       result["token"]?.let {
           token = it as String
       }
        mResult?.success("0k")
    }

    private fun initTimeSDK(activity: Activity) {
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
                Log.e(TAG, "OnTimeFileCallBack onFile")
                channel?.invokeMethod("onTimeFileCallBack", map)
            }

            override fun onFail(orderNo: String?, isSubmit: Boolean, json: String?) {
                val map = HashMap<Any?, Any?>()
                map["orderNo"] = orderNo
                map["isSubmit"] = isSubmit
                map["json"] = json
                Log.e(TAG, "OnTimeFileCallBack onFail")
                channel?.invokeMethod("onTimeFailCallBack", map)
            }
        })
        if (mSdkForZipUtils == null) {
            Log.e(TAG, "timeSdk is not init")
        } else {
            Log.d(TAG, "timeSdk init")
        }
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

    private fun startWebActivity(url:String){
        Intent(mContext,WebActivity::class.java)?.putExtra("url", url)?.apply {
            mContext.startActivity(this)
            mResult?.success("ok")
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

    val timeRequestCode: Int = SdkForZipUtils.TIME_PERMISSION_REQUEST_CODE
}