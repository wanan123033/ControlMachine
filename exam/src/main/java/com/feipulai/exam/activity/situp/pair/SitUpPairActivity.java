package com.feipulai.exam.activity.situp.pair;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class SitUpPairActivity extends SitPullPairActivity {

    public SitPullUpPairPresenter getPresenter(){
        return new SitUpPairPresenter(this, this);
    }

}
