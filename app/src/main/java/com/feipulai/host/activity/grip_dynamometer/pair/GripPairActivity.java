package com.feipulai.host.activity.grip_dynamometer.pair;

import com.feipulai.host.activity.situp.pair.BasePairActivity;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;
import com.feipulai.host.activity.vccheck.pair.VcPairPresenter;

public class GripPairActivity extends BasePairActivity {


    @Override
    public BasePairPresenter getPresenter() {
        return new GripPairPresenter(this,this);
    }
}
