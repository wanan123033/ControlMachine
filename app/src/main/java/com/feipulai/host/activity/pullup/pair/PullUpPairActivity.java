package com.feipulai.host.activity.pullup.pair;

import com.feipulai.host.activity.situp.pair.BasePairActivity;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;

public class PullUpPairActivity extends BasePairActivity {


    @Override
    public BasePairPresenter getPresenter() {
        return new PullUpPairPresenter(this,this);
    }
}
