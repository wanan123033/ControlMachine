package com.feipulai.exam.activity.volleyball.more_devices;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class VolleyBallPairActivity extends SitPullPairActivity {

    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new VolleyBallPairPresenter(this,this);
    }
}
