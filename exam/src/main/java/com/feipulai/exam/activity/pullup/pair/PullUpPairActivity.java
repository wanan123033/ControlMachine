package com.feipulai.exam.activity.pullup.pair;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;

public class PullUpPairActivity extends SitPullPairActivity {

    public PullUpPairPresenter getPresenter(){
        return new PullUpPairPresenter(this, this);
    }

}
