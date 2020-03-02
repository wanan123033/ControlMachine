package com.feipulai.host.activity.pullup.setting;

import android.content.Context;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.feipulai.host.activity.jump_rope.base.setting.RadioSettingContract;
import com.orhanobut.logger.Logger;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PullUpSettingPresenter extends AbstractRadioSettingPresenter {

    private PullUpSetting setting;
    private Context context;

    public PullUpSettingPresenter(Context context, RadioSettingContract.View view) {
        super(context, view);
        this.context = context;
        setting = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class);
        Logger.i("entering PullUpSetting:" + setting.toString());
    }



    @Override
    protected void setDeviceSum(int deviceSum) {
        setting.setDeviceSum(deviceSum);
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
    protected int getDeviceSum() {
        return setting.getDeviceSum();
    }

    @Override
    protected int getMaxDeviceSum() {
        return 5;
    }

    @Override
    protected void saveSettings() {
        SharedPrefsUtil.save(context, setting);
        Logger.i("pullup setting changed:" + setting.toString());
    }

}
