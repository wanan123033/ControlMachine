package com.feipulai.host.activity.vccheck.pair;

import com.feipulai.host.activity.situp.pair.BasePairActivity;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;

public class VcPairActivity extends BasePairActivity {


    @Override
    public BasePairPresenter getPresenter() {
        return new VcPairPresenter(this,this);
//        return new NewVCPairPresenter(this,this);
    }
}
