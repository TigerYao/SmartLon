package com.mmt.smartloan.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.blankj.utilcode.util.GsonUtils
import com.mmt.smartloan.R
import org.json.JSONObject

class DialogUtils(val data: String) {
    val context = DeviceUtils.mActivitys.lastOrNull()
    val result = JSONObject(data)
     fun showUpdateDialog(){
        DeviceUtils.mActivitys.lastOrNull()?.let { createCenterDialog(it, R.layout.common_dialog_style_1).show() }
    }

    /**
     * 通用创建居中的 Dialog, 宽度全屏 内容自定义
     */
    private fun createCenterDialog(context: Context, layoutRes: Int): AppCompatDialog {
        val forced = result["forcedUpdate"] as Boolean
        val view = LayoutInflater.from(context).inflate(layoutRes, null)
        view.findViewById<View>(R.id.buttonTv).setOnClickListener {
            val link = result["link"] as String?
            jumpMarket(link)
        }
        view.findViewById<View>(R.id.closeBtn).isVisible = !forced
        return AppCompatDialog(context)
            .apply { setContentView(view) }
            .apply {
                this.setCanceledOnTouchOutside(!forced)
                window?.attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
                window?.setBackgroundDrawable(null)
                view.findViewById<View>(R.id.closeBtn).setOnClickListener {
                    dismiss()
                }
            }
    }

    private fun jumpMarket(link: String?) {
        val browUrl = "https://play.google.com/store/apps/details?id=${link?:context?.packageName}";
        if (link == null) {
            _jumpMarket(browUrl)
        } else if(link.startsWith("http")){
            _jumpMarket(link);
        } else{
            _jumpMarket(browUrl)
        }
    }

    private fun _jumpMarket(link: String) {
        try {
            val uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            context?.startActivity(intent)
        }catch (e: Exception){

        }
    }
}