package com.feipulai.exam.activity.basketball.motion;

import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.BasketBallSettingActivity;
import com.feipulai.exam.activity.basketball.reentry.BallReentrySettingActivity;
import com.feipulai.exam.activity.basketball.reentry.BasketReentryPairActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

public class BaskBallMotionSettingActivity extends BallReentrySettingActivity {


    @Override
    public void onViewClicked() {
        IntentUtil.gotoActivity(this, BasketMotionPairActivity.class);
    }


}
