package com.feipulai.exam.netUtils.download;

import okhttp3.Headers;

/**
 * Created by pengjf on 2018/10/10.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface DownloadListener {
    void onStart(String fileName);//下载开始

    void onResponse(Headers headers);//下载开始

    void onProgress(String fileName, int progress);//下载进度

    void onFinish(String fileName);//下载完成

    void onFailure(String fileName, String errorInfo);//下载失败
}

