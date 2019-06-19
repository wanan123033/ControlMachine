package com.feipulai.exam.activity.pullup.setting;

import android.os.Bundle;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;

public class PullUpSettingActivity extends AbstractRadioSettingActivity {

    private PullUpSetting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setting = SharedPrefsUtil.loadFormSource(this, PullUpSetting.class);
        if (setting.isCountless()){
            llTestTime.setVisibility(View.GONE);
            mSpDeviceNum.setSelection(0);
            mSpDeviceNum.setEnabled(false);
        }
    }

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new PullUpSettingPresenter(this, this);
    }

}
