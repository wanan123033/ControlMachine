package com.feipulai.exam.activity.ranger;


import android.content.Intent;

import com.feipulai.exam.activity.person.BasePersonTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;

public class RangerTestActivity extends BasePersonTestActivity {

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {

    }

    @Override
    public int setTestCount() {
        return 2;
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this,RangerSettingActivity.class));
    }

    @Override
    public void stuSkip() {

    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }
}
