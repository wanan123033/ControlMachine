package com.feipulai.exam.activity.pushUp.distance;

import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;

public class PushUpDistanceSettingActivity extends AbstractRadioSettingActivity {
    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new PushUpDistanceSettingPresenter(this,this);
    }
}
