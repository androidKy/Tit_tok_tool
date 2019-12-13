package com.tik.tok.viewmodel

/**
 * Description:
 * Created by Quinin on 2019-11-01.
 **/
interface TaskViewPresenter {
    interface TaskView{
        fun onResponTask(result:Boolean,msg:String)
        fun onResponProxy(result:Boolean,msg:String)
        fun onResponClearData(result:Boolean,msg:String)
    }

    interface TaskPresenter{
        fun getTask()
        fun startProxy()
        fun clarData()
        fun startTask()
    }
}