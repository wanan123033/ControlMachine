package com.feipulai.exam.activity.situp.check;

import android.app.Activity;

import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckActivity;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;
import com.feipulai.exam.activity.situp.pair.SitUpPairActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSettingActivity;
import com.feipulai.exam.activity.situp.test.SitUpTestActivity;

public class SitUpCheckActivity extends SitPullUpCheckActivity {

    @Override
    protected SitPullUpCheckPresenter getPresenter() {
        return new SitUpCheckPresenter(this, this);
    }
    
    @Override
    protected Class<?> getProjectSettingActivity() {
        return SitUpSettingActivity.class;
    }
    
    @Override
    protected Class<? extends Activity> getTestActivity() {
        return  SitUpTestActivity.class;
    }
    
    @Override
    protected Class<? extends Activity> getPairActivity() {
        return SitUpPairActivity.class;
    }

}
