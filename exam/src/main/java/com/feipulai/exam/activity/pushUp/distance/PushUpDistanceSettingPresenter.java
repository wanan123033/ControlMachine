package com.feipulai.exam.activity.pushUp.distance;

import android.content.Context;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.feipulai.exam.activity.jump_rope.base.setting.RadioSettingContract;

public class PushUpDistanceSettingPresenter extends AbstractRadioSettingPresenter {
    private PushUpDistanceSetting setting;
    private Context context;
    public PushUpDistanceSettingPresenter(Context context, RadioSettingContract.View view) {
        super(context, view);
        this.context = context;
        this.setting = SharedPrefsUtil.loadFormSource(context,PushUpDistanceSetting.class);

    }

    @Override
    protected void setTestNo(int testNo) {
        setting.setTestNo(testNo);
    }

    @Override
    protected void setDeviceSum(int deviceSum) {
        setting.setDeviceSum(deviceSum);
    }

    @Override
    protected void setGroupMode(int groupMode) {
        setting.setGroupMode(groupMode);
    }

    @Override
    protected void setTestTime(int testTime) {
        setting.setTestTime(testTime);
    }

    @Override
    protected int getTestTime() {
        return setting.getTestTime();
    }

    @Override
    protected int getGroupMode() {
        return setting.getGroupMode();
    }

    @Override
    protected int getDeviceSum() {
        return setting.getDeviceSum();
    }

    @Override
    protected int getMaxDeviceSum() {
        return 28;
    }

    @Override
    protected void saveSettings() {
        Log.e("TAG","setting="+setting);
        SharedPrefsUtil.save(context, setting);
    }
}
