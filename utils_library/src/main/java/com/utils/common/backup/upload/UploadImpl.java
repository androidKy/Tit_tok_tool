package com.utils.common.backup.upload;/*
package com.buy.together.backup.upload;

import android.content.Context;
import android.provider.Settings;

import com.safframework.log.L;

import java.io.File;
import java.lang.ref.WeakReference;

*
 * description:
 * author: kyXiao
 * created date: 2018/9/17



public class UploadImpl implements IUpload {
    private static final String TAG = "UploadImpl";

    private static volatile UploadImpl mInstance;
    private WeakReference<Context> mContextWeakReference;
    private IUploadListener mUploadListener;
    private boolean mIsUploading;
    private String mFtpIp;
    private String mUid;
    private String mAid;
    private String mFileName;

    private UploadImpl() {

    }

    public static UploadImpl getInstance() {
        if (mInstance == null) {
            synchronized (UploadImpl.class) {
                mInstance = new UploadImpl();
            }
        }

        return mInstance;
    }


    @Override
    public void setParams(String ftpIp, String uid, String aid, String fileName) {
        this.mFtpIp = ftpIp;
        this.mUid = uid;
        this.mAid = aid;
        this.mFileName = fileName;
    }

    @Override
    public void setContext(Context context) {
        mContextWeakReference = new WeakReference<>(context);
    }

    @Override
    public void upload(IUploadListener uploadListener) {
        mUploadListener = uploadListener;

        ThreadUtil.async(new Runnable() {
            @Override
            public void run() {
                try {
                    upload();
                } catch (Exception e) {
                    e.printStackTrace();
                    L.e(TAG, e.getMessage());
                    onFailed(e.getMessage());
                }
            }
        });
    }

    private void upload() throws Exception {
        mIsUploading = true;
        File file = new File(BackupConstant.ZIP_FILE_PATH);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                File uploadFile = files[0];
                LogUtil.out(TAG, "上传的文件路径：" + uploadFile.getPath());

                FTPManager ftpManager = new FTPManager();
                boolean isConnected = ftpManager.connect(mFtpIp, "Anonymous", "");
                if (isConnected) {
                    LogUtil.out(TAG, "ftp连接成功》》》》》》");
                    String aid = file.getName().split("___")[0];
                    String serverPath = String.format("%s%s/%s/", BackupConstant.FTP_BACKUP_PATH,
                            mUid, mAid);
                    // serverPath = BackupConstant.FTP_BACKUP_PATH;
                    ftpManager.uploadFile(uploadFile.getPath(), serverPath,
                            new FTPManager.IProgressListener() {
                                @Override
                                public void onProgress(String msg, int progress) {

                                }

                                @Override
                                public void onSuccess(String filePath, int ts) {

                                    onSucceed();
                                }

                                @Override
                                public void onFailed(String errorMsg, int ts) {
                                    L.e(TAG, errorMsg);
                                    UploadImpl.this.onFailed(errorMsg);
                                }
                            });
                    ftpManager.closeFTP();
                } else {
                    onFailed("FTP连接失败");
                }

            } else {
                onFailed("上传的文件不存在");
            }
        } else {
            onFailed("上传的文件不存在");
        }

    }

    private String getUID() {
        String androidId = "";
        if (mContextWeakReference != null) {
            Context context = mContextWeakReference.get();
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return androidId;
    }

    private void onFailed(String msg) {
        mIsUploading = false;
        if (mUploadListener != null)
            mUploadListener.onUploadFailed(msg);

    }

    private void onSucceed() {
        mIsUploading = false;
        if (mUploadListener != null)
            mUploadListener.onUploadSuccess();
    }
}
*/
