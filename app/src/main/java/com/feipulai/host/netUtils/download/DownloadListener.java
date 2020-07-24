package com.feipulai.host.netUtils.download;

import okhttp3.Headers;

/**
 *
 */

public interface DownloadListener {
    void onStart(String fileName);//下载开始

    void onResponse(Headers headers);//下载开始

    void onProgress(String fileName, int progress);//下载进度

    void onFinish(String fileName);//下载完成

    void onFailure(String fileName, String errorInfo);//下载失败
}
