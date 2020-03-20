package com.feipulai.host.activity.standjump.more;

import com.feipulai.host.activity.situp.pair.BasePairActivity;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;

public class StandJumpPairActivity
        extends BasePairActivity {
    public BasePairPresenter getPresenter() {
        return new StandJumpPairPresenter(this, this);
    }


}