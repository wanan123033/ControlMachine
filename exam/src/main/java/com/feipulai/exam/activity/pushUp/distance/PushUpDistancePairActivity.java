package com.feipulai.exam.activity.pushUp.distance;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class PushUpDistancePairActivity extends SitPullPairActivity {
    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new PushUpDistancePairPresenter(this,this);
    }
}
