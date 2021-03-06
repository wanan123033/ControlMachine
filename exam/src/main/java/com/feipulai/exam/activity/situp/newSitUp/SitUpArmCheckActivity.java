package com.feipulai.exam.activity.situp.newSitUp;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckActivity;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;
import com.feipulai.exam.activity.situp.check.SitUpCheckPresenter;
import com.feipulai.exam.activity.situp.pair.SitUpPairActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSettingActivity;
import com.feipulai.exam.activity.situp.test.SitUpTestActivity;

public class SitUpArmCheckActivity extends SitPullUpCheckActivity {

    @Override
    protected int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected NewSitUpCheckPresenter getPresenter() {
        return new NewSitUpCheckPresenter(this, this);
    }

    @Override
    protected Class<?> getProjectSettingActivity() {
        return SitUpSettingActivity.class;
    }

    @Override
    protected Class<? extends Activity> getTestActivity() {
        return  NewSitUpTestActivity.class;
    }

    @Override
    protected Class<? extends Activity> getPairActivity() {
        return NewSitUpPairActivity.class;
    }
    @Override
    protected void onResume() {
        super.onResume();
        setDeviceVisible();
    }

    public void getView(){

    }
}
