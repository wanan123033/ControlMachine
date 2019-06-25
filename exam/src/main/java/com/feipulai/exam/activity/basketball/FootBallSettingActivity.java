package com.feipulai.exam.activity.basketball;

import android.view.View;

public class FootBallSettingActivity extends BasketBallSettingActivity {
    @Override
    protected void initData() {
        super.initData();
        llUseMode.setVisibility(View.VISIBLE);
    }
}
