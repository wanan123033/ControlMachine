package com.feipulai.exam.activity.pullup.test;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestContract;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;

public class PullUpTestPresenter extends SitPullUpTestPresenter<PullUpSetting> {

    private PullUpManager deviceManager;

    public PullUpTestPresenter(Context context, SitPullUpTestContract.View view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class);
        deviceManager = new PullUpManager();
    }

    @Override
    protected int getCountStartTime() {
        return PullUpManager.DEFAULT_COUNT_DOWN_TIME;
    }
    
    @Override
    protected int getCountFinishTime() {
        return 10;
    }
    
    @Override
    protected PullUpSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestTimeFromSetting() {
        return setting.getTestTime();
    }

    @Override
    protected void resetDevices() {
        for (StuDevicePair pair: pairs) {
            deviceManager.endTest(pair.getBaseDevice().getDeviceId());
        }
    }

    @Override
    protected int getGroupModeFromSetting() {
        return setting.getGroupMode();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency(originFrequency, deviceId, deviceFrequency);
    }

    @Override
    protected boolean canPenalize() {
        return setting.isPenalize();
    }

    @Override
    protected void testCountDown(long tick) {
        for (StuDevicePair pair: pairs) {
            deviceManager.startTest(pair.getBaseDevice().getDeviceId(),(int) tick, setting.getTestTime(),0);
        }
    }

    @Override
    public void onGettingState(int position) {
        deviceManager.getState(position + 1);
    }

}
