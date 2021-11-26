package com.feipulai.exam.activity.pushUp.distance;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestContract;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;

public class PushUpDistanceTestPresenter extends SitPullUpTestPresenter<PushUpDistanceSetting> {
    private Context context;
    private PushUpDistanceSetting setting;
    private SitPushUpManager deviceManager;
    protected PushUpDistanceTestPresenter(Context context, SitPullUpTestContract.View view) {
        super(context, view);
        this.context = context;
        this.setting = SharedPrefsUtil.loadFormSource(context,PushUpDistanceSetting.class);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_PUSH_UP);
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency(ItemDefault.CODE_YWQZ,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    protected boolean canPenalize() {
        return setting.isPenalize();
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
    protected PushUpDistanceSetting getSetting() {
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
    protected void testCountDown(long tick) {
        deviceManager.startTest((int) tick, setting.getTestTime());
    }

    @Override
    public void onGettingState(int position) {
        BaseDeviceState deviceState = pairs.get(position).getBaseDevice();
        deviceManager.getState(deviceState.getDeviceId(),setting.getAngle());
    }
}
