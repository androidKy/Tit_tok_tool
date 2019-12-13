package com.tik.tok.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.tik.tok.Constant
import com.tik.tok.R
import com.tik.tok.base.BaseFragment
import com.utils.common.PackageManagerUtils
import com.utils.common.PermissionUtils
import com.utils.common.SPUtils
import com.utils.common.ToastUtils

/**
 * Description:自动化
 * Created by Quinin on 2019-12-03.
 **/
class ScriptFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mCBisLookVideo: CheckBox
    private lateinit var mETlookTime: EditText
    private lateinit var mETstayTime: EditText
    private lateinit var mETkeyword: EditText
    private lateinit var mETtaskCount: EditText
    private lateinit var mCBisAgree: CheckBox
    private lateinit var mCBisComment: CheckBox
    private lateinit var mCBisPublish: CheckBox
    private lateinit var mCBisAutoLogin: CheckBox
    private lateinit var mCBisBackupLogin: CheckBox
    private lateinit var mBTstart: Button

    private lateinit var mTVrootTip: TextView

    override fun getViewId(): Int {
        return R.layout.fragment_script
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initView()
    }

    private fun initView() {
        getFragmentView()?.run {
            mTVrootTip = findViewById(R.id.tv_root_tip)
            mCBisLookVideo = findViewById(R.id.cb_look_video)
            mETlookTime = findViewById(R.id.et_look_time)
            mETstayTime = findViewById(R.id.et_stay_time)
            mETkeyword = findViewById(R.id.et_video_keyword)
            mETtaskCount = findViewById(R.id.et_task_count)
            mCBisAgree = findViewById(R.id.cb_agree)
            mCBisComment = findViewById(R.id.cb_comment)
            mCBisBackupLogin = findViewById(R.id.cb_backup_login)
            mCBisPublish = findViewById(R.id.cb_publish_video)
            mCBisAutoLogin = findViewById(R.id.cb_login)
            mBTstart = findViewById(R.id.bt_start)

            mBTstart.setOnClickListener(this@ScriptFragment)
        }

        initData()
    }

    private fun initData() {
        if (PermissionUtils.isRoot()) {
            if (PermissionUtils.getRootAuth())
                mTVrootTip.text = getString(R.string.tv_root_tip)
            else mTVrootTip.text = getString(R.string.tv_root_unauth)
        } else mTVrootTip.text = getString(R.string.tv_unroot_tip)

        SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).run {
            mCBisLookVideo.isChecked = getBoolean(Constant.KEY_IS_LOOK_VIDEO)
            mETlookTime.setText(getString(Constant.KEY_LOOK_TIME))
            mETstayTime.setText(getString(Constant.KEY_STAY_TIME))
            mETkeyword.setText(getString(Constant.KEY_KEYWORD))
            mETtaskCount.setText(getString(Constant.KEY_TASK_COUNT))
            mCBisAgree.isChecked = getBoolean(Constant.KEY_IS_AGREE)
            mCBisComment.isChecked = getBoolean(Constant.KEY_IS_COMMENT)
            mCBisPublish.isChecked = getBoolean(Constant.KEY_IS_PUBLISH)
            mCBisAutoLogin.isChecked = getBoolean(Constant.KEY_IS_AUTOLOGIN)
            mCBisBackupLogin.isChecked = getBoolean(Constant.KEY_IS_BACKUP)
        }
    }


    override fun onClick(v: View?) {
        if (v?.id == R.id.bt_start) {
            val isLookVideo = mCBisLookVideo.isChecked
            val lookTime = mETlookTime.text.toString()
            val stayTime = mETstayTime.text.toString()
            val keyword = mETkeyword.text.toString()
            val isAgree = mCBisAgree.isChecked
            val isComment = mCBisComment.isChecked
            val isPublish = mCBisPublish.isChecked
            val isAutoLogin = mCBisAutoLogin.isChecked
            val isBackup = mCBisBackupLogin.isChecked

            val taskCount = mETtaskCount.text.toString()

            SPUtils.getInstance(Constant.SP_AUTO_SCRIPT).run {
                put(Constant.KEY_IS_LOOK_VIDEO, isLookVideo)
                put(Constant.KEY_LOOK_TIME, lookTime)
                put(Constant.KEY_STAY_TIME, stayTime)
                put(Constant.KEY_KEYWORD, keyword)
                put(Constant.KEY_IS_AGREE, isAgree)
                put(Constant.KEY_IS_COMMENT, isComment)
                put(Constant.KEY_IS_PUBLISH, isPublish)
                put(Constant.KEY_IS_AUTOLOGIN, isAutoLogin)
                put(Constant.KEY_IS_BACKUP, isBackup)
                put(Constant.KEY_TASK_COUNT, taskCount)
            }

            val taskStatus = mBTstart.text.toString()
            if (taskStatus == resources.getString(R.string.bt_start)) {
                mBTstart.text = resources.getString(R.string.bt_stop)
                startTikTok()
            } else {
                mBTstart.text = resources.getString(R.string.bt_start)
                stopTikTok()
            }
        }
    }

    private fun startTikTok() {
        if (!PermissionUtils.getRootAuth()) {
            ToastUtils.showToast(context!!, context?.getString(R.string.tv_root_unauth)!!)
            mBTstart.text = resources.getString(R.string.bt_start)
            return
        }

        val intent = context?.packageManager?.getLaunchIntentForPackage(Constant.PKG_TIK_TOK)
        intent?.apply {
            context?.startActivity(this)
        }
    }

    private fun stopTikTok() {
        PackageManagerUtils.killApplication(Constant.PKG_TIK_TOK)
    }

}