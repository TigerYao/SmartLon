package com.mmt.smartloan.bridge

import ai.advance.core.PermissionActivity
import ai.advance.liveness.lib.impl.LivenessCallback
import ai.advance.liveness.lib.LivenessView
import ai.advance.common.utils.SystemUtil
import ai.advance.liveness.lib.GuardianLivenessDetectionSDK
import ai.advance.common.IMediaPlayer
import ai.advance.liveness.lib.Detector.DetectionType
import ai.advance.liveness.lib.Detector.WarnCode
import ai.advance.liveness.lib.LivenessResult
import ai.advance.liveness.lib.impl.LivenessGetFaceDataCallback
import ai.advance.liveness.lib.http.entity.ResultEntity
import ai.advance.liveness.lib.Detector.DetectionFailedType
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.mmt.smartloan.R

/**
 * createTime:6/7/21
 *
 * @author fan.zhang@advance.ai
 */
open class LivenessActivity : PermissionActivity(), LivenessCallback {
    /**
     * the array of tip imageView animationDrawable
     * 动作提示 imageView 的图像集合
     */
    private var mDrawableCache: SparseArray<AnimationDrawable>? = null

    /**
     * the circle mask view above livenessView
     * 蒙版控件
     */
    protected var mMaskImageView: ImageView? = null

    /**
     * liveness function view
     * 活体检测功能控件
     */
    private var mLivenessView: LivenessView? = null

    /**
     * bottom anim tip imageView
     * 底部提示动画控件
     */
    private var mTipImageView: ImageView? = null

    /**
     * bottom tip textView
     * 底部提示文本控件
     */
    private var mTipTextView: TextView? = null

    /**
     * the countdown timer view
     * 倒计时控件
     */
    private var mTimerView: TextView? = null

    /**
     * open/close sounds checkbox
     * 打开/关闭声音的单选框
     */
    private var mVoiceCheckBox: CheckBox? = null

    /**
     * the loading dialog after all action success
     * 全部动作成功后的加载框
     */
    private var mProgressLayout: View? = null

