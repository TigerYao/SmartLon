package com.mmt.smartloan.utils

import android.content.Context
import android.os.RemoteException
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse
import java.util.HashMap

class GoogleReferrerHelper {
    private var mReferrerClient: InstallReferrerClient? = null
    var args: MutableMap<String?, Any?>? = HashMap()
    fun start(context: Context?) {
        args = null;
        if (mReferrerClient != null) {
            end()
        }
        mReferrerClient = InstallReferrerClient.newBuilder(context!!).build()
        mReferrerClient?.startConnection(object : InstallReferrerStateListener {
            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                Log.d(
                    TAG,
                    String.format("onInstallReferrerSetupFinished, responseCode: %d", responseCode)
                )
                when (responseCode) {
                    InstallReferrerResponse.OK -> getParams()                       // Connection established.
                    InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {}
                    InstallReferrerResponse.SERVICE_UNAVAILABLE -> {}
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                Log.d(TAG, "onInstallReferrerServiceDisconnected")
            }
        })
    }

    private fun getParams(): Map<String?, Any?>? {
        try {
            val response = mReferrerClient!!.installReferrer
            val referrerUrl = response.installReferrer
            val referrerClickTime = response.referrerClickTimestampSeconds
            val appInstallTime = response.installBeginTimestampSeconds
            val version = response.installVersion
            args = mutableMapOf(
                "installReferce" to referrerUrl,
                "referrerClickTime" to referrerClickTime,
                "installTime" to appInstallTime,
                "installVersion" to version
            )
            return args
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        return null
    }

    private fun end() {
        if (mReferrerClient != null) {
            mReferrerClient!!.endConnection()
            mReferrerClient = null
        }
    }

    companion object {
        private var instance: GoogleReferrerHelper? = null
        val ins: GoogleReferrerHelper?
            get() {
                if (instance == null) {
                    instance = GoogleReferrerHelper()
                }
                return instance
            }
        private const val TAG = "--- ReferrerHelper"
    }
}