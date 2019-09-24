package com.feipulai.host.activity.situp.test;

import com.feipulai.host.activity.jump_rope.base.test.AbstractRadioTestActivity;
import com.feipulai.host.activity.situp.setting.SitUpSetting;

public class SitUpTestActivity
        extends AbstractRadioTestActivity<SitUpSetting>
        implements SitUpTestContract.View<SitUpSetting> {


    @Override
    protected SitUpTestPresenter getPresenter() {
        return new SitUpTestPresenter(this, this);
    }


    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
    }


}
