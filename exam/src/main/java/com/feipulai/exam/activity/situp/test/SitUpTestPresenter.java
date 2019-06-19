package com.feipulai.exam.activity.situp.test;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestContract;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;

public class SitUpTestPresenter extends SitPullUpTestPresenter<SitUpSetting> {

    private SitPushUpManager deviceManager;

    public SitUpTestPresenter(Context context, SitPullUpTestContract.View view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
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
    protected SitUpSetting getSetting() {
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
        return setting.isPenalize();
    }

    @Override
    public void onGettingState(int position) {
        BaseDeviceState deviceState = pairs.get(position).getBaseDevice();
        deviceManager.getState(deviceState.getDeviceId(),setting.getAngle());
    }

}
