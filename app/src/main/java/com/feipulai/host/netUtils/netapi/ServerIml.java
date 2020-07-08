package com.feipulai.host.netUtils.netapi;

import android.content.Context;
import android.content.Intent;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.LoadingDialog;
import com.feipulai.host.activity.data.DataRetrieveActivity;
import com.feipulai.host.bean.UploadResults;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务接口工具类
 * Created by zzs on  2019/12/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ServerIml {


    public static void downloadData(final Context context, final String lastDownLoadTime) {
        final LoadingDialog loadingDialog;
        if (context != null) {
            loadingDialog = new LoadingDialog(context);
            loadingDialog.showDialog("数据下载中，请稍后...", true);
        } else {
            loadingDialog = null;
        }

        final ItemSubscriber subscriber = new ItemSubscriber();
        subscriber.setOnRequestEndListener(new OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case ItemSubscriber.DWON_ITEL_ALL:
                        subscriber.getStudentData( 1, lastDownLoadTime);
                        break;
                    case ItemSubscriber.DWON_STAUENT_DATA:
                        Intent intent = new Intent(DataRetrieveActivity.UPDATE_MESSAGE);
                        context.sendBroadcast(intent);
                        if (loadingDialog != null && loadingDialog.isShow()) {
                            loadingDialog.dismissDialog();
                        }
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                if (loadingDialog != null && loadingDialog.isShow()) {
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onRequestData(Object data) {

            }
        });
        subscriber.getItemAll(context);
    }


    /**
     * 上传成绩
     *
     * @param context
     * @param uploadResultsList
     */
    public static void uploadResult(final Context context, final List<UploadResults> uploadResultsList) {
        if (uploadResultsList == null || uploadResultsList.size() == 0) {
            ToastUtils.showShort("没有需要上传的成绩");
            return;
        }
        final LoadingDialog loadingDialog;
        if (context != null) {
            loadingDialog = new LoadingDialog(context);
            loadingDialog.showDialog("成绩上传中，请稍后...", true);
        } else {
            loadingDialog = null;
        }
        final ItemSubscriber subscriber = new ItemSubscriber();
        subscriber.setOnRequestEndListener(new OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case ItemSubscriber.DWON_ITEL_ALL://项目
                        subscriber.uploadResult(uploadResultsList);
                        break;

                    case ItemSubscriber.UPLOAD_BIZ://上传成绩
                        if (loadingDialog != null && loadingDialog.isShow()) {
                            loadingDialog.dismissDialog();
                        }
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                if (loadingDialog != null && loadingDialog.isShow()) {
                    loadingDialog.dismissDialog();
                }
            }

            @Override
            public void onRequestData(Object data) {

            }
        });
        subscriber.getItemAll(context);
    }


    /**
     * 自动上传成绩,不处理没有项目代码等(处理项目代码等可能会导致 项目代码 变更,在测试过程中不应该出现这种情况)
     */
    public static void uploadResult(final List<UploadResults> uploadResultsList) {
        Logger.i("自动上传成绩:" + uploadResultsList.toString());
        if (uploadResultsList == null || uploadResultsList.size() == 0) {
            ToastUtils.showShort("没有需要上传的成绩");
            return;
        }
        final ItemSubscriber subscriber = new ItemSubscriber();
        subscriber.setOnRequestEndListener(new OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case ItemSubscriber.UPLOAD_BIZ://上传成绩
                        Logger.i("成绩自动上传成功");
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                Logger.i("成绩自动上传失败");
            }

            @Override
            public void onRequestData(Object data) {

            }
        });
        subscriber.uploadResult(uploadResultsList);
    }

    /**
     * 上传单个成绩
     *
     * @param context       null 则不显示dialog
     * @param uploadResults
     */
    public static void uploadResult(final Context context, UploadResults uploadResults) {

        if (uploadResults == null)
            return;
        List<UploadResults> uploadResultsList = new ArrayList<>();
        uploadResultsList.add(uploadResults);
        uploadResult(context, uploadResultsList);
    }
}
