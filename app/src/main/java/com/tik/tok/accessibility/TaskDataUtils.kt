package com.tik.tok.accessibility

/**
 * Description:
 * Created by Quinin on 2019-11-04.
 **/
class TaskDataUtils {

    companion object{
        val instance:TaskDataUtils by lazy(mode=LazyThreadSafetyMode.SYNCHRONIZED){
            TaskDataUtils()
        }
    }

    fun initTaskData(){

    }

    /**
     * 获取登录的手机号码
     */
    fun getPhoneNumber():String{
        return ""
    }
}