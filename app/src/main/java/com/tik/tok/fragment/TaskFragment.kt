package com.tik.tok.fragment

import android.os.Bundle
import android.widget.TextView
import com.tik.tok.Constant.Companion.PKG_TIK_TOK
import com.tik.tok.R
import com.tik.tok.base.BaseFragment
import com.tik.tok.viewmodel.TaskPresenterImpl
import com.tik.tok.viewmodel.TaskViewPresenter

/**
 * Description:
 * Created by Quinin on 2019-11-01.
 **/
class TaskFragment : BaseFragment(), TaskViewPresenter.TaskView {

    companion object {
        const val STATUS_UNSTART = "任务未开始"
        const val STATUS_GET_TASKING = "正在获取任务..."
        const val STATUS_GET_TASKED = "获取任务完成"
        const val STATUS_GET_TASK_FAILED = "获取任务失败:"
        const val STATUS_START_PROXYING = "正在开启代理..."
        const val STATUS_START_PROXYED = "开启代理完成"
        const val STATUS_START_PROXY_FAILED = "开启代理失败:"
        const val STATUS_CLEAR_DATAING = "正在清理抖音缓存..."
        const val STATUS_CLEAR_DATAED = "清理缓存完成"
        const val STATUS_CLEAR_DATA_FAILED = "清理缓存失败:"
        const val STATUS_TASK_STARTED = "任务正在进行..."
    }

    private var mTaskPresenter: TaskViewPresenter.TaskPresenter? = null
    private var mTVtaskStatus: TextView? = null

    override fun getViewId(): Int {
        return R.layout.fragment_task
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mTaskPresenter = TaskPresenterImpl(this, context!!)
        mTVtaskStatus = getFragmentView()?.findViewById(R.id.tv_task_status)
    }

    fun startTask() {
        setTaskStatus(STATUS_GET_TASKING)
        mTaskPresenter?.getTask()
    }

    override fun onResponTask(result: Boolean, msg: String) {
        if (result) {
            setTaskStatus(STATUS_GET_TASKED)
            setTaskStatus(STATUS_START_PROXYING)
            mTaskPresenter?.startProxy()
        } else {
            setTaskStatus("${STATUS_GET_TASK_FAILED}$msg")
        }
    }

    override fun onResponProxy(result: Boolean, msg: String) {
        if (result) {
            setTaskStatus(STATUS_START_PROXYED)
            setTaskStatus(STATUS_CLEAR_DATAING)
            mTaskPresenter?.clarData()
        }else{
            setTaskStatus("${STATUS_START_PROXY_FAILED}$msg")
        }
    }

    override fun onResponClearData(result: Boolean, msg: String) {
        if(result)
        {
            setTaskStatus(STATUS_CLEAR_DATAED)
            setTaskStatus(STATUS_TASK_STARTED)

            mTaskPresenter?.startTask()
        }else{
            setTaskStatus("${STATUS_CLEAR_DATA_FAILED}$msg")
        }
    }

    private fun setTaskStatus(status: String) {
        mTVtaskStatus?.text = status
    }
}