    /**
     * auth loading dialog
     * 授权过程的加载框
     */
    private var mInitProgressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_liveness)
        SystemUtil.changeActivityBrightness(this, 255)
        findViews()
        initData()
        if (GuardianLivenessDetectionSDK.isSDKHandleCameraPermission() && !allPermissionsGranted()) {
            requestPermissions()
        }
    }

    /**
     * init fields
     * 初始化变量
     */
    private fun initData() {
        mDrawableCache = SparseArray()
        mLivenessView!!.setLivenssCallback(this)
    }

    /**
     * init views
     * 初始化控件
     */
    private fun findViews() {
        mMaskImageView = findViewById(R.id.mask_view)
        mLivenessView = findViewById(R.id.liveness_view)
        mTipImageView = findViewById(R.id.tip_image_view)
        mTipTextView = findViewById(R.id.tip_text_view)
        mTimerView = findViewById(R.id.timer_text_view_camera_activity)
        mProgressLayout = findViewById(R.id.progress_layout)
        mVoiceCheckBox = findViewById(R.id.voice_check_box)
        val mBackView = findViewById<View>(R.id.back_view_camera_activity)
        mBackView.setOnClickListener { onBackPressed() }
        mVoiceCheckBox?.isChecked = IMediaPlayer.isPlayEnable()
        mVoiceCheckBox?.setOnCheckedChangeListener{ buttonView, isChecked ->
            mLivenessView?.setSoundPlayEnable(isChecked)
            if (isChecked) {
                playSound()
            }
        }
    }

    /**
     * play sound
     * 播放语音
     */
    private fun playSound() {
        if (mVoiceCheckBox!!.visibility != View.VISIBLE) {
            mVoiceCheckBox!!.visibility = View.VISIBLE
        }
        var resID = -1
        val detectionType = mLivenessView!!.currentDetectionType
        if (detectionType != null) {
            when (detectionType) {
                DetectionType.POS_YAW -> resID = R.raw.action_turn_head
                DetectionType.MOUTH -> resID = R.raw.action_open_mouth
                DetectionType.BLINK -> resID = R.raw.action_blink
                else -> {}
            }
        }
        mLivenessView!!.playSound(resID, true, 1500)
    }

    /**
     * update tip text
     * 更新提示语文案
     *
     * @param strResId resId 资源id
     */
    private fun changeTipTextView(strResId: Int) {
        mTipTextView!!.setText(strResId)
    }

    /**
     * update tip textView text
     * 更新提示文本的文案
     *
     * @param warnCode the status of current frame 当前的状态
     */
    private fun updateTipUIView(warnCode: WarnCode?) {
        if (mLivenessView!!.isVertical) { //phone not vertical
            if (warnCode != null) {
                when (warnCode) {
                    WarnCode.FACEMISSING -> changeTipTextView(R.string.liveness_no_people_face)
                    WarnCode.FACESMALL -> changeTipTextView(R.string.liveness_tip_move_closer)
                    WarnCode.FACELARGE -> changeTipTextView(R.string.liveness_tip_move_furthre)
                    WarnCode.FACENOTCENTER -> changeTipTextView(R.string.liveness_move_face_center)
                    WarnCode.FACENOTFRONTAL -> changeTipTextView(R.string.liveness_frontal)
                    WarnCode.FACENOTSTILL, WarnCode.FACECAPTURE -> changeTipTextView(R.string.liveness_still)
                    WarnCode.WARN_MOUTH_OCCLUSION -> changeTipTextView(R.string.liveness_face_occ)
                    WarnCode.FACEINACTION -> showActionTipUIView()
                    else -> {}
                }
            }
        } else {
            changeTipTextView(R.string.liveness_hold_phone_vertical)
        }
    }

    /**
     * show current action tips
     * 显示当前动作的动画提示
     */
    private fun showActionTipUIView() {
        val currentDetectionType = mLivenessView!!.currentDetectionType
        if (currentDetectionType != null) {
            var detectionNameId = 0
            when (currentDetectionType) {
                DetectionType.POS_YAW -> detectionNameId = R.string.liveness_pos_raw
                DetectionType.MOUTH -> detectionNameId = R.string.liveness_mouse
                DetectionType.BLINK -> detectionNameId = R.string.liveness_blink
                else -> {}
            }
            changeTipTextView(detectionNameId)
            val anim = getDrawRes(currentDetectionType)
            mTipImageView!!.setImageDrawable(anim)
            anim.start()
        }
    }

    /**
     * called by when detection auth start
     * 活体检测授权开始时会执行该方法
     */
    override fun onDetectorInitStart() {
        if (mInitProgressDialog != null) {
            mInitProgressDialog?.dismiss()
        }
        mInitProgressDialog = ProgressDialog(this)
        mInitProgressDialog?.setMessage(getString(R.string.liveness_auth_check))
        mInitProgressDialog?.setCanceledOnTouchOutside(false)
        mInitProgressDialog?.show()
    }

    /**
     * called by when detection auth complete
     * 活体检测授权完成后会执行该方法
     *
     * @param isValid   whether the auth is success 活体检测是否成功
     * @param errorCode the error code 错误码
     * @param message   the error message 错误信息
     */
    override fun onDetectorInitComplete(
        isValid: Boolean, errorCode: String,
        message: String
    ) {
        if (mInitProgressDialog != null) {
            mInitProgressDialog!!.dismiss()
        }
        if (isValid) {
            updateTipUIView(null)
        } else {
            val errorMessage: String = if (LivenessView.NO_RESPONSE == errorCode) {
                getString(R.string.liveness_failed_reason_auth_failed)
            } else {
                message
            }
            AlertDialog.Builder(this).setMessage(errorMessage)
                .setPositiveButton(R.string.liveness_perform) { dialog, which ->
                    LivenessResult.setErrorMsg(errorMessage)
                    dialog.dismiss()
                    setResult(Activity.RESULT_OK)
                    finish()
                }.create().show()
        }
    }

    /**
     * Get the prompt picture/animation according to the action type
     * 根据动作类型获取动画资源
     *
     * @param detectionType Action type 动作类型
     * @return Prompt picture/animation
     */
    private fun getDrawRes(detectionType: DetectionType?): AnimationDrawable {
        var resID = -1
        if (detectionType != null) {
            when (detectionType) {
                DetectionType.POS_YAW -> resID = R.drawable.anim_frame_turn_head
                DetectionType.MOUTH -> resID = R.drawable.anim_frame_open_mouse
                DetectionType.BLINK -> resID = R.drawable.anim_frame_blink
            }
        }
        val cachedDrawAble = mDrawableCache!![resID]
        return if (cachedDrawAble == null) {
            val drawable = resources.getDrawable(resID) as AnimationDrawable
            mDrawableCache!!.put(resID, drawable)
            drawable
        } else {
            cachedDrawAble
        }
    }

    /**
     * called by first action start or after an action finish
     * 当准备阶段完成时，以及每个动作完成后，会执行该方法
     */
    override fun onDetectionActionChanged() {
        playSound()
        showActionTipUIView()
        mTimerView!!.setBackgroundResource(R.drawable.liveness_shape_right_timer)
    }

    /**
     * called by local liveness detection success
     * 活体检测成功时会执行该方法
     */
    override fun onDetectionSuccess() {
        mLivenessView!!.getLivenessData(object : LivenessGetFaceDataCallback {
            override fun onGetFaceDataStart() {
                mProgressLayout!!.visibility = View.VISIBLE
                mTimerView!!.visibility = View.INVISIBLE
                mLivenessView!!.visibility = View.INVISIBLE
                mVoiceCheckBox!!.visibility = View.INVISIBLE
                mTipImageView!!.visibility = View.INVISIBLE
                mTipTextView!!.visibility = View.INVISIBLE
                mMaskImageView!!.visibility = View.INVISIBLE
            }

            override fun onGetFaceDataSuccess(entity: ResultEntity, livenessId: String) {
                // liveness detection success
                setResultData()
            }

            override fun onGetFaceDataFailed(entity: ResultEntity) {
                if (!entity.success && LivenessView.NO_RESPONSE == entity.code) {
                    LivenessResult.setErrorMsg(getString(R.string.liveness_failed_reason_bad_network))
                }
                setResultData()
            }
        })
    }

    private fun setResultData() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * called by current frame is warn or become normal,is necessary to update tip UI
     * 当前帧的状态发生异常或者从异常状态变为正常的时候，需要更新 UI 上的提示语
     *
     * @param warnCode status of current frame 本帧的状态
     */
    override fun onDetectionFrameStateChanged(warnCode: WarnCode) {
        updateTipUIView(warnCode)
    }

    /**
     * called by Remaining time changed of current action,is necessary to update countdown timer view
     * 当前动作剩余时间变化,需要更新倒计时控件上的时间
     *
     * @param remainingTimeMills remaining time of current action 毫秒单位的剩余时间
     */
    @SuppressLint("SetTextI18n")
    override fun onActionRemainingTimeChanged(remainingTimeMills: Long) {
        val mills = (remainingTimeMills / 1000).toInt()
        mTimerView!!.text = mills.toString() + "s"
    }

    /**
     * called by detection failed
     * 活体检测失败时的回调
     *
     * @param failedType    Type of failures 失败的类型
     * @param detectionType Type of action 失败的原因
     */
    override fun onDetectionFailed(failedType: DetectionFailedType, detectionType: DetectionType) {
        when (failedType) {
            DetectionFailedType.WEAKLIGHT -> changeTipTextView(R.string.liveness_weak_light)
            DetectionFailedType.STRONGLIGHT -> changeTipTextView(R.string.liveness_too_light)
            else -> {
                var errorMsg: String? = null
                when (failedType) {
                    DetectionFailedType.FACEMISSING -> when (detectionType) {
                        DetectionType.MOUTH, DetectionType.BLINK -> errorMsg = getString(
                            R.string.liveness_failed_reason_facemissing_blink_mouth
                        )
                        DetectionType.POS_YAW -> errorMsg =
                            getString(R.string.liveness_failed_reason_facemissing_pos_yaw)
                    }
                    DetectionFailedType.TIMEOUT -> errorMsg =
                        getString(R.string.liveness_failed_reason_timeout)
                    DetectionFailedType.MULTIPLEFACE -> errorMsg =
                        getString(R.string.liveness_failed_reason_multipleface)
                    DetectionFailedType.MUCHMOTION -> errorMsg =
                        getString(R.string.liveness_failed_reason_muchaction)
                }
                LivenessResult.setErrorMsg(errorMsg)
                setResultData()
            }
        }
    }

    override fun onResume() {
        uiReset()
        if (allPermissionsGranted()) {
            mLivenessView!!.onResume()
        }
        super.onResume()
    }

    override fun onPause() {
        if (mInitProgressDialog != null) {
            mInitProgressDialog!!.dismiss()
        }
        mLivenessView!!.onPause()
        super.onPause()
    }

    private fun uiReset() {
        mLivenessView!!.visibility = View.VISIBLE
        mTipTextView!!.visibility = View.VISIBLE
        mTipImageView!!.visibility = View.VISIBLE
        mMaskImageView!!.visibility = View.VISIBLE
        mTimerView!!.text = ""
        mTimerView!!.setBackgroundResource(0)
        mTimerView!!.visibility = View.VISIBLE
        mVoiceCheckBox!!.visibility = View.INVISIBLE
        mTipImageView!!.setImageDrawable(null)
        mProgressLayout!!.visibility = View.INVISIBLE
    }

    override fun onDestroy() {
        mLivenessView!!.onDestroy()
        super.onDestroy()
    }

    override fun getRequiredPermissions(): Array<String> {
        return arrayOf(Manifest.permission.CAMERA)
    }

    override fun onPermissionGranted() {}
    override fun onPermissionRefused() {
        AlertDialog.Builder(this).setMessage(getString(R.string.liveness_no_camera_permission))
            .setPositiveButton(
                getString(
                    R.string.liveness_perform
                )
            ) { dialog, which -> finish() }
            .create().show()
    }
}