package com.feipulai.exam.activity.sargent_jump.more_device;

import com.feipulai.exam.activity.person.BaseStuPair;

public class SargentMoreTestActivity extends SargentJumpMoreActivity {


    @Override
    public int setTestCount() {
        return 4;
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }

    @Override
    public void gotoItemSetting() {

    }

    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {

    }

    @Override
    protected void stuSkip() {

    }
}
