package com.utils.common.backup;

import android.content.Context;

import com.safframework.log.L;
import com.utils.common.CMDUtil;
import com.utils.common.ThreadUtils;
import com.utils.common.backup.backup.IBackup;
import com.utils.common.backup.restore.IRestore;
import com.utils.common.backup.utils.ThreadUtil;
import com.utils.common.backup.utils.ZipUtils;
import com.utils.common.backup.zip.IZip;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static com.utils.common.backup.BackupConstant.SDCARD_PATH;

/**
 * description: 备份管理实现类
 * author: kyXiao
 * created date: 2018/9/12
 */

public class BackupManageImpl implements IManager {
    private static final String TAG = "BackupManageImpl";

    public static final String DATA_ZIP_PRE_PATH = SDCARD_PATH + "backupData" + File.separator;

    private String mPkgName;

    private Context mContext;

    private String BACKUP_WAY = BackupConstant.TAI_BACKUP;
    private String mDestDir = "";

    private IBackup mBackupObj = null;
    private IResponListener mResponListener;
    private List<String> mPackageNameList;
    private boolean mIsOperating = false;   //是否正在备份或者还原

    private String mFtpIp = "";
    private String mUid = "";
    private String mAid = "";
    private String mZipFileName = "";   //备份文件名

    private static volatile BackupManageImpl mInstance;

    private BackupManageImpl() {

    }

    public static BackupManageImpl getInstance() {
        if (mInstance == null) {
            synchronized (BackupManageImpl.class) {
                if (mInstance == null)
                    mInstance = new BackupManageImpl();
            }
        }

        return mInstance;
    }

    @Override
    public BackupManageImpl setContext(Context context) {
        if (context != null)
            mContext = context.getApplicationContext();
        return this;
    }

    @Override
    public BackupManageImpl setBackupWay(String type) {
        BACKUP_WAY = type;
        return this;
    }

    @Override
    public BackupManageImpl setBackupDestDir(String destDir) {
        mDestDir = destDir;
        return this;
    }

    @Override
    public BackupManageImpl setParams(String ftpIp, String uid, String aid, String fileName) {
        this.mFtpIp = ftpIp;
        this.mUid = uid;
        this.mAid = aid;
        this.mZipFileName = fileName;
        return this;
    }


/*
    @Override
    public BackupManageImpl setUid(String mUid) {
        BackupConstant.UID = mUid;
        return this;
    }

    @Override
    public BackupManageImpl setAid(String mAid) {
        BackupConstant.AID = mAid;
        return this;
    }*/


    @Override
    public BackupManageImpl backup(String packageName) {
        mPkgName = packageName;
        backup(packageName, null);

        return this;
    }

    //备份完app数据到sdcard后
    //再压缩sdcard到zipFile目录下
    //然后上传到FTP,清楚备份的数据
    @Override
    public BackupManageImpl backup(String packageName, IResponListener responListener) {
        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);

        backup(packageNameList, responListener);

