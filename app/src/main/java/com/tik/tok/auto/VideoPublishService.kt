package com.tik.tok.auto

import com.safframework.log.L
import com.tik.tok.accessibility.MyAccessibilityService
import com.utils.common.accessibility.auto.AdbScriptController
import com.utils.common.accessibility.auto.NodeController
import com.utils.common.accessibility.base.BaseAcService
import com.utils.common.accessibility.listener.TaskListener

/**
 * Description:
 * Created by Quinin on 2019-11-04.
 **/
class VideoPublishService(private val myAccessibilityService: MyAccessibilityService):BaseAcService(myAccessibilityService) {

    override fun startService() {
        L.i("")
        AdbScriptController.Builder()
           // .setXY("540,1830")  //小米4
            .setXY("540,1720")  //Pixel
            .setTaskListener(object:TaskListener{
                override fun onTaskFinished() {
                    chooseVideo()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed(failedMsg)
                }
            })
            .create()
            .execute()
    }

    private fun chooseVideo()
    {
        AdbScriptController.Builder()
            //.setXY("880,1645",2000)   //小米4
            .setXY("880,1535")  //Pixel
            .setTaskListener(object:TaskListener{
                override fun onTaskFinished() {
                    clickVideo()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed(failedMsg)
                }
            })
            .create()
            .execute()
       /* NodeController.Builder()
            .setNodeService(myAccessibilityService)
            .setNodeParams("上传",0,10)
            .setTaskListener(object:TaskListener{
                override fun onTaskFinished() {
                    clickVideo()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed(failedMsg)
                }
            })
            .create()
            .execute()*/
    }

    private fun clickVideo(){
        AdbScriptController.Builder()
            //.setXY("145,480") //小米4
            .setXY("145,370")   //Pixel
            .setTaskListener(object:TaskListener{
                override fun onTaskFinished() {
                    nextStep()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed("应用未获得root权限")
                }
            })
            .create()
            .execute()
    }

    private fun nextStep(){
        NodeController.Builder()
            .setNodeService(myAccessibilityService)
            .setNodeParams("下一步",0,5)
            .setNodeParams("下一步",0,5)
            .setTaskListener(object:TaskListener{
                override fun onTaskFinished() {
                    inputTitle()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed(failedMsg)
                }
            })
            .create()
            .execute()
    }

    private fun inputTitle(){
        AdbScriptController.Builder()
            .setText("视频标题")    //todo 视频标题
            //.setXY("785,1800")  //小米4
            .setXY("785,1690")  //Pixel
            .setTaskListener(object:TaskListener{
                override fun onTaskFinished() {
                    L.i("视频发布完成")
                    responSucceed()
                }

                override fun onTaskFailed(failedMsg: String) {
                    responFailed("视频发布失败：$failedMsg")
                }
            })
            .create()
            .execute()
    }
}