package com.mmt.smartloan.bridge

import ai.advance.liveness.lib.LivenessResult
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.GsonUtils
import com.mmt.smartloan.plugin.SmartloanPlugin
import com.mmt.smartloan.utils.DeviceUtils
import com.mmt.smartloan.utils.TimeSDKHelp
import com.mmt.smartloan.utils.bitmapToBase64
import com.mmt.smartloan.utils.convertToThumb
import com.tbruyelle.rxpermissions3.RxPermissions
import java.io.File


class WebViewInjector(private val jsBridge: JsBridge, private val context: AppCompatActivity) {
    val permission = RxPermissions(context)

    companion object {
        const val SelectContract = 10010
        const val CAMERA_PICK = 10011
        const val LIVE_NESS = 10012
    }


    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var imageUri: Uri? = null

    @JavascriptInterface
    fun onShowFileChooser(filePathCallback: ValueCallback<Array<Uri>>?) {
        mFilePathCallback = filePathCallback;
        permission.requestEach(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
            .subscribe({
                Log.i("jsMessage", "onShowFileChooser")
                if (it.granted) {
                    val absolutePath = context.getExternalFilesDir(DIRECTORY_PICTURES)
                    val file =
                        File.createTempFile("${System.currentTimeMillis()}", ".png", absolutePath)
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    imageUri = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.owen.fileprovider",
                            file
                        )
                    } else {
                        Uri.fromFile(file)
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    context.startActivityForResult(intent, CAMERA_PICK)
                } else {
                    Toast.makeText(
                        context.applicationContext,
                        "Autoriza el permiso a la cámara antes de fotografiar",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }) {
                it.message?.let { it1 -> Log.e("showFileChoose", it1) }
            }
    }

    @JavascriptInterface
    fun postMessage(msg: String) {
        Log.i("jsmessage", "$msg")
        val jsMessage = GsonUtils.fromJson(msg, JSMessage::class.java)
        Log.i("jsmessage:data", "${jsMessage.action}")
        when (jsMessage.action) {
            "getLoginInfo" -> getLoginInfo(jsMessage)
            "getPackageName" -> getPackageName(jsMessage)
            "getVersionName" -> getVersionName(jsMessage)
            "toLogin" -> logOut(jsMessage)
            "logout" -> logOut(jsMessage)
            "timeSDK" -> invokeTimeSdk(jsMessage)
            "selectContact" -> selectContact(jsMessage)
            "getAccuauthSDK" -> getAccuauthSDK(jsMessage)
            "setNewToken" -> resetToken(jsMessage)
            "ToWhatsapp" -> jumpWhatsapp(jsMessage)
        }
    }

    private fun getLoginInfo(jsMessage: JSMessage) {
        jsMessage.result = "OK"
        jsMessage.data = Data(SmartloanPlugin.getInstance()?.token)
        val callback = GsonUtils.toJson(jsMessage)
        jsBridge.loadUrl("javascript: ${jsMessage.callback} (${callback})")
    }

    private fun getPackageName(jsMessage: JSMessage) {
        jsMessage.result = "OK"
        jsMessage.data = DeviceUtils.getInstance(context)?.getData()
        val callback = GsonUtils.toJson(jsMessage)
        jsBridge.loadUrl("javascript: ${jsMessage.callback} (${callback})")
    }

    private fun getVersionName(jsMessage: JSMessage) {
        jsMessage.result = "OK"
        val version = DeviceUtils.getInstance(context)?.getData()?.get("versionName")
        jsMessage.data = { "versionName" to version }
        val callback = GsonUtils.toJson(jsMessage)
        jsBridge.loadUrl("javascript: ${jsMessage.callback} (${callback})")
    }

    private fun logOut(jsMessage: JSMessage) {
        context.runOnUiThread {
            SmartloanPlugin.getInstance()?.logout()
            context.finish()
        }
    }

    private var timeSdkJSMessage: JSMessage? = null
    private fun invokeTimeSdk(jsMessage: JSMessage) {
        timeSdkJSMessage = jsMessage
        context.runOnUiThread {
            TimeSDKHelp.getInstance().setResultCallback(::timeSDkCallback)
            permission.requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe {
             if(it.granted){
                 TimeSDKHelp.getInstance().collectMessage(GsonUtils.toJson(jsMessage))
             }
            }
        }
    }

    private fun timeSDkCallback(result: Boolean) {
        timeSdkJSMessage?.apply {
            if (result) {
                this.result = "ok"
            } else
                this.result = "fail"
            val callback = GsonUtils.toJson(this)
            jsBridge.loadUrl("javascript: ${this.callback} (${callback})")
        }
    }

    private fun resetToken(jsMessage: JSMessage) {
        context.runOnUiThread {
            SmartloanPlugin.getInstance()?.resetToken(GsonUtils.toJson(jsMessage))
        }
    }

    private var contractMessage: JSMessage? = null
    private fun selectContact(jsMessage: JSMessage) {
        contractMessage = jsMessage
        context.runOnUiThread {
            Log.i("seletcontact", "startContect")
            permission.request(Manifest.permission.READ_CONTACTS)
                .subscribe {
                    if (it) {
                        val intent =
                            Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                        context.startActivityForResult(intent, SelectContract)
                    }
                }
        }

    }

    private var cameraMessage: JSMessage? = null
    private fun getAccuauthSDK(cameraJSMessage: JSMessage) {
        cameraMessage = cameraJSMessage
        val intent = Intent(context, LivenessActivity::class.java)
        context.startActivityForResult(intent, LIVE_NESS)
    }

    private fun jumpWhatsapp(jsMessage: JSMessage) {

    }

    fun onLiveNessBack(isOk: Boolean) {
        cameraMessage?.apply {
            if (isOk && LivenessResult.isSuccess()) {
                val bitmap = LivenessResult.getLivenessBitmap().convertToThumb()
                val fileStr = bitmap.bitmapToBase64()
                result = "ok"
                data = mapOf(
                    "livenessId" to LivenessResult.getLivenessId(),
                    "file" to fileStr,
                    "errorMsg" to LivenessResult.getErrorMsg()
                )
            } else {
                result = "fail"
                msg = LivenessResult.getErrorMsg()
                data = {
                    "errorMsg" to LivenessResult.getErrorMsg()
                }
            }
            val callback = GsonUtils.toJson(this)
            jsBridge.loadUrl("javascript: ${this.callback} (${callback})")
        }

    }

    @SuppressLint("Range")
    fun onContractSelected(uri: Uri) {
        if (contractMessage == null)
            return
        permission.request(Manifest.permission.READ_CONTACTS).subscribe {
            if (it) {
                var phoneNum: String? = null
                var name: String? = null
                //获取内容解析者
                val resolver: ContentResolver = context.contentResolver;
                //查询数据
                val cursor: Cursor? = resolver.query(uri, null, null, null, null);
                cursor?.apply {
                    if (moveToFirst()) {
                        name = getString(getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                        val hasPhone: String =
                            getString(getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                        val contactId: String =
                            getString(getColumnIndex(ContactsContract.Contacts._ID))
                        if (hasPhone == "1") {
                            phoneNum = getPhoneNum(contactId)
                        }
                    }
                    close()
                }
                contractMessage?.result = "OK"
                contractMessage?.data = mapOf(
                    "name" to name,
                    "phone" to phoneNum
                )
                val callback = GsonUtils.toJson(contractMessage)
                jsBridge.loadUrl("javascript: ${contractMessage?.callback} (${callback})")
            }
        }

    }

    @SuppressLint("Range")
    private fun getPhoneNum(contactId: String): String {
        var phoneNumber = ""
        val curse: Cursor? = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = "
                    + contactId,
            null,
            null
        )
        curse?.apply {
            while (moveToNext()) {
                phoneNumber =
                    getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            close();
        }
        return phoneNumber
    }

    fun onCameraResult() {
        mFilePathCallback?.apply {
            val value = imageUri ?: Uri.EMPTY
            this.onReceiveValue(arrayOf(value))
        }
    }

}