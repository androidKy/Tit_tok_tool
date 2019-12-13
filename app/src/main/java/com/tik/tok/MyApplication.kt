package com.tik.tok

import android.app.Application
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.utils.common.Utils

/**
 * Description:
 * Created by Quinin on 2019-11-04.
 **/
class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)

        Logger.clearLogAdapters()
        val formatStrategy = PrettyFormatStrategy.newBuilder()
            //.showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
            //.methodCount(0)         // (Optional) How many method line to show. Default 2
            //.methodOffset(0)        // (Optional) Hides internal method calls up to offset. Default 5
            .tag("Tik_Tok")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
            .build()

        Logger.addLogAdapter(DiskLogAdapter(formatStrategy))
    }
}