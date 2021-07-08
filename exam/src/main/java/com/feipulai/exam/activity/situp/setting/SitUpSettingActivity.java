package com.feipulai.exam.activity.situp.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;

public class SitUpSettingActivity extends AbstractRadioSettingActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llTestMin.setVisibility(View.VISIBLE);
        llTestMax.setVisibility(View.VISIBLE);
        llTestLed.setVisibility(View.VISIBLE);
    }

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new SitUpSettingPresenter(this, this);
    }

}
