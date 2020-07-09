package com.feipulai.exam.activity.grip.pair;


import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.GripManager;
import com.feipulai.exam.activity.grip.GripSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by pengjf on 2019/10/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripPairPresenter extends SitPullUpPairPresenter {
    private Context context;
    private GripSetting setting;
    private GripManager manager;

    public GripPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, GripSetting.class);
        manager =  new GripManager();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    protected int getDeviceSum() {
        return setting.getDeviceSum();
    }

    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        manager.setGrip(SettingHelper.getSystemSetting().getUseChannel(),
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }
}
