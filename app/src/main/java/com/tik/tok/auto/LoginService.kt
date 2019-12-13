package com.tik.tok.auto

import com.tik.tok.accessibility.MyAccessibilityService
import com.tik.tok.accessibility.TaskDataUtils
import com.utils.common.accessibility.auto.AdbScriptController
import com.utils.common.accessibility.auto.NodeController
import com.utils.common.accessibility.base.BaseAcService
import com.utils.common.accessibility.listener.AfterClickedListener
import com.utils.common.accessibility.listener.TaskListener

/**
 * Description:
 * Created by Quinin on 2019-11-04.
 **/
class LoginService(private val myAccessibilityService: MyAccessibilityService) :
    BaseAcService(myAccessibilityService) {

    fun checkLogin(taskListener: TaskListener) {
        NodeController.Builder()
            .setNodeService(myAccessibilityService)
            .setNodeParams("我", 0, 5)
            .setNodeParams("密码登录", 0, false, 5)
            .setTaskListener(object : TaskListener {
                override fun onTaskFinished() {
                    // startLogin()
                    taskListener.onTaskFailed("未登录")
                }

                override fun onTaskFailed(failedMsg: String) {
                    //已登录
                    myAccessibilityService.apply {
                        performBackClick()

                        performViewClick(findViewByFullText("首页"),2,object:AfterClickedListener{
                            override fun onClicked() {
                                taskListener.onTaskFinished()
                            }
                        })
                    }
                }
            })
            .create()
            .execute()
    }

    override fun startService() {
        NodeController.Builder()
            .setNodeService(myAccessibilityService)
            .setNodeParams("我", 0, 5)
            .setNodeParams("密码登录", 0, false, 5)
            .setTaskListener(object : TaskListener {
                override fun onTaskFinished() {
                    startLogin()
                }

                override fun onTaskFailed(failedMsg: String) {
                    //已登录
                    alreadyLogined()
                }
            })
            .create()
            .execute()
    }

    private fun alreadyLogined() {
        responSucceed()
    }

    /**
     * 开始登录
     */
    private fun startLogin() {
        AdbScriptController.Builder()
            .setXY("540,645")  //点击手机号输入框
            .setText(TaskDataUtils.instance.getPhoneNumber())    //todo 输入手机号码
            .setXY("800,805")      //点击获取验证码
            .setTaskListener(object : TaskListener {
                override fun onTaskFinished() {
                    waitForSMS()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed("登录失败：$failedMsg")
                }
            })
            .create()
            .execute()
    }

    /**
     * 等待接收短信 todo
     */
    private fun waitForSMS() {

    }
}