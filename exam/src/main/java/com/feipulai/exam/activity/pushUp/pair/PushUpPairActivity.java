package com.feipulai.exam.activity.pushUp.pair;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class PushUpPairActivity extends SitPullPairActivity {

    public SitPullUpPairPresenter getPresenter(){
        return new PushUpPairPresenter(this, this);
    }

}
