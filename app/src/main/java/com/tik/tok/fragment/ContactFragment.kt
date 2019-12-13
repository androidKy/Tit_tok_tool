package com.tik.tok.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.*
import com.safframework.log.L
import com.tik.tok.Constant
import com.tik.tok.R
import com.tik.tok.base.BaseFragment
import com.tik.tok.utils.ContactUtils
import com.utils.common.SPUtils
import com.utils.common.ThreadUtils
import com.utils.common.ToastUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

/**
 * Description:
 * Created by Quinin on 2019-12-02.
 **/
class ContactFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mTVaddedIndex: TextView
    private lateinit var mRGoperate: RadioGroup
    private lateinit var mRBaddContact: RadioButton
    private lateinit var mTVtotalContacts: TextView
    private lateinit var mRBdeleteContact: RadioButton
    private lateinit var mETstartIndex: EditText
    private lateinit var mETendIndex: EditText
    private lateinit var mTVcount: TextView
    private lateinit var mBTstart: Button
    private lateinit var mRGcontactsCount: RadioGroup
    private lateinit var mRBcontactsMin: RadioButton
    private lateinit var mRBcontactsMiddle: RadioButton
    private lateinit var mRBcontactsMax: RadioButton

    @Volatile
    private var mCount: Int = 0

    private var mStartIndex: Int = 0
    private var mStopIndex: Int = 0
    private var mRandomScope: Int = 0
    private var mStarted: Boolean = false
    private val mNamePrex = arrayOf("许", "王", "陈", "黄", "廖", "梁", "萧")
    private var mOperateThread: Thread? = null


    override fun getViewId(): Int {

        return R.layout.fragment_contact
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initView()

        initData()
    }

    private fun initView() {
        getFragmentView()?.run {
            mTVaddedIndex = findViewById(R.id.tv_added_index)
            mRGoperate = findViewById(R.id.rg_operate)
            mETstartIndex = findViewById(R.id.et_start_index)
            mETendIndex = findViewById(R.id.et_end_index)
            mRBaddContact = findViewById(R.id.rb_add_contacts)
            mRBdeleteContact = findViewById(R.id.rb_delete_contacts)
            mTVcount = findViewById(R.id.tv_count)
            mBTstart = findViewById(R.id.bt_contact_start)
            mTVtotalContacts = findViewById(R.id.tv_total_contacts)

            mRGcontactsCount = findViewById(R.id.rg_contacts_count)
            mRBcontactsMin = findViewById(R.id.rb_contacts_min)
            mRBcontactsMiddle = findViewById(R.id.rb_contacts_middle)
            mRBcontactsMax = findViewById(R.id.rb_contacts_max)
        }

        mBTstart.setOnClickListener(this)

        mRGcontactsCount.setOnCheckedChangeListener { group, checkedId ->
            mRandomScope = when (checkedId) {
                R.id.rb_contacts_min -> {
                    Random.nextInt(200, 300)
                }
                R.id.rb_contacts_middle -> {
                    Random.nextInt(500, 600)
                }
                else -> {
                    Random.nextInt(900, 1000)
                }
            }

            mETendIndex.setText((mETstartIndex.text.toString().toInt() + mRandomScope).toString())
        }
    }

    private fun initData() {
        mRBaddContact.isChecked = true
        mETstartIndex.setText("0")
        mETendIndex.setText("0")
        mETstartIndex.setSelection(mETstartIndex.text.length)
        mETendIndex.setSelection(mETendIndex.text.length)

        loadIndex()
        loadConstactsCount()

        mRBcontactsMin.isChecked = true
    }

    private fun loadIndex() {
        val startIndex =
            SPUtils.getInstance(Constant.SP_CONTACTS).getInt(Constant.KEY_START_INDEX, 0)
        val endIndex = SPUtils.getInstance(Constant.SP_CONTACTS).getInt(Constant.KEY_END_INDEX, 0)

        if (startIndex == 0 && endIndex == 0) {
            mTVaddedIndex.text = getString(R.string.tv_added_index) + "0"
        } else {
            mTVaddedIndex.text =
                getString(R.string.tv_added_index) + startIndex + "-" + endIndex + "=" + (endIndex - startIndex)
        }
    }

    private fun loadConstactsCount() {
        ThreadUtils.executeByCached(object : ThreadUtils.Task<Int>() {
            override fun doInBackground(): Int {
                var mTotalContacts = 0
                val inputStream = context?.assets?.open("sqlResult_2673093.csv")
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))

                var line = bufferedReader.readLine()
                while (line != null) {
                    mTotalContacts++
                    line = bufferedReader.readLine()
                }
                bufferedReader.close()

                return mTotalContacts
            }

            override fun onSuccess(result: Int?) {
                mTVtotalContacts.text = result?.toString()
            }

            override fun onCancel() {
            }

            override fun onFail(t: Throwable?) {
            }

        })
    }

    private fun addAphone() {
        ContactUtils.addContacts(context!!, "10G-A01", "13380085914")
        ContactUtils.addContacts(context!!, "10G-A02", "13316028946")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bt_contact_start -> {
                val currentStatus = mBTstart.text.toString()
                if (currentStatus == context?.resources?.getString(R.string.bt_start)) {
                    mBTstart.text = context?.resources?.getString(R.string.bt_stop)
                    startTask()
                } else {
                    mBTstart.text = resources.getString(R.string.bt_start)
                    stopTask()
                }
            }
        }
    }

    private fun startTask() {

        if (mRBaddContact.isChecked) //添加联系人
        {
            addContact()
        } else {  //删除联系人
            deleteContact()
        }
    }

    private fun stopTask() {
        mOperateThread?.apply {
            mStarted = false

            interrupt()
            mOperateThread = null
        }
    }

    private val mHandler = Handler(Looper.getMainLooper()) {
        if (it.what == 2000) {  //添加联系人完成
            mBTstart.text = context?.resources?.getString(R.string.bt_start)

            mTVcount.text = it.arg1.toString()

            SPUtils.getInstance(Constant.SP_CONTACTS).run {
                put(Constant.KEY_START_INDEX, mETstartIndex.text.toString().toInt(), true)
                put(Constant.KEY_END_INDEX, mETendIndex.text.toString().toInt(), true)
            }

            loadIndex()

            if (mRBcontactsMiddle.isChecked)
                addAphone()
        } else if (it.what == 20000) {  //删除联系人完成
            mBTstart.text = context?.resources?.getString(R.string.bt_start)

            SPUtils.getInstance(Constant.SP_CONTACTS).clear()

            loadIndex()
        }
        false
    }

    @Synchronized
    private fun addContact() {
        if (mStarted) {
            ToastUtils.showToast(context!!, "正在添加...")
            return
        }
        mStarted = true
        mCount = 0
        mStartIndex = mETstartIndex.text.toString().toInt()
        mStopIndex = mETendIndex.text.toString().toInt()

        if (mStopIndex - mStartIndex <= 0) {
            ToastUtils.showToast(context!!, "终止位置不能小于起始位置")
            return
        }
        if (mStopIndex - mStartIndex > 6588) {
            ToastUtils.showToast(context!!, "数值超出范围")
            return
        }
        if (mStopIndex > 6588 || mStartIndex > 6588) {
            ToastUtils.showToast(context!!, "数值超出范围")
            return
        }

        mOperateThread = Thread(Runnable {
            val inputStream = context?.assets?.open("sqlResult_2673093.csv")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

            var line = bufferedReader.readLine()
            while (line != null) {
                if (mCount >= mStartIndex && mStarted) {
                    if (mCount > mStopIndex)
                        break
                    L.i("导入手机号：$line mCount=${mCount - mStartIndex}")
                    ContactUtils.addContacts(
                        context!!,
                        "${mNamePrex[Random.nextInt(0, 6)]}某$mCount",
                        line
                    )
                    val msg = Message.obtain()
                    msg.what = 2000
                    msg.arg1 = mCount - mStartIndex
                    mHandler.sendMessage(msg)
                }

                mCount++
                line = bufferedReader.readLine()
            }
            mStarted = false
            bufferedReader.close()

            mHandler.sendEmptyMessage(20000)
        }, "OperateThread")

        mOperateThread?.start()
    }

    /**
     * 删除联系人
     */
    private fun deleteContact() {
        mOperateThread = Thread(Runnable {
            context?.run {
                ContactUtils.deleteAllContacts(this)

                mHandler.sendEmptyMessageDelayed(20000, 1000)
            }
        }, "OperateThread")
        mOperateThread?.start()
    }
}