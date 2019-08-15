package com.feipulai.exam.activity.jump_rope.base.setting;

import android.content.Context;
import android.os.Build;

import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import java.util.Objects;

public abstract class AbstractRadioSettingPresenter implements RadioSettingContract.Presenter {

    private Context context;
    private RadioSettingContract.View view;

    public AbstractRadioSettingPresenter(Context context, RadioSettingContract.View view) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        this.view = view;
        Logger.i("entering setting,system setting:" + SettingHelper.getSystemSetting().toString());
        // view.setPresenter(this);
    }

    @Override
    public void start() {
        view.showMax(TestConfigs.MAX_TEST_NO, getMaxDeviceSum());
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
            view.disableGroupSetting();
        } else {
            view.showGroupMode(getGroupMode());
        }

        int testNo = TestConfigs.sCurrentItem.getTestNum();
        if (testNo > 0) {
            // 数据库中已经指定了测试次数,就不能再设置了
            view.disableTestNoSetting();
        }
        testNo = TestConfigs.getMaxTestCount(context);

        view.showTestNo(testNo);
        view.showDeviceSum(getDeviceSum());
        view.showTestTime(getTestTime());
    }

    @Override
    public void updateTestNo(int testNo) {
        if (testNo > 0 && testNo <= TestConfigs.MAX_TEST_NO) {
            setTestNo(testNo);
            // Log.i("james", "updateTestNo():" + testNo);
        } else {
            view.showToast("无效的测试次数");
        }
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
    public void updateGroupMode(int groupMode) {
        if (groupMode == TestConfigs.GROUP_PATTERN_LOOP
                || groupMode == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
            setGroupMode(groupMode);
        }else{
            view.showToast("无效的分组测试模式");
        }
    }


    @Override
    public void updateTestTime(int testTime) {
        if (testTime < 10){
            view.showToast("测试时长不能小于10秒");
        }else if(testTime > 3600){
            view.showToast("测试时长不能大于3600秒");
        }else {
            setTestTime(testTime);
        }
    }

    @Override
    public void showJudgements(){
        view.showToast("功能开发中,敬请期待");
    }

    protected abstract void setTestNo(int testNo);

    protected abstract void setDeviceSum(int deviceSum);

    protected abstract void setGroupMode(int groupMode);

    protected abstract void setTestTime(int testTime);

    protected abstract int getTestTime();

    protected abstract int getGroupMode();

    protected abstract int getDeviceSum();

    protected abstract int getMaxDeviceSum();

    protected abstract void saveSettings();

}
