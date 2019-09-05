package com.feipulai.exam.activity.pushUp.check;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;

public class PushUpCheckPresenter extends SitPullUpCheckPresenter<PushUpSetting> {

    private final SitPushUpManager deviceManager;
    private int countForSetAngle = 20;

    public PushUpCheckPresenter(Context context, RadioCheckContract.View<PushUpSetting> view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, PushUpSetting.class);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_PUSH_UP, PushUpSetting.WIRELESS_TYPE);
    }

    @Override
    public void startTest() {
        if (setting.getTestTime() <= 0) {
            ToastUtils.showShort("请设置测试时限");
            return;
        }
        super.startTest();
    }

    @Override
    protected PushUpSetting getSetting() {
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
        deviceManager.setFrequency(ItemDefault.CODE_FWC,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    protected void endTest() {
        deviceManager.endTest();
    }

    @Override
    public void onGettingState(int position) {
        deviceManager.getState(position + 1, 0);
        if (countForSetAngle++ % 20 == 0) {
            deviceManager.setBaseline(SitPushUpManager.PROJECT_CODE_PUSH_UP, 0);
        }
    }

}
