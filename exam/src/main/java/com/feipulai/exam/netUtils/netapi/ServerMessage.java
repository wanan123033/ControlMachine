package com.feipulai.exam.netUtils.netapi;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.LoadingDialog;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForGroupActivity;
import com.feipulai.exam.activity.MiddleDistanceRace.MiddleDistanceRaceForPersonActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.ScheduleBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.netUtils.CommonUtils;
import com.feipulai.exam.netUtils.HttpManager;
import com.feipulai.exam.netUtils.URLConstant;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.feipulai.exam.config.EventConfigs.REQUEST_UPLOAD_LOG_E;
import static com.feipulai.exam.config.EventConfigs.REQUEST_UPLOAD_LOG_S;

/**
 * Created by zzs on  2019/1/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ServerMessage {
    static List<Item> itemList = null;
    static int position = 0;

    /**
     * 下载数据
     *
     * @param context
     */
    public static void downloadData(final Context context, final int examType, final String downTime) {

        itemList = null;
        final HttpSubscriber subscriber = new HttpSubscriber();
        subscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onRequestData(Object data) {

            }

            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.SCHEDULE_BIZ://日程
                        subscriber.getItemAll(context);
                        break;
                    case HttpSubscriber.ITEM_BIZ://项目
                        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZCP) {
                            itemList = DBManager.getInstance().queryItemsByMachineCode(ItemDefault.CODE_ZCP);
                        }

                        if (itemList != null) {
                            for (int i = 0; i < itemList.size(); i++) {
                                if (!TextUtils.isEmpty(itemList.get(i).getItemCode())) {
                                    subscriber.getItemStudent(downTime, itemList.get(i).getItemCode(), 1, examType);
                                    position = i + 1;
                                    return;
                                }
                            }
                        } else {
                            subscriber.getItemStudent(downTime, TestConfigs.getCurrentItemCode(), 1, examType);
                        }
                        break;
                    case HttpSubscriber.STUDENT_BIZ://学生
                        if (itemList != null) {
                            if (position < itemList.size()) {
                                subscriber.getItemStudent(downTime, itemList.get(position).getItemCode(), 1, examType);
                                position++;
                                return;
                            }
                        }
                        if (ScheduleBean.SITE_EXAMTYPE == 1) {
                            if (itemList != null) {
                                for (int i = 0; i < itemList.size(); i++) {
                                    if (!TextUtils.isEmpty(itemList.get(i).getItemCode())) {
                                        subscriber.getItemGroupAll(itemList.get(i).getItemCode(), "", 1, examType);
                                        position = i + 1;
                                        return;
                                    }
                                }

                            } else {
                                subscriber.getItemGroupAll(TestConfigs.getCurrentItemCode(), "", 1, examType);
                            }

                        } else {
                            EventBus.getDefault().post(new BaseEvent(EventConfigs.DATA_DOWNLOAD_SUCCEED));
                        }
                        break;
                    case HttpSubscriber.GROUP_BIZ://分组
                        if (itemList != null) {
                            if (position < itemList.size()) {
                                subscriber.getItemGroupAll(itemList.get(position).getItemCode(), "", 1, examType);
                                position++;
                                return;
                            }
                        }
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

//        Logger.i("uploadResult==>" + uploadResultsList.toString());
        if (uploadResultsList == null || uploadResultsList.size() == 0) {
            ToastUtils.showShort("没有需要上传的成绩");
            return;
        }
//        if (SettingHelper.getSystemSetting().isTCP()) {
//            uploadTCPResult(context, uploadResultsList);
//            return;
//        }
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
            public void onRequestData(Object data) {

            }

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
                        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPLOAD_RESULT_SUCCEED));
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                if (loadingDialog != null && loadingDialog.isShow()) {
                    loadingDialog.dismissDialog();
                }
                EventBus.getDefault().post(new BaseEvent(EventConfigs.UPLOAD_RESULT_FAULT));
            }
        });
        subscriber.uploadResult(uploadResultsList);
