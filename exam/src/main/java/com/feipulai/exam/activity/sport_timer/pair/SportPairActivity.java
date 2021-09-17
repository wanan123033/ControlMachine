package com.feipulai.exam.activity.sport_timer.pair;

import android.view.View;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.config.TestConfigs;

public class SportPairActivity extends SitPullPairActivity {


    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new SportTimerPairPresenter(this,this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setInitWayVisible();
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ){
            txtInitWay.setVisibility(View.GONE);
        }
    }
}
