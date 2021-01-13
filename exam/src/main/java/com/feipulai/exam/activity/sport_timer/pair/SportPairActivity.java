package com.feipulai.exam.activity.sport_timer.pair;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class SportPairActivity extends SitPullPairActivity {


    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new SportTimerPairPresenter(this,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInitWayVisible();
    }
}
