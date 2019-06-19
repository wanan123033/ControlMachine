package com.feipulai.exam.netUtils.netapi;

import android.content.Context;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.bean.ScheduleBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.view.LoadingDialog;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzs on  2019/1/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ServerMessage {
    /**
     * 下载数据
     *
     * @param context
     */
    public static void downloadData(final Context context) {
        final HttpSubscriber subscriber = new HttpSubscriber();
        subscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.SCHEDULE_BIZ://日程
                        subscriber.getItemAll(context);
                        break;
                    case HttpSubscriber.ITEM_BIZ://项目
                        subscriber.getItemStudent();
                        break;
                    case HttpSubscriber.STUDENT_BIZ://学生
                        if (ScheduleBean.SITE_EXAMTYPE == 1) {
                            subscriber.getItemGroupAll();
                        } else {
                            EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_SUCCEED));
                        }
                        break;
                    case HttpSubscriber.GROUP_BIZ://分组
                        EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_SUCCEED));
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {

            }
        });
        subscriber.getScheduleAll();
    }

    /**
     * 上传成绩
     *
     * @param context
     * @param uploadResultsList
     */
    public static void uploadResult(final Context context, final List<UploadResults> uploadResultsList) {
        Logger.i("uploadResult==>" + uploadResultsList.toString());
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
        final HttpSubscriber subscriber = new HttpSubscriber();
        subscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.SCHEDULE_BIZ://日程
                        subscriber.getItemAll(context);
                        break;
                    case HttpSubscriber.ITEM_BIZ://项目
                        //更新项目代码
                        for (UploadResults uploadResults : uploadResultsList) {
                            uploadResults.setExamItemCode(TestConfigs.getCurrentItemCode());
                        }
                        subscriber.uploadResult(uploadResultsList);
                        break;
                    case HttpSubscriber.UPLOAD_BIZ://上传成绩
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
        });
        subscriber.getScheduleAll();
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
        final HttpSubscriber subscriber = new HttpSubscriber();
        subscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.UPLOAD_BIZ://上传成绩
                        Logger.i("成绩自动上传成功");
                        break;
                }
            }
            
            @Override
            public void onFault(int bizType) {
                Logger.i("成绩自动上传失败");
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