package com.feipulai.exam.activity.pullup.check;

import android.app.Activity;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.pullup.pair.PullUpPairActivity;
import com.feipulai.exam.activity.pullup.setting.PullUpSettingActivity;
import com.feipulai.exam.activity.pullup.test.PullUpTestActivity;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckActivity;

public class PullUpCheckActivity extends SitPullUpCheckActivity {

    @Override
    protected int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected PullUpCheckPresenter getPresenter() {
        return new PullUpCheckPresenter(this, this);
    }
    
    @Override
    protected Class<?> getProjectSettingActivity() {
        return PullUpSettingActivity.class;
    }
    
    @Override
    protected Class<? extends Activity> getTestActivity() {
        return PullUpTestActivity.class;
    }
    
    @Override
    protected Class<? extends Activity> getPairActivity() {
        return PullUpPairActivity.class;
    }
    
}
