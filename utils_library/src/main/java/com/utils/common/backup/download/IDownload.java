package com.utils.common.backup.download;

/**
 * description: 下载接口
 * author: kyXiao
 * created date: 2018/9/18
 */

public interface IDownload {
    void setParams(String ftpIp, String uid, String aid, String fileName);

    void addListener(IDownloadListener downloadListener);

    void download();

    interface IDownloadListener {
        void onDownloadProcess(String msg, int progress);

        void onDownloadSuccess(String filePath);

        void onDownloadFailed(String errorMsg);
    }
}
