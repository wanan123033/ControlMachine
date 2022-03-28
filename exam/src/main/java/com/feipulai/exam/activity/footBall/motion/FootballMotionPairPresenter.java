package com.feipulai.exam.activity.footBall.motion;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.reentry.BallReentryPairPresenter;
import com.feipulai.exam.activity.basketball.reentry.BasketBallReentryLinker;
import com.feipulai.exam.activity.footBall.FootBallSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class FootballMotionPairPresenter extends SitPullUpPairPresenter {



    private Context context;
    public FootBallSetting setting;
    private SportTimerManger manager;
    private BallManager ballManager;

    public FootballMotionPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, FootBallSetting.class);
        if (setting == null)
            setting = new FootBallSetting();
        manager = new SportTimerManger();
        this.ballManager = new BallManager(setting.getTestType());
    }

    @Override
    public void start() {
        RadioManager.getInstance().setOnRadioArrived(this);
        linker = new BasketBallReentryLinker(machineCode, TARGET_FREQUENCY, getDeviceSum(), this);
        linker.startPair(0);
        super.start();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    protected int getDeviceSum() {
        return setting.getUseLedType() == 0 ? 4 : 3;
    }

    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    @Override
    public void setFrequency(int deviceId, int deviceHostId, int targetFrequency) {
        if (focusPosition == getDeviceSum() - 1 && setting.getUseLedType() == 0) {
            ballManager.setRadioFrequency(
                    SettingHelper.getSystemSetting().getUseChannel(),
                    0,
                    SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(), setting.getInterceptSecond());
        } else {
            manager.setFrequency(deviceId, targetFrequency, deviceHostId, SettingHelper.getSystemSetting().getHostId());
        }

    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }

    @Override
    public void changeFocusPosition(int position) {
        super.changeFocusPosition(position);
        linker.startPair(position);
    }




}