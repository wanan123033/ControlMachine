package com.feipulai.exam.activity.situp.newSitUp;

import com.feipulai.exam.activity.situp.base_test.SitPullUpTestActivity;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;

public class NewSitUpTestActivity extends SitPullUpTestActivity<SitUpSetting> {


    @Override
    protected SitPullUpTestPresenter<SitUpSetting> getPresenter() {
        return new NewSitUpTestPresenter(this, this);
    }
}
