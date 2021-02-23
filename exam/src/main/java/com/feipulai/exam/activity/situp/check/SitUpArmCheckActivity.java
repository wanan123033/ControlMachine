package com.feipulai.exam.activity.situp.check;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckActivity;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;
import com.feipulai.exam.activity.situp.pair.SitUpPairActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSettingActivity;
import com.feipulai.exam.activity.situp.test.SitUpTestActivity;

public class SitUpArmCheckActivity extends SitPullUpCheckActivity {

    @Override
    protected int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceVisible();
    }
}
