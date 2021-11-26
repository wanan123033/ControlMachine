package com.feipulai.exam.activity.pushUp.distance;

import com.feipulai.exam.activity.situp.base_test.SitPullUpTestActivity;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;

public class PushUpDistanceTestActivity extends SitPullUpTestActivity {
    @Override
    protected SitPullUpTestPresenter getPresenter() {
        return new PushUpDistanceTestPresenter(this,this);
    }
}
