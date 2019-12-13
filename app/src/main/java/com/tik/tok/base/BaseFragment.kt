package com.tik.tok.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Description:
 * Created by Quinin on 2019-11-01.
 **/
abstract class BaseFragment : Fragment() {
    private lateinit var mView:View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(getViewId(),container,false)
        return mView
    }

    abstract fun getViewId():Int

    fun getFragmentView():View?{
        return mView
    }
}