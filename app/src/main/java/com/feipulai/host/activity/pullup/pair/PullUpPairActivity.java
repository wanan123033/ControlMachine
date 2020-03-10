package com.feipulai.host.activity.pullup.pair;

import com.feipulai.host.activity.situp.pair.BasePairActivity;

public class PullUpPairActivity extends BasePairActivity {


    @Override
    public PullUpPairPresenter getPresenter() {
        return new PullUpPairPresenter(this,this);
    }
}
