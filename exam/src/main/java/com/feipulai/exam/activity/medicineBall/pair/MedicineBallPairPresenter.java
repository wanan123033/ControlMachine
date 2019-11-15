package com.feipulai.exam.activity.medicineBall.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.medicineBall.MedicineBallSetting;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class MedicineBallPairPresenter extends SitPullUpPairPresenter {

    private Context context;
    private MedicineBallSetting setting;
    private SitPushUpManager sitPushUpManager;

    public MedicineBallPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, MedicineBallSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SXQ,PushUpSetting.WIRELESS_TYPE);
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    protected int getDeviceSum() {
        return setting.getSpDeviceCount();
    }

    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }
    
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        sitPushUpManager.setFrequencySXQ(
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context,setting);
    }

}
