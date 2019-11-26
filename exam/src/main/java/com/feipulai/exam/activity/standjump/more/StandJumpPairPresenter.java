package com.feipulai.exam.activity.standjump.more;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.activity.standjump.StandJumpSetting;

import java.util.Objects;

public class StandJumpPairPresenter extends SitPullUpPairPresenter {
    private Context context;
    private StandJumpSetting setting;

    public StandJumpPairPresenter(Context context, SitPullUpPairContract.View paramView) {
        super(context, paramView);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 19) {
            Objects.requireNonNull(paramView);
        }
        this.setting = SharedPrefsUtil.loadFormSource(context, StandJumpSetting.class);
    }

    @Override
    public void start() {
        RadioManager.getInstance().setOnRadioArrived(this);
        linker = new StandJumpLinker(machineCode, TARGET_FREQUENCY, this);
        linker.startPair(1);
        super.start();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    protected int getDeviceSum() {
        return setting.getTestDeviceCount();
    }

    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        StandJumpManager.setFrequencyParameter(SettingHelper.getSystemSetting().getUseChannel(),
                SettingHelper.getSystemSetting().getHostId(), deviceId, setting.getPointsScopeArray()[deviceId - 1] - 42);
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }
}
