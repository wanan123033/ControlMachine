package com.feipulai.exam.netUtils.download;

/**
 * Created by pengjf on 2018/10/10.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface DownloadListener {
    void onStart();//下载开始

    void onProgress(int progress);//下载进度

    void onFinish(String path);//下载完成

    void onFailure(String errorInfo);//下载失败
}
