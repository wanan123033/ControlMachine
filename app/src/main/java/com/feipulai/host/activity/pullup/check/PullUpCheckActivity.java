package com.feipulai.host.activity.pullup.check;

import android.app.Activity;

import com.feipulai.host.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.host.activity.pullup.pair.PullUpPairActivity;
import com.feipulai.host.activity.pullup.setting.PullUpSettingActivity;

public class PullUpCheckActivity extends BasePullUpActivity {


    @Override
    protected RadioCheckContract.Presenter getPresenter() {
        return new PullUpCheckPresenter(this, this);
    }

    @Override
    protected Class<?> getProjectSettingActivity() {
        return PullUpSettingActivity.class;
    }

    @Override
    protected Class<? extends Activity> getTestActivity() {
        return null;
    }

    @Override
    protected Class<? extends Activity> getPairActivity() {
        return PullUpPairActivity.class;
    }
}
