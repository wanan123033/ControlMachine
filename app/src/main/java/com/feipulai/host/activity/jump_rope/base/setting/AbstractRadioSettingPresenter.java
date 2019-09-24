package com.feipulai.host.activity.jump_rope.base.setting;

import android.content.Context;

import com.feipulai.host.R;

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
            view.showToast(context.getString(R.string.jump_setting_device_num_hint));
        }
    }

    @Override
    public void updateTestTime(int testTime) {
        if (testTime < 10) {
            view.showToast(context.getString(R.string.jump_setting_time_hint_1));
        } else if (testTime > 1000) {
            view.showToast(context.getString(R.string.jump_setting_time_hint_2));
        } else {
            setTestTime(testTime);
        }
    }

    @Override
    public void showJudgements() {
        view.showToast(context.getString(R.string.function_development_hint));
    }

    protected abstract void setDeviceSum(int deviceSum);

    protected abstract void setTestTime(int testTime);

    protected abstract int getTestTime();

    protected abstract int getDeviceSum();

    protected abstract int getMaxDeviceSum();

    protected abstract void saveSettings();

}
