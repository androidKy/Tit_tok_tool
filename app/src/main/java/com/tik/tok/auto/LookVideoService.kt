package com.tik.tok.auto

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.safframework.log.L
import com.tik.tok.Constant
import com.tik.tok.accessibility.MyAccessibilityService
import com.utils.common.SPUtils
import com.utils.common.accessibility.auto.AdbScriptController
import com.utils.common.accessibility.base.BaseAcService
import com.utils.common.accessibility.listener.TaskListener

/**
 * Description:
 * Created by Quinin on 2019-12-03.
 **/
class LookVideoService(private val myAccessibilityService: MyAccessibilityService) :
    BaseAcService(myAccessibilityService) {

    private var mIsAgree: Boolean = false
    private var mIsComment: Boolean = false
    private var mLookTime: Int = 0   //单位分钟
    private var mStayTime: Int = 30  //单位秒
    private var mKeyword: String = ""
    private var mIsFirst: Boolean = false
    private var mStartTime:Long = 0L //开始时间

    companion object {
        const val MSG_WHAT_LOOK = 1000
    }

    private val mHandler: Handler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            MSG_WHAT_LOOK -> {
                lookVideo()
            }
        }
        false
    }


    override fun startService() {
        val isLookVideo =
            SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).getBoolean(Constant.KEY_IS_LOOK_VIDEO)
        if (!isLookVideo)
            return

        var lookTime =
            SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).getString(Constant.KEY_LOOK_TIME)
        if (TextUtils.isEmpty(lookTime))
            lookTime = "0"

        var stayTime =
            SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).getString(Constant.KEY_STAY_TIME)
        if (TextUtils.isEmpty(stayTime))
            stayTime = "30"

        mKeyword = SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).getString(Constant.KEY_KEYWORD)
        mLookTime = lookTime.toInt()
        mStayTime = stayTime.toInt()

        mIsAgree = SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).getBoolean(Constant.KEY_IS_AGREE)
        mIsComment =
            SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).getBoolean(Constant.KEY_IS_COMMENT)

        mStartTime = System.currentTimeMillis()
        mHandler.sendEmptyMessage(MSG_WHAT_LOOK)
    }

    private fun lookVideo() {
        //判断浏览时间是否超时
        if(mLookTime > 0)
        {
            val timeLength = (System.currentTimeMillis() - mStartTime)/(1000*60)
            if((timeLength-mLookTime)>0)
            {
                L.i("浏览时间用完")
                responSucceed()
                return
            }
        }

        if (!mIsFirst) {
            accordingKeyword()
            mIsFirst = true
            return
        }
        AdbScriptController.Builder()
            .setSwipeXY("540,1600", "540,600")
            .setTaskListener(object : TaskListener {
                override fun onTaskFinished() {
                    accordingKeyword()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed(failedMsg)
                }
            })
            .create()
            .execute()
    }

    private fun accordingKeyword() {
        //根据关键字判断视频类型
        if (!TextUtils.isEmpty(mKeyword)) {
            val keywordNodes = myAccessibilityService.findViewByFullText(mKeyword)
            if (keywordNodes != null) {
                //点赞
                //agree()
                //comment()
                L.i("找到关键字：$mKeyword")
                mHandler.sendEmptyMessageDelayed(MSG_WHAT_LOOK, mStayTime * 1000L)
            } else {
                L.i("未找到关键字")
                mHandler.sendEmptyMessageDelayed(MSG_WHAT_LOOK, 1 * 1000L)
            }
        } else {
            mHandler.sendEmptyMessageDelayed(MSG_WHAT_LOOK, 5 * 1000L)
        }
    }

    private fun agree() {
        if (!mIsAgree)
            return
        AdbScriptController.Builder()
            .setXY("1000,900")
            .create()
            .execute()
    }

    private fun comment() {
        if (!mIsComment)
            return
        AdbScriptController.Builder()
            .setXY("1000,1100")
            .setText("")    //todo 评论内容
            .create()
            .execute()
    }
}