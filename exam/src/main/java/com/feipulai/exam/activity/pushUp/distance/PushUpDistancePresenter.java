package com.feipulai.exam.activity.pushUp.distance;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;

public class PushUpDistancePresenter extends SitPullUpCheckPresenter<PushUpDistanceSetting> {
    private PushUpDistanceSetting setting;
    private SitPushUpManager deviceManager;
    public PushUpDistancePresenter(Context context, RadioCheckContract.View<PushUpDistanceSetting> view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context,PushUpDistanceSetting.class);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_PUSH_UP);
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency( deviceFrequency,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    protected PushUpDistanceSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestPattern() {
        return setting.getGroupMode();
    }

    @Override
    protected int getDeviceSumFromSetting() {
        return setting.getDeviceSum();
    }

    @Override
    protected void endTest() {
        deviceManager.endTest();
    }

    @Override
    public void onGettingState(int position) {
        deviceManager.getState(position + 1,setting.getAngle());
    }
}
