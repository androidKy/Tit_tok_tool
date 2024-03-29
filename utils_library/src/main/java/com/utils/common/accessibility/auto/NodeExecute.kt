package com.utils.common.accessibility.auto

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.safframework.log.L
import com.utils.common.accessibility.base.BaseAccessibilityService
import com.utils.common.accessibility.listener.AfterClickedListener
import com.utils.common.accessibility.listener.NodeFoundListener
import com.utils.common.accessibility.listener.TaskListener
import com.utils.common.accessibility.utils.AdbScrollUtils
import com.utils.common.accessibility.utils.WidgetConstant

/**
 * Description:
 * Created by Quinin on 2019-07-12.
 **/
class NodeExecute(
    val nodeService: BaseAccessibilityService, val nodeTextList: ArrayList<String>,
    val nodeClickedList: ArrayList<Boolean>, val nodeFlagList: ArrayList<Int>,
    val nodeEditTextList: ArrayList<String>, val nodeTimeOutList: ArrayList<Int>,
    val taskListener: TaskListener, val filterText: String?, val nodeScrolledList: ArrayList<Boolean>,
    val nodeFindList: ArrayList<Boolean>
) {
    private var mStartTime: Int = 0

    init {
        L.init(NodeExecute::class.java.simpleName)
    }

    companion object {
        const val MSG_NOT_FOUND = 404
        const val MSG_START = 1
    }


    private var mHandler: Handler = Handler(Looper.getMainLooper()) {

        when (it.what) {
            MSG_NOT_FOUND -> {
                findNode(it.arg1)
            }

            MSG_START -> {
                if (nodeTextList.size > 0)
                    findNode(0)
                else L.i("查找的节点集合为0")
            }
        }
        false
    }

    fun startFindNodeList() {
        mHandler.sendEmptyMessage(MSG_START)
    }

    fun findNode(index: Int) {
        if (index > nodeTextList.size - 1) {
            L.i("index = $index 超过数组范围.")
            return
        }
        val textOrId = nodeTextList[index]
        val isClicked = nodeClickedList[index]
        val nodeFlag = nodeFlagList[index]
        val editInputText = nodeEditTextList[index]
        val timeout = nodeTimeOutList[index]
        val isScrolled = nodeScrolledList[index]

        // L.i("查找节点的线程名：${Thread.currentThread().name} 节点：$textOrId")
        L.i(
            "开始查找节点：textOrId: $textOrId; isFoundById: $nodeFlag; isClicked: $isClicked; " +
                    "editInputText: $editInputText; timeout: $timeout; isScrolled: $isScrolled"
        )

        val nodeResult = findNode(textOrId, nodeFlag)
        when {
            nodeResult == null && mStartTime <= timeout -> {
                mStartTime += 1
                val message = mHandler.obtainMessage()
                message?.arg1 = index
                message?.what = MSG_NOT_FOUND
                mHandler.sendMessageDelayed(message, 1500)
            }
            nodeResult == null && mStartTime > timeout -> //找不到时是否需要下滑查找
                dealNodeFailed(index, textOrId, editInputText, isClicked, isScrolled)
            nodeResult != null -> dealNodeSucceed(index, textOrId, editInputText, isClicked, nodeResult)
            else -> dealNodeFailed(index, textOrId, editInputText, isClicked, isScrolled)
        }
    }

    fun findNode(textOrId: String, nodeFlag: Int): AccessibilityNodeInfo? {
        var nodeInfo: AccessibilityNodeInfo? = null
        nodeService.apply {
            when (nodeFlag) {
                0 -> nodeInfo = findViewByFullText(textOrId)
                1 -> nodeInfo = findViewByText(textOrId)
                2 -> nodeInfo = findViewById(textOrId)
                3 -> this.rootInActiveWindow?.apply {
                    findViewByClassName(this, textOrId, object : NodeFoundListener {
                        override fun onNodeFound(nodeResult: AccessibilityNodeInfo?) {
                            nodeInfo = nodeResult
                        }
                    })
                }
            }
        }

        return nodeInfo
    }

    fun dealNodeFailed(index: Int, textOrId: String, editInputText: String, isClicked: Boolean, isScrolled: Boolean) {
        mHandler.removeMessages(MSG_START)
        mHandler.removeMessages(MSG_NOT_FOUND)
        mStartTime = 0
        L.i("$textOrId node was not found ")

        if (isScrolled) {
            AdbScrollUtils.instantce
                .setNodeService(nodeService)
                .setFindText(textOrId)
                .setTaskListener(object : NodeFoundListener {
                    override fun onNodeFound(nodeInfo: AccessibilityNodeInfo?) {
                        L.i("Adb 滑动查找的结果：${nodeInfo?.text}")
                        if (nodeInfo == null) taskListener.onTaskFailed(textOrId)
                        else dealNodeSucceed(index, textOrId, editInputText, isClicked, nodeInfo)
                    }
                })
                .startScroll()
            return
            /*nodeService.findViewByClassName(
                nodeService.rootInActiveWindow,
                WidgetConstant.RECYCLERVIEW,
                object : NodeFoundListener {
                    override fun onNodeFound(nodeInfo: AccessibilityNodeInfo?) {
                        nodeInfo?.apply {
                            ScrollUtils(nodeService, this)
                                .setForwardTotalTime(20)
                                .setNodeText(textOrId)
                                .setNodeFoundListener(object : NodeFoundListener {
                                    override fun onNodeFound(nodeInfo: AccessibilityNodeInfo?) {
                                        if (nodeInfo == null) taskListener.onTaskFailed(textOrId)
                                        L.i("下滑找到节点：${nodeInfo?.text}")
                                        nodeInfo?.apply {
                                            dealNodeSucceed(index, textOrId, editInputText, isClicked, this)
                                        }
                                    }
                                })
                                .scrollForward()
                        }
                    }
                })*/

        }

        if (index < nodeTextList.size - 1) {   //当查找一个节点通过多种方法时
            if (TextUtils.isEmpty(filterText)) {
                if (nodeFindList[index])    //
                    findNode(index + 1)
                else taskListener.onTaskFailed(textOrId)
            } else {  //如果filterText存在，过滤后面的节点
                findNode(filterText!!, 0)?.apply {
                    taskListener.onTaskFinished()
                }
            }
        } else {
            taskListener.onTaskFailed(textOrId)
        }
    }

    fun dealNodeSucceed(
        index: Int,
        textOrId: String,
        editInputText: String,
        isClicked: Boolean,
        nodeResult: AccessibilityNodeInfo
    ) {
        mHandler.removeMessages(MSG_START)
        mHandler.removeMessages(MSG_NOT_FOUND)
        mStartTime = 0
        L.i("nodeResult: ${nodeResult.text}")
        nodeResult.apply {
            if (editInputText != "null") {
                WidgetConstant.setEditText(editInputText, this)
            }

            if (isClicked) {  //点击
                nodeService.performViewClick(this, 1, object : AfterClickedListener {
                    override fun onClicked() {
                        if (index == nodeTextList.size - 1) {
                            taskListener.onTaskFinished()
                        } else
                            findNode(index + 1)
                    }
                })
            } else {  //不点击，直接找下一个节点
                if (index == nodeTextList.size - 1)
                    taskListener.onTaskFinished()
                else
                    findNode(index + 1)
            }
        }
    }
}