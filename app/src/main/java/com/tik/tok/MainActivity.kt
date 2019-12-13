package com.tik.tok

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import com.tik.tok.fragment.TaskFragment
import com.tik.tok.viewmodel.MainPresenterImpl
import com.tik.tok.viewmodel.MainViewPresenter
import com.utils.common.ToastUtils

class MainActivity : AppCompatActivity(), MainViewPresenter.MainView {

    private var mMainPresenter: MainViewPresenter.MainPresenter? = null
    private var mTVpermission: TextView? = null
    private var mTVaccessibility: TextView? = null
    private var mFragmentTask: TaskFragment? = null

    override fun onPermissionGranted(result: Boolean) {
        var permissionText = ""
        if (result) {
            permissionText = "申请成功"
            mMainPresenter?.openAccessibility()
        } else {
            permissionText = "申请失败"
            ToastUtils.showToast(this, "权限申请失败")
        }

        setPermisstionText(permissionText)


    }

    override fun onAccessibilityOpened(result: Boolean) {
        var accessibilityText = ""
        if (result) {
            accessibilityText = "已打开"
            //开始任务
            mFragmentTask?.startTask()
        } else {
            accessibilityText = "打开失败"
            ToastUtils.showToast(this, "无障碍打开失败")
        }

        setAccessibility(accessibilityText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.activity_main)

        initView()

        mMainPresenter = MainPresenterImpl(this, this)
    }

    private fun initView() {
        mTVpermission = findViewById(R.id.tv_permission)
        mTVaccessibility = findViewById(R.id.tv_accessibility)
    }

    override fun onResume() {
        super.onResume()

        initFragment()
    }

    private fun requestPermissions() {
        val permissionArray = arrayListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
           // Manifest.permission.WRITE_SECURE_SETTINGS
        )
        mMainPresenter?.requestPermissions(permissionArray)
    }

    private fun initFragment() {
        if (mFragmentTask != null)
            return
        mFragmentTask = TaskFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fl_main_container, mFragmentTask!!)
        transaction.commit()

        requestPermissions()
    }

    private fun setPermisstionText(text: String) {
        mTVpermission?.text = text
    }

    private fun setAccessibility(text: String) {
        mTVaccessibility?.text = text
    }
}
