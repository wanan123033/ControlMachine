package com.feipulai.exam.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.netUtils.netapi.ServerMessage;

/**
 * Created by zzs on  2019/2/18
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class UploadService extends Service {


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getExtras()!=null){
            UploadResults uploadResults = (UploadResults) intent.getExtras().getSerializable(UploadResults.BEAN_KEY);

            uploadResult(uploadResults);
        }
        return START_REDELIVER_INTENT;
    }
    /**
     * 成绩上传
     *
     * @param uploadResults 上传成绩
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("自动上传成绩需下载更新项目信息");
            return;
        }
        ServerMessage.baseUploadResult(null, uploadResults);

    }
}
