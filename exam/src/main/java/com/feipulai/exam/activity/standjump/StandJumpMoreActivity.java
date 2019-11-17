package com.feipulai.exam.activity.standjump;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreActivity;

/**
 * Created by zzs on  2019/10/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpMoreActivity extends BaseMoreActivity {

    @Override
    public int setTestCount() {
        return 0;
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, StandJumpSettingActivity.class);
    }

    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {

    }

    @Override
    protected void confirmResult(int pos) {

    }
}
