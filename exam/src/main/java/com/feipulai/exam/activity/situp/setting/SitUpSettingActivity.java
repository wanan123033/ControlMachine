package com.feipulai.exam.activity.situp.setting;

import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;

public class SitUpSettingActivity extends AbstractRadioSettingActivity {

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new SitUpSettingPresenter(this, this);
    }

}
