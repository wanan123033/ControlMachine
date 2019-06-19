package com.feipulai.exam.activity.pushUp.test;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestContract;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;

public class PushUpTestPresenter extends SitPullUpTestPresenter<PushUpSetting> {

    private SitPushUpManager deviceManager;

    public PushUpTestPresenter(Context context, SitPullUpTestContract.View view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, PushUpSetting.class);

        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_PUSH_UP,PushUpSetting.WIRELESS_TYPE);
    }

    @Override
    protected int getCountStartTime() {
        return 5;
    }

    @Override
    protected int getCountFinishTime() {
        return 10;
    }

    @Override
    protected PushUpSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestTimeFromSetting() {
        return setting.getTestTime();
    }

    @Override
    protected void resetDevices() {
        deviceManager.endTest();
    }

    @Override
    protected int getGroupModeFromSetting() {
        return setting.getGroupMode();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency(ItemDefault.CODE_YWQZ,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    protected void testCountDown(long tick) {
        deviceManager.startTest((int) tick, setting.getTestTime());
    }

    @Override
    protected boolean canPenalize() {
        return false;
    }

    @Override
    public void onGettingState(int position) {
        BaseDeviceState deviceState = pairs.get(position).getBaseDevice();
        deviceManager.getState(deviceState.getDeviceId(), 0);
    }

}
