package com.feipulai.host.activity.situp.pair;

public class SitPullPairActivity extends BasePairActivity {


    @Override
    public SitUpPairPresenter getPresenter() {
        return new SitUpPairPresenter(this,this);
    }
}
