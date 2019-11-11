package com.feipulai.exam.activity.medicineBall.pair;

import com.feipulai.exam.activity.sargent_jump.pair.SargentPairPresenter;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class MedicineBallPairActivity extends SitPullPairActivity {


    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new MedicineBallPairPresenter(this,this);
    }
}
