package com.mmt.smartloan

import ai.advance.liveness.lib.GuardianLivenessDetectionSDK
import ai.advance.liveness.lib.Market
import android.os.Bundle
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.mmt.smartloan.plugin.SmartloanPlugin
import com.mmt.smartloan.utils.GoogleReferrerHelper
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {
    private var smartloadPlugin: SmartloanPlugin? = SmartloanPlugin.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GoogleReferrerHelper.ins?.start(this)
        AppsFlyerLib.getInstance().init("yFbZbrMQ7eoqbZ4BdAPN", object: AppsFlyerConversionListener{
            override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {

            }

            override fun onConversionDataFail(p0: String?) {

            }

            override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {

            }

            override fun onAttributionFailure(p0: String?) {

            }

        }, applicationContext);

        AppsFlyerLib.getInstance().start(this);
        GuardianLivenessDetectionSDK.init(application,"54e03a28ec301bb8","36181f76c174e848", Market.Mexico);
        GuardianLivenessDetectionSDK.letSDKHandleCameraPermission()
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        smartloadPlugin?.registerWith(flutterEngine, this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == smartloadPlugin?.timeRequestCode) {
            smartloadPlugin?.onRequestPermission()
        }
    }

}
