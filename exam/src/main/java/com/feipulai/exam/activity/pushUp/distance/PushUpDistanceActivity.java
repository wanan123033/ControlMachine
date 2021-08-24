package com.feipulai.exam.activity.pushUp.distance;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckActivity;

public class PushUpDistanceActivity extends SitPullUpCheckActivity<PushUpDistanceSetting> {

    @Override
    protected int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected PushUpDistancePresenter getPresenter() {
        return new PushUpDistancePresenter(this,this);
    }

    @Override
    protected Class<?> getProjectSettingActivity() {
        return PushUpDistanceSettingActivity.class;
    }

    @Override
    protected Class<? extends Activity> getTestActivity() {
        return PushUpDistanceTestActivity.class;
    }

    @Override
    protected Class<? extends Activity> getPairActivity() {
        return PushUpDistancePairActivity.class;
    }
}
