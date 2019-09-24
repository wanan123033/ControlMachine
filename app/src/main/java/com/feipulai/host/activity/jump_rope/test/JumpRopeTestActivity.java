package com.feipulai.host.activity.jump_rope.test;


import android.view.View;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.base.test.AbstractRadioTestActivity;
import com.feipulai.host.activity.jump_rope.base.test.AbstractRadioTestPresenter;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.setting.JumpRopeSetting;

import java.util.List;

public class JumpRopeTestActivity
        extends AbstractRadioTestActivity<JumpRopeSetting> {

    @Override
    public void initView(List<StuDevicePair> pairs, JumpRopeSetting setting) {
        llDeviceGroup.setVisibility(View.VISIBLE);
        tvGroup.setText(String.format(getString(R.string.group_name), SerialConfigs.GROUP_NAME[setting.getDeviceGroup()]));
        super.initView(pairs, setting);
    }

    @Override
    protected AbstractRadioTestPresenter getPresenter() {
        return new JumpRopeTestPresenter(this, this);
    }

}
