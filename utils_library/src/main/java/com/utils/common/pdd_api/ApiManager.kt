package com.utils.common.pdd_api

import android.text.TextUtils
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.OkHttpResponseListener
import com.safframework.log.L
import com.utils.common.NetworkUtils
import com.utils.common.SPUtils
import com.utils.common.ThreadUtils
import com.utils.common.UnicodeUtils
import okhttp3.Response
import org.json.JSONObject

/**
 * Description:接口管理类
 * Created by Quinin on 2019-07-29.
 **/
class ApiManager {
    private var mDataListener: DataListener? = null
    private var mImei:String = ""

    companion object {
        const val SP_REAL_DEVICE_PARAMS = "real_device_params_sp"
        const val KEY_REAL_DEVICE_IMEI = "key_real_imei"

        private const val URL_SERVER_DOMAIN: String = "49.234.51.174:8000"
        private const val URL_TEST_DOMAIN: String = "118.25.137.21:8000"
        private const val URL_HTTP: String = "http://"
        const val POST_JSON_CONTENT_TYPE: String = "application/json"

        const val URL_GET_TASK: String = "$URL_HTTP$URL_SERVER_DOMAIN/task/get/"
        const val URL_GET_COMMENT_TASK: String = "$URL_HTTP$URL_SERVER_DOMAIN/task/comment/"
        const val URL_UPDATE_TASK_INFO: String = "$URL_HTTP$URL_SERVER_DOMAIN/task/inform/"
        const val URL_GET_ACCOUNT: String = "$URL_HTTP$URL_SERVER_DOMAIN/others/account/?id="
        const val URL_UPDATE_ACCOUNT: String = "$URL_HTTP$URL_SERVER_DOMAIN/others/account/"
        const val URL_GET_ADDRESS: String = "$URL_HTTP$URL_SERVER_DOMAIN/others/address/"
        const val URL_GET_CONFIRM_SIGNED:String = "$URL_HTTP$URL_SERVER_DOMAIN/task/confirm/"

        /* const val URL_GET_TASK: String = "$URL_HTTP$URL_TEST_DOMAIN/task/get/"
         const val URL_GET_COMMENT_TASK: String = "$URL_HTTP$URL_TEST_DOMAIN/task/comment/"
         const val URL_UPDATE_TASK_INFO: String = "$URL_HTTP$URL_TEST_DOMAIN/task/inform/"
         const val URL_GET_ACCOUNT: String = "$URL_HTTP$URL_TEST_DOMAIN/others/account/?id="
         const val URL_UPDATE_ACCOUNT: String = "$URL_HTTP$URL_TEST_DOMAIN/others/account/"
         const val URL_GET_ADDRESS: String = "$URL_HTTP$URL_TEST_DOMAIN/others/address/"*/
    }

    init {
        L.init(ApiManager::class.java)
        mImei = SPUtils.getInstance(SP_REAL_DEVICE_PARAMS).getString(KEY_REAL_DEVICE_IMEI)
    }

    fun setDataListener(dataListener: DataListener): ApiManager {
        mDataListener = dataListener

        return this
    }

    /**
     * 获取普通任务
     */
    fun getNormalTask(imei: String) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                val url = "https://www.baidu.com/"   //URL_GET_TASK

