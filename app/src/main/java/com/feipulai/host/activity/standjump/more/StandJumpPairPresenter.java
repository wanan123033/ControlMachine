package com.feipulai.host.activity.standjump.more;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;
import com.feipulai.host.activity.situp.pair.SitUpPairContract;
import com.feipulai.host.activity.standjump.StandJumpSetting;

import java.util.Objects;

public class StandJumpPairPresenter extends BasePairPresenter {
    private Context context;
    private StandJumpSetting setting;

    public StandJumpPairPresenter(Context context, SitUpPairContract.View paramView) {
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
        linker = new StandJumpLinker(machineCode, TARGET_FREQUENCY, this, SettingHelper.getSystemSetting().getHostId());
        linker.startPair(1);
        super.start();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    protected int getDeviceSum() {
        return SettingHelper.getSystemSetting().isFreedomTest() ? 1 : setting.getTestDeviceCount();
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
