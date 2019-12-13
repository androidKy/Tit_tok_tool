package com.tik.tok.viewmodel

import android.content.Context
import com.tik.tok.Constant

/**
 * Description:
 * Created by Quinin on 2019-11-01.
 **/
class TaskPresenterImpl(val view: TaskViewPresenter.TaskView, val context: Context) :
    TaskViewPresenter.TaskPresenter {


    override fun getTask() {
        view.onResponTask(true, "")
    }

    override fun startProxy() {
        view.onResponProxy(true, "")
    }

    override fun clarData() {
        view.onResponClearData(true, "")
    }

    override fun startTask() {
        val intent = context.packageManager?.getLaunchIntentForPackage(Constant.PKG_TIK_TOK)
        intent?.apply {
            context.startActivity(this)
        }
    }

}