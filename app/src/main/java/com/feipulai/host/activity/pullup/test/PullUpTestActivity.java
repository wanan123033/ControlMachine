package com.feipulai.host.activity.pullup.test;

import com.feipulai.host.activity.jump_rope.base.test.AbstractRadioTestActivity;
import com.feipulai.host.activity.pullup.setting.PullUpSetting;
import com.feipulai.host.activity.situp.setting.SitUpSetting;
import com.feipulai.host.activity.situp.test.SitUpTestContract;
import com.feipulai.host.activity.situp.test.SitUpTestPresenter;

public class PullUpTestActivity
        extends AbstractRadioTestActivity<PullUpSetting>
        implements PullUpTestContract.View<PullUpSetting> {


    @Override
    protected PullUpTestPresenter getPresenter() {
        return new PullUpTestPresenter(this, this);
    }


    @Override
    public void showToast(String msg) {
        toastSpeak(msg);
    }


}
