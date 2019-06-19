package com.feipulai.host.activity.jump_rope.setting;

import com.feipulai.host.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.host.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;

public class JumpRopeSettingActivity extends AbstractRadioSettingActivity {

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new JumpRopeSettingPresenter(this, this);
    }

}
