package com.feipulai.host.activity.jump_rope.base.setting;

import android.content.Context;

public abstract class AbstractRadioSettingPresenter implements RadioSettingContract.Presenter {

    private Context context;
    private RadioSettingContract.View view;

    public AbstractRadioSettingPresenter(Context context, RadioSettingContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void start() {
        view.showMax(getMaxDeviceSum());
        view.showDeviceSum(getDeviceSum());
        view.showTestTime(getTestTime());
    }

    @Override
    public void updateDeviceSum(int deviceSum) {
        if (deviceSum > 0 && deviceSum <= getMaxDeviceSum()) {
            setDeviceSum(deviceSum);
        } else {
            view.showToast("无效的最大设备数");
        }
    }

    @Override
    public void updateTestTime(int testTime) {
        if (testTime < 10){
            view.showToast("测试时长不能小于10秒");
        }else if(testTime > 1000){
            view.showToast("测试时长不能大于1000秒");
        }else {
            setTestTime(testTime);
        }
    }

    @Override
    public void showJudgements(){
        view.showToast("功能开发中,敬请期待");
    }

    protected abstract void setDeviceSum(int deviceSum);

    protected abstract void setTestTime(int testTime);

    protected abstract int getTestTime();

    protected abstract int getDeviceSum();

    protected abstract int getMaxDeviceSum();

    protected abstract void saveSettings();

}
