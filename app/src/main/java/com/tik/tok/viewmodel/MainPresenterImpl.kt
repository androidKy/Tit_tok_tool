package com.tik.tok.viewmodel

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.safframework.log.L
import com.tik.tok.accessibility.MyAccessibilityService
import com.utils.common.CMDUtil
import com.utils.common.PermissionUtils
import com.utils.common.ThreadUtils
import com.utils.common.accessibility.base.BaseAccessibilityService

/**
 * Description:
 * Created by Quinin on 2019-11-01.
 **/
class MainPresenterImpl(val view: MainViewPresenter.MainView, val context: Context) :
    MainViewPresenter.MainPresenter {

    override fun requestPermissions(permissionList: List<String>) {
        PermissionUtils
            .permission(permissionList)
            .callback(object : PermissionUtils.FullCallback {
                override fun onGranted(permissionsGranted: MutableList<String>?) {
                    view.onPermissionGranted(true)
                }

                override fun onDenied(
                    permissionsDeniedForever: MutableList<String>?,
                    permissionsDenied: MutableList<String>?
                ) {
                    view.onPermissionGranted(false)
                    // ToastUtils.showToast(context, "请授予该应用相应的权限")
                    // PackageManagerUtils.getInstance().killApplication(context.packageName)
                }
            })
            .request()
    }

    override fun openAccessibility() {
        if(!PermissionUtils.getRootAuth())
        {
            view.onAccessibilityOpened(false)
            return
        }

        if (!BaseAccessibilityService.isAccessibilitySettingsOn(
                context,
                MyAccessibilityService::class.java.canonicalName!!
            )
        ) {
            //自动开启无障碍服务
            ThreadUtils.executeByCached(object : ThreadUtils.Task<Boolean>() {
                override fun onSuccess(result: Boolean?) {
                    if (result!!) {
                        Settings.Secure.putString(
                            context.contentResolver,
                            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                            context.packageName + "/com.tik.tok.accessibility.MyAccessibilityService"
                        )
                        Settings.Secure.putInt(
                            context.contentResolver,
                            Settings.Secure.ACCESSIBILITY_ENABLED, 1
                        )
                        openAccessibility()
                    }else{
                        view.onAccessibilityOpened(true)
                    }
                }

                override fun onCancel() {

                }

                override fun onFail(t: Throwable?) {

                }

                override fun doInBackground(): Boolean {
                    val result = try {
                        CMDUtil().execCmd(
                            "pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS;"
                        )
                    } catch (e: Exception) {
                        L.e(e.message,e)
                        "false"
                    }
                    /* "settings put secure enabled_accessibility_services ${Constant.BUY_TOGETHER_PKG}/com.accessibility.service.MyAccessibilityService;" +
                     "settings put secure accessibility_enabled 1;"
         )*/
                    Log.i("adb","用adb命令开启无障碍:$result")
                    if (result.contains("Success")) {
                        return true
                    }
                    if(result == "false")
                        return false

                    return true
                }
            })

        } else {
            view.onAccessibilityOpened(true)
        }
    }
}