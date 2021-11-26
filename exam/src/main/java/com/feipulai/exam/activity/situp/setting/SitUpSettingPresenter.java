package com.feipulai.exam.activity.situp.setting;

import android.content.Context;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.jump_rope.base.setting.RadioSettingContract;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.orhanobut.logger.Logger;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SitUpSettingPresenter extends AbstractRadioSettingPresenter {

    private SitUpSetting setting;
    private Context context;

    public SitUpSettingPresenter(Context context, RadioSettingContract.View view) {
        super(context, view);
        this.context = context;
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);

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
        SharedPrefsUtil.save(context, setting);
        Logger.i("situp setting changed:" + setting.toString());
    }

    public void setLedShow(boolean show){
        setting.setShowLed(show);
    }

}
