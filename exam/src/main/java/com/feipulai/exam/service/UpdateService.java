package com.feipulai.exam.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.utils.archiver.IArchiverListener;
import com.feipulai.common.utils.archiver.ZipArchiver;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.data.DownLoadPhotoHeaders;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.download.DownService;
import com.feipulai.exam.netUtils.download.DownloadHelper;
import com.feipulai.exam.netUtils.download.DownloadListener;
import com.feipulai.exam.netUtils.download.DownloadUtils;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;

import java.io.File;
import java.util.HashMap;

import okhttp3.Headers;

public class UpdateService extends IntentService {
    private Headers saveHeaders;
    private DownLoadPhotoHeaders photoHeaders;

    private static final Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (TextUtils.isEmpty(msg.obj.toString()) && msg.what == 1) {
                ToastUtils.showShort("服务访问失败");
            } else {
                fileZipArchiver((String) msg.obj, msg.arg1 == 1);
            }
        }
    };

    public UpdateService() {
        super("UpdateService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String lastDownLoadTime = SharedPrefsUtil.getValue(getApplicationContext(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, "");
        HttpSubscriber subscriber = new HttpSubscriber();
        subscriber.getItemStudent(TestConfigs.getCurrentItemCode(), 1, StudentItem.EXAM_NORMAL);

        photoHeaders = SharedPrefsUtil.loadFormSource(this, DownLoadPhotoHeaders.class);
        uploadPhotos(1, photoHeaders.getUploadTime());
    }

    private void uploadPhotos(int batch, final String uploadTime) {
        DownloadUtils downloadUtils = new DownloadUtils();
        HashMap<String, String> parameData = new HashMap<>();
        parameData.put("batch", batch + "");
        parameData.put("uploadTime", uploadTime);
        parameData.put("itemcode", TestConfigs.getCurrentItemCode());
        downloadUtils.downloadFile(DownloadHelper.getInstance().buildRetrofit(CommonUtils.getIp()).createService(DownService.class)
                        .downloadFile("bearer " + MyApplication.TOKEN, CommonUtils.encryptQuery("10001", uploadTime, parameData)),
                MyApplication.PATH_IMAGE, DateUtil.getCurrentTime() + ".zip", new DownloadListener() {
                    @Override
                    public void onStart(String fileName) {

                    }

                    @Override
                    public void onResponse(Headers headers) {
                        saveHeaders = headers;

                    }

                    @Override
                    public void onProgress(String fileName, int progress) {

                    }

                    @Override
                    public void onFinish(String fileName) {
                        if (!new File(MyApplication.PATH_IMAGE + fileName).exists()) {
                            return;
                        }

                        if (photoHeaders == null) {
                            photoHeaders = SharedPrefsUtil.loadFormSource(getApplicationContext(), DownLoadPhotoHeaders.class);
                        }
                        if (saveHeaders != null && !TextUtils.isEmpty(saveHeaders.get("BatchTotal"))) {
                            photoHeaders.setInit(Integer.valueOf(saveHeaders.get("PageNo")), Integer.valueOf(saveHeaders.get("BatchTotal")), saveHeaders.get("UploadTime"));
                            SharedPrefsUtil.save(getApplicationContext(), photoHeaders);
                            if (photoHeaders.getPageNo() != photoHeaders.getBatchTotal()) {
                                uploadPhotos(photoHeaders.getPageNo() + 1, uploadTime);
                            }
                            int isDismiss = photoHeaders.getPageNo() == photoHeaders.getBatchTotal() ? 1 : 0;

                            HandlerUtil.sendMessage(myHandler, 0, isDismiss, fileName);
                        } else {
                            HandlerUtil.sendMessage(myHandler, 1, 1, "");
                        }

                    }

                    @Override
                    public void onFailure(String fileName, String errorInfo) {
                        HandlerUtil.sendMessage(myHandler, 1, 1, "");
                    }
                });
    }

    private static void fileZipArchiver(final String fileName, final boolean isDismissDialog) {

        new ZipArchiver().doUnArchiver(MyApplication.PATH_IMAGE + fileName, MyApplication.PATH_IMAGE, "", new IArchiverListener() {
            @Override
            public void onStartArchiver() {

            }

            @Override
            public void onProgressArchiver(int current, int total) {
                if (current == total) {
                    //解压完成删除文件
                    new File(MyApplication.PATH_IMAGE + fileName).delete();

                }
            }

            @Override
            public void onEndArchiver() {

            }
        });
    }

}
