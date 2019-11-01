package com.feipulai.exam.activity.basketball.wiress;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class BasketBallPairActivity extends SitPullPairActivity {


    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new BasketBallPairPresenter(this,this);
    }
}