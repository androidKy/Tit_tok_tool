package com.tik.tok

import android.Manifest
import android.annotation.TargetApi
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.tik.tok.base.BaseFragment
import com.tik.tok.fragment.ContactFragment
import com.tik.tok.fragment.ScriptFragment
import com.tik.tok.viewmodel.MainPresenterImpl
import com.tik.tok.viewmodel.MainViewPresenter
import com.utils.common.PermissionUtils
import com.utils.common.ThreadUtils
import com.utils.common.ToastUtils

class TaskActivity : AppCompatActivity(), View.OnClickListener, MainViewPresenter.MainView {


    private lateinit var mScriptFragment: ScriptFragment
    private lateinit var mContactFragment: ContactFragment
    private lateinit var mTVscriptItem: TextView
    private lateinit var mTVcontactItem: TextView

    private var mPresenter: MainViewPresenter.MainPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        initView()
        initFragment()

        mPresenter = MainPresenterImpl(this, this)

    }

    override fun onStart() {
        super.onStart()

        requestPermissions()
    }

    private fun requestPermissions() {
        val permissionArray = arrayListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
            // Manifest.permission.WRITE_SECURE_SETTINGS
        )
        mPresenter?.requestPermissions(permissionArray)
    }

    private fun initView() {
        mTVscriptItem = findViewById(R.id.tv_navigation_script)
        mTVcontactItem = findViewById(R.id.tv_navigation_contact)

        mTVscriptItem.setOnClickListener(this)
        mTVcontactItem.setOnClickListener(this)
    }

    private fun initFragment() {
        mScriptFragment = ScriptFragment()
        mContactFragment = ContactFragment()
        val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.add(R.id.fl_task_container, mScriptFragment)
        beginTransaction.add(R.id.fl_task_container, mContactFragment)
        beginTransaction.commit()

        showFragment(mScriptFragment)
    }

    private fun showFragment(fragment: BaseFragment) {
        val hideFragment: Fragment = if (fragment is ScriptFragment) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTVscriptItem.setTextColor(resources.getColor(R.color.red, theme))
                mTVcontactItem.setTextColor(resources.getColor(R.color.gray, theme))
            }
            mContactFragment
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mTVscriptItem.setTextColor(resources.getColor(R.color.gray, theme))
                mTVcontactItem.setTextColor(resources.getColor(R.color.red, theme))
            }
            mScriptFragment
        }

        val beginTransaction = supportFragmentManager.beginTransaction()
        beginTransaction.show(fragment)
            .hide(hideFragment)
            .commit()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_navigation_contact -> {
                showFragment(mContactFragment)
            }

            R.id.tv_navigation_script -> {
                showFragment(mScriptFragment)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPermissionGranted(result: Boolean) {
        mTVcontactItem.postDelayed(Runnable {
            ThreadUtils.executeByCached(object : ThreadUtils.Task<Boolean>() {
                override fun doInBackground(): Boolean {
                    return PermissionUtils.isRoot()
                }

                override fun onSuccess(rootResult: Boolean) {
                    if (result && rootResult) {
                        mPresenter?.openAccessibility()
                    } else ToastUtils.showToast(this@TaskActivity, "授权失败")
                }

                override fun onCancel() {
                }

                override fun onFail(t: Throwable?) {
                }
            })
        },1000)

    }

    override fun onAccessibilityOpened(result: Boolean) {
        if (!result)
            ToastUtils.showToast(this, "无障碍打开失败")
    }
}
