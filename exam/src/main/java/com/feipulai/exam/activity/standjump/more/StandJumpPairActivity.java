package com.feipulai.exam.activity.standjump.more;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class StandJumpPairActivity
        extends SitPullPairActivity {
    public SitPullUpPairPresenter getPresenter() {
        return new StandJumpPairPresenter(this, this);
    }


}