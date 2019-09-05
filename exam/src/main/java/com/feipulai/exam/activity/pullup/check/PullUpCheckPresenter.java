package com.feipulai.exam.activity.pullup.check;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;

public class PullUpCheckPresenter extends SitPullUpCheckPresenter<PullUpSetting> {

    private final PullUpManager deviceManager;
    private int endPosition;

    public PullUpCheckPresenter(Context context, RadioCheckContract.View<PullUpSetting> view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class);
        deviceManager = new PullUpManager();
    }

    @Override
    protected PullUpSetting getSetting() {
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
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency(originFrequency, deviceId, deviceFrequency);
    }

    @Override
    protected void endTest() {
        deviceManager.endTest(endPosition ++ % pairs.size());
    }

    @Override
    public void onGettingState(int position) {
        deviceManager.getState(position + 1);
    }

}