                AndroidNetworking.get(URL_GET_TASK)
                    .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                    .addHeaders("Imei",imei)
                    .addHeaders("Version",0.1.toString())
                    .build()
                    .getAsOkHttpResponse(object : OkHttpResponseListener {
                        override fun onResponse(response: Response?) {
                            responseSucceed(response, "获取任务成功")
                        }

                        override fun onError(anError: ANError?) {
                            responseError(anError, "获取任务失败")
                        }
                    })
            }

            override fun unvalid() {
                responseError("获取任务", "网络连接失败")
            }

        })

    }

    /**
     * 获取评论任务
     */
    fun getCommentTask(imei:String) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                AndroidNetworking.get(URL_GET_COMMENT_TASK)
                    .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                    .addHeaders("Imei",imei)
                    .addHeaders("Version",0.1.toString())
                    .build()
                    .getAsOkHttpResponse(object : OkHttpResponseListener {
                        override fun onResponse(response: Response?) {
                            responseSucceed(response, "获取评论任务成功")
                        }

                        override fun onError(anError: ANError?) {
                            responseError(anError, "获取评论任务失败")
                        }
                    })
            }

            override fun unvalid() {
                responseError("获取评论任务", "网络连接失败")
            }
        })
    }

    /**
     * 获取确认收货任务
     */
    fun getConfrimSignedTask(imei:String){
        checkNetwork(object :NetworkListener{
            override fun valid() {
                AndroidNetworking.get(URL_GET_CONFIRM_SIGNED)
                    .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                    .addHeaders("Imei",imei)
                    .addHeaders("Version",0.1.toString())
                    .build()
                    .getAsOkHttpResponse(object:OkHttpResponseListener{
                        override fun onResponse(response: Response?) {
                            responseSucceed(response,"获取确认收货任务成功")
                        }

                        override fun onError(anError: ANError?) {
                            responseError(anError,"获取确认收货失败")
                        }
                    })
            }

            override fun unvalid() {
                responseError("获取确认收货任务","网络连接失败")
            }
        })
    }

    /**
     * 获取QQ账号
     * @param taskId 任务ID
     */
    fun getQQAccount(taskId: String) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                AndroidNetworking.get("$URL_GET_ACCOUNT$taskId")
                    .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                    .addHeaders("Imei",mImei)
                    .addHeaders("Version",0.1.toString())
                    .build()
                    .getAsOkHttpResponse(object : OkHttpResponseListener {
                        override fun onResponse(response: Response?) {
                            responseSucceed(response, "QQ登录失败，获取QQ账号成功")
                        }

                        override fun onError(anError: ANError?) {
                            responseError(anError, "QQ登录失败,获取QQ账号失败")
                        }
                    })
            }

            override fun unvalid() {
                responseError("获取账号", "网络连接失败")
            }

        })
    }

    /**
     * 获取收货地址
     */
    fun getAddressInfo(accountId: String, cityName: String) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                AndroidNetworking.post(URL_GET_ADDRESS)
                    .setContentType(POST_JSON_CONTENT_TYPE)
                    .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                    .addBodyParameter("id", accountId)
                    .addBodyParameter("city", cityName)
                    .build()
                    .getAsOkHttpResponse(object : OkHttpResponseListener {
                        override fun onResponse(response: Response?) {
                            responseSucceed(response, "获取地址成功")
                        }

                        override fun onError(anError: ANError?) {
                            responseError(anError, "获取地址失败")
                        }
                    })
            }

            override fun unvalid() {
                responseError("获取收货地址", "网络连接失败")
            }

        })
    }

    /**
     * 上传IP信息
     */
    fun uploadIpInfo(taskId: String, cityName: String, ipContent: String, macAddress: String) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                JSONObject().run {
                    put("option", "update_ip")
                    put("task_id", taskId.toInt())
                    put("ip_city", cityName)
                    put("ip_content", ipContent)
                    put("mac_address", macAddress)

                    AndroidNetworking.post(URL_UPDATE_TASK_INFO)
                        .setContentType(POST_JSON_CONTENT_TYPE)
                        .addHeaders("Imei",mImei)
                        .addHeaders("Version",0.1.toString())
                        .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                        .addJSONObjectBody(this)
                        .build()
                        .getAsOkHttpResponse(object : OkHttpResponseListener {
                            override fun onResponse(response: Response?) {
                                responseSucceed(response, "上传IP信息成功")
                            }

                            override fun onError(anError: ANError?) {
                                responseError(anError, "上传IP信息失败")
                            }
                        })
                }

            }

            override fun unvalid() {
                responseError("上传IP信息", "网络连接失败")
            }
        })
    }

    /**
     * 上报账号使用情况
     */
    fun updateQQAcount(accountId: Int, isValid: Int) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                JSONObject().run {
                    put("id", accountId)
                    put("effective", isValid)

                    AndroidNetworking.post(URL_UPDATE_ACCOUNT)
                        .setContentType(POST_JSON_CONTENT_TYPE)
                        .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                        .addHeaders("Imei",mImei)
                        .addHeaders("Version",0.1.toString())
                        .addJSONObjectBody(this)
                        .build()
                        .getAsOkHttpResponse(object : OkHttpResponseListener {
                            override fun onResponse(response: Response?) {
                                responseSucceed(response, "上报账号使用成功")
                            }

                            override fun onError(anError: ANError?) {
                                responseError(anError, "更新账号使用情况失败")
                            }
                        })
                }
            }

            override fun unvalid() {
                responseError("上报账号使用情况", "网络连接失败")
            }
        })
    }

    /**
     *
     */
    fun updateTaskStatus(taskId: String, isSucceed: Boolean, remark: String) {
        updateTaskStatus(taskId, isSucceed, "", "", "", "", remark)
    }

    /**
     * 更新任务状态
     */
    fun updateTaskStatus(
        taskId: String,
        isSucceed: Boolean,
        accountName: String,
        progress: String,
        orderId: String,
        orderMoney: String,
        failedMark: String

    ) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                JSONObject().run {
                    put("option", "complete_task")
                    put("task_id", taskId.toInt())
                    put("success", isSucceed)
                    put("nickname", accountName)
                    put("progress", progress)
                    put("order_id", orderId)
                    put("remark", failedMark)
                    put("order_amount", orderMoney)
                    put(
                        "imei",
                        SPUtils.getInstance(SP_REAL_DEVICE_PARAMS).getString(KEY_REAL_DEVICE_IMEI)
                    )

                    AndroidNetworking.post(URL_UPDATE_TASK_INFO)
                        .setContentType(POST_JSON_CONTENT_TYPE)
                        .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                        .addHeaders("Imei",mImei)
                        .addHeaders("Version",0.1.toString())
                        .addJSONObjectBody(this)
                        .build()
                        .getAsOkHttpResponse(object : OkHttpResponseListener {
                            override fun onResponse(response: Response?) {
                                responseSucceed(response, "更新任务状态成功")
                            }

                            override fun onError(anError: ANError?) {
                                responseError(anError, "更新任务状态失败")
                            }
                        })
                }
            }

            override fun unvalid() {
                responseError("更新任务状态", "网络连接失败")
            }
        })
    }

    /**
     * 更新评论任务的状态
     */
    fun updateCommentTaskStatus(taskId: Int, successCode: Int, remark: String) {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                JSONObject().run {
                    put("option", "complete_comment")
                    put("task_id", taskId)
                    put("success", successCode)
                    put("remark", remark)
                    put("imei",mImei)

                    AndroidNetworking.post(URL_UPDATE_TASK_INFO)
                        .setContentType(POST_JSON_CONTENT_TYPE)
                        .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                        .addHeaders("Imei",mImei)
                        .addHeaders("Version",0.1.toString())
                        .addJSONObjectBody(this)
                        .build()
                        .getAsOkHttpResponse(object : OkHttpResponseListener {
                            override fun onResponse(response: Response?) {
                                responseSucceed(response, "更新评论任务状态成功")
                            }

                            override fun onError(anError: ANError?) {
                                responseError(anError, "更新评论任务状态失败")
                            }

                        })
                }
            }

            override fun unvalid() {
                responseError("更新评论任务状态", "网络连接失败")
            }
        })
    }

    /**
     * 更新确认收货
     */
    fun updateConfirmSignedTask(taskId: Int, successCode: Int, remark: String)
    {
        checkNetwork(object : NetworkListener {
            override fun valid() {
                JSONObject().run {
                  //  put("option", "complete_comment")
                    put("id", taskId)
                    put("success", successCode)
                    put("remark", remark)
                    put("imei",mImei)

                    AndroidNetworking.post(URL_GET_CONFIRM_SIGNED)
                        .setContentType(POST_JSON_CONTENT_TYPE)
                        .addHeaders("API-AUTH", "a69caa73-f126-444d-879c-74e75d433940")
                        .addHeaders("Imei",mImei)
                        .addHeaders("Version",0.1.toString())
                        .addJSONObjectBody(this)
                        .build()
                        .getAsOkHttpResponse(object : OkHttpResponseListener {
                            override fun onResponse(response: Response?) {
                                responseSucceed(response, "更新确认收货任务状态成功")
                            }

                            override fun onError(anError: ANError?) {
                                responseError(anError, "更新确认收货任务状态失败")
                            }

                        })
                }
            }

            override fun unvalid() {
                responseError("更新确认收货任务状态", "网络连接失败")
            }
        })
    }


    private fun checkNetwork(networkListener: NetworkListener) {
        ThreadUtils.executeByCached(object : ThreadUtils.Task<Boolean>() {
            override fun doInBackground(): Boolean {
                return NetworkUtils.isAvailable()
            }

            override fun onSuccess(result: Boolean?) {
                result?.apply {
                    if (this)
                        networkListener.valid()
                    else networkListener.unvalid()
                }
            }

            override fun onCancel() {

            }

            override fun onFail(t: Throwable?) {
            }

        })
    }

    interface NetworkListener {
        fun valid()
        fun unvalid()
    }


    private fun responseSucceed(response: Response?, log: String) {
        ThreadUtils.executeByCached(object : ThreadUtils.Task<String>() {
            override fun doInBackground(): String? {
                return UnicodeUtils.decodeUnicode(response?.body()?.string())
            }

            override fun onSuccess(result: String?) {
                result?.let {
                    L.i("$log：$it")
                    mDataListener?.onSucceed(it)
                }
                if (TextUtils.isEmpty(result)) {
                    mDataListener?.onFailed("获取的数据为空")
                }
            }

            override fun onCancel() {
            }

            override fun onFail(t: Throwable?) {
            }

        })
    }

    private fun responseError(anError: ANError?, log: String) {
        anError?.apply {
            L.e("$log: $errorDetail")
            mDataListener?.onFailed(errorDetail)
        }
        /* ThreadUtils.executeByCached(object : ThreadUtils.Task<String>() {
             override fun doInBackground(): String? {
                 return anError?.errorDetail
             }

             override fun onSuccess(result: String?) {
                 result?.let {
                     L.e("$log：$it")
                     mDataListener?.onFailed(it)
                 }
             }

             override fun onCancel() {
             }

             override fun onFail(t: Throwable?) {
             }

         })*/
    }

    private fun responseError(tag: String, errorMsg: String) {
        L.e("$tag: $errorMsg")
        mDataListener?.onFailed("$tag: $errorMsg")
    }
}