        return this;
    }

    @Override
    public BackupManageImpl backup(List<String> packageNameList, IResponListener responListener) {
        mResponListener = responListener;
        if (mContext == null) {
            //mResponListener.onResponFailed("未设置context");
            onFailed("未设置context");
            return this;
        }
        L.i(TAG, "mIsOperating = " + mIsOperating);
        if (mIsOperating) {
            onFailed("正在备份...请等待完成再进行操作");
            return this;
        }
        mIsOperating = true;

        backAppData(packageNameList);

        return this;
    }

    /**
     * 1、下载服务器的备份压缩包
     * 2、解压到sdcard/zipDir
     * 3、清空sdcard数据并移动加压的数据到sdcard上
     * 4、恢复backupData目录下的app数据
     *
     * @param packageName
     */
    @Override
    public void restore(String packageName) {
        restore(packageName, null);
    }

    @Override
    public void restore(String packageName, IResponListener responListener) {
        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);

        restore(packageNameList, responListener);
    }

    @Override
    public void restore(List<String> packageNameList, IResponListener responListener) {
        mResponListener = responListener;
        mPackageNameList = new LinkedList<>();
        mPackageNameList.clear();
        mPackageNameList.addAll(packageNameList);

        if (mIsOperating) {
            onFailed("正在进行恢复备份，请等待完成再次操作");
            return;
        }
        mIsOperating = true;

        download();
    }

    @Override
    public void removeBackup(String packageName) {
        removeBackup(packageName, null);
    }

    @Override
    public void removeBackup(String packageName, IResponListener responListener) {
        List<String> packageNameList = new LinkedList<>();
        packageNameList.add(packageName);
        removeBackup(packageNameList, responListener);
    }

    @Override
    public void removeBackup(List<String> packageNameList, IResponListener responListener) {

        if (mIsOperating) {
            onFailed("正在删除备份，请等待完成再次操作");
            return;
        }
        mIsOperating = true;

        mBackupObj = SimpleFactory.createBackupObj(BACKUP_WAY);
        mBackupObj.removeBackup(packageNameList, mDestDir, responListener);
    }

    /**
     * 备份app数据
     *
     * @param packageNameList
     */
    private void backAppData(List<String> packageNameList) {
        mBackupObj = SimpleFactory.createBackupObj(BACKUP_WAY);
        if (mBackupObj == null) {
            onFailed("暂时没有这种备份方式");
            return;
        }
        mBackupObj.startBackup(packageNameList, mDestDir, new IResponListener() {
            @Override
            public void onResponSuccess(String msg) {
                L.i(TAG, "备份APP数据成功");
                zipFile();
            }

            @Override
            public void onResponFailed(String msg) {
                L.e(TAG, "backAppData : 备份APP数据失败：" + msg);
                onFailed("onBackUpFailed：" + msg);
            }
        });
    }

    /**
     * 压缩
     */
    private void zipFile() {
        ThreadUtils.executeByCached(new ThreadUtils.Task<String>() {
            @Override
            public String doInBackground() throws Throwable {
                String ignoreFile = "rm -fr /sdcard/backupData/" + mPkgName + "/tinker;" +
                        // "rm -fr /sdcard/backupData/" + Constant.BUY_TOGETHER_PKG +"/files;"+  //删除这个文件夹，权限需要重新授予
                        "rm -fr /sdcard/backupData/" + mPkgName + "/cache;";
                // "rm -fr /sdcard/backupData/" + Constant.BUY_TOGETHER_PKG +"/app_tbs;";    //删除这个文件夹，需要重新登录

                return new CMDUtil().execCmd(ignoreFile);
            }

            @Override
            public void onSuccess(String result) {
                L.i("忽略文件夹(files、tinker)下的数据：" + result);

                IZip zip = SimpleFactory.createZiper(mContext);
                zip.setFileNameParams(mUid, mAid);
                zip.addZipListener(new IZip.IZipListener() {
                    @Override
                    public void onZipSuccess(String zipFileName) {
                        L.i(TAG, "压缩sdcard数据成功 文件名：" + zipFileName);
                        mZipFileName = zipFileName;
                        //uploadFile();
                        onSucceed();
                    }

                    @Override
                    public void onZipFailed(String failedMsg) {
                        L.e(TAG, "压缩sdcard数据失败：" + failedMsg);
                        onFailed("onZipFailed : " + failedMsg);
                    }
                });
                zip.startZip(BackupConstant.DATA_UNZIP_PATH +mPkgName);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFail(Throwable t) {

            }
        });
    }

    /**
     * 上传
     */
    @Override
    public void uploadFile(final IResponListener responListener) {
       /* IUpload upload = SimpleFactory.createUploader();
        upload.setParams(mFtpIp, mUid, mAid, mZipFileName);
        upload.setContext(mContext);
        upload.upload(new IUpload.IUploadListener() {
            @Override
            public void onUploadSuccess() {
                ZipUtils.deleteFile(new File(BackupConstant.ZIP_FILE_PATH));
                if (responListener != null) {
                    responListener.onResponSuccess(mZipFileName);
                }
            }

            @Override
            public void onUploadFailed(String msg) {
                //onFailed("onUploadFailed : " + msg);
                if (responListener != null) {
                    responListener.onResponFailed("上传失败：" + msg);
                }
            }
        });*/
    }


    /**
     * 下载
     */
    private void download() {
       //recoverAppData();
        unZip();
        ThreadUtils.executeByCached(new ThreadUtils.Task<Boolean>() {
            @Override
            public Boolean doInBackground() throws Throwable {

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

       /* IDownload download = SimpleFactory.createDownloader();
        download.setParams(mFtpIp, mUid, mAid, mZipFileName);
        download.addListener(new IDownload.IDownloadListener() {
            @Override
            public void onDownloadProcess(String msg, int progress) {
                // L.i(TAG,"");
            }

            @Override
            public void onDownloadSuccess(String fileName) {
                mZipFileName = fileName;
                L.i(TAG, "onAllDownloadSuccess fileName : " + fileName);
                unZip();
            }

            @Override
            public void onDownloadFailed(String errorMsg) {
                L.i(TAG, "onDownloadFailed: " + errorMsg);
                onFailed("下载备份失败：" + errorMsg);
            }
        });
        download.download();*/
    }

    /**
     * 解压
     */
    private void unZip() {
        L.i(TAG, "开始解压》》》》》》");
        File cacheFile = new File(BackupConstant.DATA_UNZIP_PATH +mPkgName);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }

        File file = new File(BackupConstant.ZIP_FILE_PATH + "2019_1011_20191011181427.zip");    //todo 压缩包文件名需要下载
        if (file.exists()) {
            //删除sdcard上的原有数据，但不删除压缩包
            //deleteFile(new File(BackupConstant.SDCARD_PATH), mZipFileName);
            String zipFilePath = file.getAbsolutePath();

            L.i(TAG, "zipFilePath : " + zipFilePath);
            ZipUtils.unZip(zipFilePath, BackupConstant.DATA_UNZIP_PATH);
            //file.delete();

            ThreadUtil.main(new Runnable() {
                @Override
                public void run() {
                    recoverAppData();
                }
            },3000);

        } else {
            onFailed("找不到解压的备份压缩包");
        }
    }

    /**
     * 移动数据到sdcard目录下
     */
    private void removeData2sdcard() {
        //deleteZipPackage(mZipFileName);

       /* String command = "cp -ar " + "/sdcard/sdcard/*" + " " + BackupConstant.SDCARD_PATH;
        CommandUtil.sendCommand(command, new CommandUtil.OnResponListener() {
            @Override
            public void onSuccess(List<String> responList) {

            }

            @Override
            public void onFailed(String msg) {
                BackupManageImpl.this.onFailed("移动数据失败： " + msg);
            }
        });*/
    }

    /**
     * 删除压缩包
     *
     * @param zipFileName
     */
    private void deleteZipPackage(String zipFileName) {
        File file = new File(SDCARD_PATH);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File file1 : files) {
                    String fileName = file1.getName();
                    if (fileName.equals("sdcard") || fileName.equals(zipFileName)) {
                        //ZipUtils.deleteFile(file1);
                        file1.delete();
                    }
                }
            }
        }
    }

    /**
     * 恢复APP数据
     */
    private void recoverAppData() {
        IRestore restore = SimpleFactory.createRestoreObj(BackupConstant.TAI_BACKUP);
        restore.restore(mPackageNameList, mDestDir, new IResponListener() {
            @Override
            public void onResponSuccess(String msg) {
                onSucceed();
            }

            @Override
            public void onResponFailed(String msg) {
                onFailed(msg);
            }
        });
    }

    /**
     * 删除sdcard上的数据除了压缩包不删除
     *
     * @param path
     * @param zipFileName
     */
    private void deleteFile(File path, String zipFileName) {
        if (!path.exists())
            return;
        if (path.isFile()) {
            if (!path.getName().equals(zipFileName))
                path.delete();
            return;
        }
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            deleteFile(files[i], zipFileName);
        }
        path.delete();
    }


    private void onFailed(final String msg) {
        mIsOperating = false;
        ThreadUtil.main(new Runnable() {
            @Override
            public void run() {
                if (mResponListener != null) {
                    mResponListener.onResponFailed(msg);
                }
            }
        });
    }

    private void onSucceed() {
        mIsOperating = false;
        ThreadUtil.main(new Runnable() {
            @Override
            public void run() {
                if (mResponListener != null)
                    mResponListener.onResponSuccess(mZipFileName);
            }
        });
    }
}