//        subscriber.getScheduleAll();
    }

    /**
     * 上传成绩
     *
     * @param context
     * @param uploadResultsList
     */
    private static void uploadZCPResult(final Context context, final String itemName, final List<UploadResults> uploadResultsList) {

        if (SettingHelper.getSystemSetting().isTCP()) {
            uploadTCPResult(context, uploadResultsList);
            if (!SettingHelper.getSystemSetting().isTCPSimultaneous()) {
                return;
            }

        }
//        Logger.d("uploadZCPResult==>"+uploadResultsList.toString());

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
            public void onRequestData(Object data) {

            }

            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.SCHEDULE_BIZ://日程
                        subscriber.getItemAll(context);
                        break;
                    case HttpSubscriber.ITEM_BIZ://项目
                        Item item = DBManager.getInstance().queryItemByName(itemName);
                        if (item != null) {
                            //更新项目代码
                            for (UploadResults uploadResults : uploadResultsList) {
                                uploadResults.setExamItemCode(item.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : item.getItemCode());
                                for (RoundResultBean resultBean : uploadResults.getRoundResultList()) {
                                    resultBean.setItemCode(item.getItemCode());
                                }
                            }
                        }
                        subscriber.uploadResult(uploadResultsList);
                        break;
                    case HttpSubscriber.UPLOAD_BIZ://上传成绩
                        if (loadingDialog != null && loadingDialog.isShow()) {
                            loadingDialog.dismissDialog();
                        }
                        if (context instanceof MiddleDistanceRaceForGroupActivity) {
                            MiddleDistanceRaceForGroupActivity.instance.refreshItemList();
                        } else if (context instanceof MiddleDistanceRaceForPersonActivity) {
                            MiddleDistanceRaceForPersonActivity.instance.refreshItemList();
                        }
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                if (loadingDialog != null && loadingDialog.isShow()) {
                    loadingDialog.dismissDialog();
                }
                if (context instanceof MiddleDistanceRaceForGroupActivity) {
                    MiddleDistanceRaceForGroupActivity.instance.refreshItemList();
                } else if (context instanceof MiddleDistanceRaceForPersonActivity) {
                    MiddleDistanceRaceForPersonActivity.instance.refreshItemList();
                }
            }
        });
