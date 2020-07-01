package com.feipulai.exam.activity.grip.pair;

import com.feipulai.exam.activity.sitreach.more_device.pair.MorePairPresenter;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

public class GripPairActivity extends SitPullPairActivity {

    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new GripPairPresenter(this,this);
    }
}
