package com.tik.tok.viewmodel

/**
 * Description:
 * Created by Quinin on 2019-11-01.
 **/
interface MainViewPresenter {
    interface MainView{
        fun onPermissionGranted(result: Boolean)

        fun onAccessibilityOpened(result:Boolean)
    }

    interface MainPresenter{
        fun requestPermissions(permissionList:List<String>)
        /**
         * 打开无障碍
         */
        fun openAccessibility()
    }
}