//        subscriber.getScheduleAll();
        subscriber.uploadResult(uploadResultsList);
    }

    /**
     * 自动上传成绩,不处理没有项目代码等(处理项目代码等可能会导致 项目代码 变更,在测试过程中不应该出现这种情况)
     */
    public static void uploadResult(final List<UploadResults> uploadResultsList) {
//        Logger.i("自动上传成绩:" + uploadResultsList.toString());

        if (uploadResultsList == null || uploadResultsList.size() == 0) {
            ToastUtils.showShort("没有需要上传的成绩");
            return;
        }
        final HttpSubscriber subscriber = new HttpSubscriber();
        subscriber.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onRequestData(Object data) {

            }

            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.UPLOAD_BIZ://上传成绩
                        Logger.i("成绩自动上传成功");
                        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPLOAD_RESULT_SUCCEED));
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                EventBus.getDefault().post(new BaseEvent(EventConfigs.UPLOAD_RESULT_FAULT));
                Logger.i("成绩自动上传失败");
            }
        });
        if (SettingHelper.getSystemSetting().isTCP()) {
            subscriber.uploadResultTCP(null, uploadResultsList);
            if (SettingHelper.getSystemSetting().isTCPSimultaneous()) {
                subscriber.uploadResult(uploadResultsList);
            }
        } else {
            subscriber.uploadResult(uploadResultsList);
        }
    }

    public static HttpSubscriber subscriber0;

    /**
     * tcp上传所有成绩
     *
     * @param context
     * @param uploadResultsList
     */
    private static void uploadTCPResult(Context context, final List<UploadResults> uploadResultsList) {
        if (uploadResultsList == null || uploadResultsList.size() == 0) {
            ToastUtils.showShort("没有需要上传的成绩");
            return;
        }
        Logger.i("TCP上传成绩:" + uploadResultsList.toString());

        final LoadingDialog loadingDialog;
        if (context != null) {
            loadingDialog = new LoadingDialog(context);
            loadingDialog.showDialog("成绩上传中，请稍后...", true);
        } else {
            loadingDialog = null;
        }

        subscriber0 = new HttpSubscriber();
        subscriber0.setOnRequestEndListener(new HttpSubscriber.OnRequestEndListener() {
            @Override
            public void onRequestData(Object data) {

            }

            @Override
            public void onSuccess(int bizType) {
                switch (bizType) {
                    case HttpSubscriber.UPLOAD_BIZ://上传成绩
                        Logger.i("成绩上传成功");
                        if (loadingDialog != null && loadingDialog.isShow()) {
                            loadingDialog.dismissDialog();
                        }
                        break;
                }
            }

            @Override
            public void onFault(int bizType) {
                Logger.i("成绩上传失败");
                if (loadingDialog != null && loadingDialog.isShow()) {
                    loadingDialog.dismissDialog();
                }
            }
        });
        subscriber0.uploadResultTCP((Activity) context, uploadResultsList);
    }

    /**
     * 上传单个成绩
     *
     * @param context       null 则不显示dialog
     * @param uploadResults
     */
    private static void uploadResult(final Context context, UploadResults uploadResults) {

        if (uploadResults == null)
            return;
        List<UploadResults> uploadResultsList = new ArrayList<>();
        uploadResultsList.add(uploadResults);
        uploadResult(context, uploadResultsList);
    }


    public static void baseUploadResult(final Context context, UploadResults uploadResults) {
        if (uploadResults == null)
            return;
        List<UploadResults> uploadResultsList = new ArrayList<>();
        uploadResultsList.add(uploadResults);
        if (SettingHelper.getSystemSetting().isTCP()) {
            uploadTCPResult(context, uploadResultsList);
            if (SettingHelper.getSystemSetting().isTCPSimultaneous()) {
                uploadResult(null, uploadResultsList);
            }
        } else {
            uploadResult(context, uploadResultsList);
        }
    }

    public static void baseUploadResult(final Context context, final List<UploadResults> uploadResultsList) {
        if (uploadResultsList == null || uploadResultsList.size() == 0) {
            ToastUtils.showShort("没有需要上传的成绩");
            return;
        }
        if (SettingHelper.getSystemSetting().isTCP()) {
            uploadTCPResult(context, uploadResultsList);
            if (SettingHelper.getSystemSetting().isTCPSimultaneous()) {
                uploadResult(null, uploadResultsList);
            }
        } else {
            uploadResult(context, uploadResultsList);
        }
    }

    public static void uploadLogFile(String fileName, String zipPathName) {

        File file = new File(zipPathName);
        final OkHttpClient client = new OkHttpClient.Builder().build();
        final MediaType mediaType = MediaType.parse("multipart/form-data;boundary=params");
        String itemCode = TestConfigs.sCurrentItem.getItemCode();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("logList[0].itemCode",TextUtils.isEmpty(itemCode)?"default":itemCode)
                .addFormDataPart("logList[0].itemName",TestConfigs.sCurrentItem.getItemName())
                .addFormDataPart("logList[0].logTime",System.currentTimeMillis()+"")
                .addFormDataPart("logList[0].hostNo",SettingHelper.getSystemSetting().getHostId()+"")
                .addFormDataPart("logList[0].fileName",fileName+".zip")
                .addFormDataPart("logList[0].msEquipment", CommonUtils.getDeviceId(MyApplication.getInstance()))
                .addFormDataPart("logList[0].file",fileName+".zip", RequestBody.create(MediaType.parse("application/octet-stream"),
                        file)).build();
        final String url = URLConstant.BASE_URL+"/run/terminalLogBackUp";
        final Request request = new Request.Builder().url(url)
                .method("POST", body)
                .addHeader("Content-Type", "multipart/form-data;boundary=params")
                .addHeader("Authorization", "bearer " + MyApplication.TOKEN) .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Logger.i(e.getMessage()+"失败");
                        EventBus.getDefault().post(new BaseEvent(REQUEST_UPLOAD_LOG_E));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String body = response.body().string();
                            Logger.i(body+response.code()+"返回"+url);
                            if (body.contains("备份成功")){
                                EventBus.getDefault().post(new BaseEvent(REQUEST_UPLOAD_LOG_S));
                            }else {
                                EventBus.getDefault().post(new BaseEvent(REQUEST_UPLOAD_LOG_E));
                            } } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
            }
        }).start();
    }
}
