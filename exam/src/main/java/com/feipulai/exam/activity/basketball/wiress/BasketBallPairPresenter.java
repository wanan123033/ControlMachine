package com.feipulai.exam.activity.basketball.wiress;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.config.TestConfigs;

import java.util.Objects;

public class BasketBallPairPresenter extends SitPullUpPairPresenter {
    private BallManager ballManager;
    private Context context;
    private BasketBallSetting setting;

    public BasketBallPairPresenter(Context context, SitPullUpPairContract.View paramView) {
        super(context, paramView);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 19) {
            Objects.requireNonNull(paramView);
        }
        this.setting = SharedPrefsUtil.loadFormSource(context, BasketBallSetting.class);
        this.ballManager = new BallManager(setting.getTestType());
    }

    @Override
    public void start() {
        linker = new BasketBallLinker(machineCode, TARGET_FREQUENCY, this);
        linker.startPair(1);
        super.start();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    protected int getDeviceSum() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            return 2;
        }
        return 3;
    }

    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        ballManager.setRadioFrequency(
                originFrequency,
                deviceId == getDeviceSum() ? 0 : deviceId,
                SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(), setting.getInterceptSecond());
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context,setting);
    }
}
