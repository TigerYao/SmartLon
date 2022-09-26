package com.mmt.smartloan.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerLib
import com.mmt.smartloan.BuildConfig
import com.blankj.utilcode.util.DeviceUtils as Utils

class DeviceUtils() {
    companion object {
        //获取软件版本号，对应AndroidManifest.xml下android:versionCode
        private lateinit var activity: Activity
        val mActivitys = listOf<Activity>().toMutableList()
        @SuppressLint("StaticFieldLeak")
        var sInstance: DeviceUtils? = null

        @Synchronized
        fun getInstance(activity: Activity): DeviceUtils? {
            if (sInstance == null) {
                sInstance = DeviceUtils()
            }
            this.activity = activity
            return sInstance
        }

        fun addActivity(activity: Activity){
            mActivitys.add(activity)
        }

        fun removeActivity(activity: Activity){
            mActivitys.remove(activity)
        }
        fun finishAll(){
            mActivitys.map {
                it.finish()
            }
        }
    }

    fun getData(): Map<String, Any?> {
        return mapOf(
            "packageName" to activity.packageName,
            "androidId" to Utils.getUniqueDeviceId(),
            "adrVersion" to Utils.getSDKVersionCode(),
            "afid" to AppsFlyerLib.getInstance().getAppsFlyerUID(activity),
            "appVersion" to BuildConfig.VERSION_NAME,
            "appName" to "SmartLoan",
            "channelId" to "SmartLoan",
            "imei" to getIMEI(),
            "versionCode" to BuildConfig.VERSION_CODE,
            "installReferce" to GoogleReferrerHelper.ins?.args?.get("installReferce")
        );
    }

    private val versionCode: Int
        get() {
            var versionCode = 0
            try {
                //获取软件版本号，对应AndroidManifest.xml下android:versionCode
                versionCode = activity.packageManager
                    .getPackageInfo(activity.packageName, 0).versionCode
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()

                versionCode = BuildConfig.VERSION_CODE
            }
            return versionCode
        }

    /**
     * 获取版本号名称
     *
     * @param
     * @return
     */
    private val verName: String
        get() {
            var verName = ""
            try {
                verName = activity.packageManager
                    .getPackageInfo(activity.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return verName
        }


    private fun getIMEI(): String? {
        if((ContextCompat.checkSelfPermission(activity, "android.permission.READ_PRIVILEGED_PHONE_STATE") or ContextCompat.checkSelfPermission(activity, "android.permission.android.permission.READ_PHONE_STATE"))== PackageManager.PERMISSION_GRANTED) {
            val tm: TelephonyManager =
                activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val deviceId: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                tm.imei
            } else {
                tm.deviceId
            }
            return deviceId ?: "000000000000000"
        }
        return "000000000000000"
    }
}