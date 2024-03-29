package com.utils.common;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.util.List;

/**
 * description: 包管理工具类
 * author:kyXiao
 * date:2019/3/15
 */
public class PackageManagerUtils {
    /*private volatile static PackageManagerUtils mInstance;

    private PackageManagerUtils() {
    }

    public static PackageManagerUtils getInstance() {
        if (mInstance == null) {
            synchronized (PackageManagerUtils.class) {
                if (mInstance == null) {
                    mInstance = new PackageManagerUtils();
                }
            }
        }
        return mInstance;
    }*/

    public static void killApplication(final String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return ;
        }
        ThreadUtils.executeByCached(new ThreadUtils.Task<Boolean>() {
            @Override
            public Boolean doInBackground() throws Throwable {
                String result = new CMDUtil().execCmd("am force-stop " + packageName);
                return null;
            }

            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
    }

    public static void startApplication(final String pkgName) {
        ThreadUtils.executeByCached(new ThreadUtils.Task<Boolean>() {
            @Override
            public Boolean doInBackground() throws Throwable {
                CMDUtil cmdUtil = new CMDUtil();
                //cmdUtil.execCmd("settings put secure accessibility_enabled 1");
                cmdUtil.execCmd("am start -n " + pkgName + "/" + pkgName + ".guide.GuideActivity");
                return false;
            }

            @Override
            public void onSuccess(Boolean result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
    }


    public static void restartApplication(final String pkgName, final String activityName) {
      /*  ActivityManager mAm = (ActivityManager) Utils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        mAm.killBackgroundProcesses(pkgName);
        startActivity(pkgName);*/
        // 关闭辅助点击
        ThreadUtils.executeByCached(new ThreadUtils.Task<Boolean>() {
            @Override
            public Boolean doInBackground() throws Throwable {
                CMDUtil cmdUtil = new CMDUtil();
                cmdUtil.execCmd("am force-stop " + pkgName);
                return false;
            }

            @Override
            public void onSuccess(Boolean result) {
                startActivity(pkgName,activityName);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
        //android.os.Process.killProcess(android.os.Process.myPid());
       /* final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);*/
    }

    private static void startActivity(final String pkgName, final String activityName) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
               /* Intent intent = Utils.getApp().getPackageManager().getLaunchIntentForPackage(pkgName);
                Utils.getApp().startActivity(intent);*/
                ThreadUtils.executeByCached(new ThreadUtils.Task<Boolean>() {
                    @Override
                    public Boolean doInBackground() throws Throwable {
                        CMDUtil cmdUtil = new CMDUtil();
                        cmdUtil.execCmd("am start -n " + pkgName + "/" + activityName);
                        return false;
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        Utils.getApp().sendBroadcast(new Intent("com.task.status"));
                        //startActivity(pkgName);
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFail(Throwable t) {

                    }
                });
            }
        }, 3000);
    }

//    public void forceStopApplication() {
//        // 关闭辅助点击
//        CMDUtil.execCmd("settings put secure accessibility_enabled 0");
//        CMDUtil.execCmd("am force-stop com.aiman.hardwarecode");
//    }

    public boolean isPackageAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName != null && pinfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isApkAvilible(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                result = true;
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

}
