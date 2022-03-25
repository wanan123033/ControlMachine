package com.feipulai.exam.activity.basketball.motion;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.reentry.BallReentryPairPresenter;
import com.feipulai.exam.activity.basketball.reentry.BasketBallReentryLinker;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class BaskBallMotionPairPresenter extends BallReentryPairPresenter {


    public BaskBallMotionPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
    }


    protected int getDeviceSum() {
        return setting.getUseLedType() == 0 ? 3 : 2;
    }


}
