package com.tik.tok.accessibility

import android.view.accessibility.AccessibilityEvent
import com.safframework.log.L
import com.tik.tok.auto.LoginService
import com.tik.tok.auto.LookVideoService
import com.tik.tok.auto.VideoPublishService
import com.utils.common.accessibility.base.BaseAccessibilityService
import com.utils.common.accessibility.listener.TaskListener

/**
 * Description:
 * Created by Quinin on 2019-11-01.
 **/
class MyAccessibilityService : BaseAccessibilityService() {

    private var mCurStatus = STATUS_INIT //当前状态

    companion object {
        var TAG = MyAccessibilityService::class.java.simpleName
        const val STATUS_INIT = 100 //检查是否登录
        const val STATUS_VIDEO_PUSH = 200   //发布视频
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onInterrupt() {

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (mCurStatus == STATUS_INIT) {
            L.i("进入无障碍服务")
            setPageStatus(STATUS_VIDEO_PUSH)

            /**1、检查是否登录
             * 2、是否备份登录数据
             * 3、是否发布视频
             * 4、是否浏览视频
             */
            LoginService(this)
                .checkLogin(object : TaskListener {
                    override fun onTaskFinished() {
                        L.i("用户已登录")
                    }

                    override fun onTaskFailed(failedMsg: String) {
                        L.i("用户未登录")
                        lookVideo()
                    }
                })
        }

        authPermission()
    }

    private fun lookVideo() {
        LookVideoService(this)
            .setTaskListener(object : TaskListener {
                override fun onTaskFinished() {

                }

                override fun onTaskFailed(failedMsg: String) {
                }
            })
            .startService()
    }

    private fun authPermission() {
        findViewByText("我知道了")?.apply {
            performViewClick(this)
        }

        findViewByText("允许")?.apply {
            performViewClick(this)
        }
    }

    private fun setPageStatus(status: Int) {
        mCurStatus = status
    }
}