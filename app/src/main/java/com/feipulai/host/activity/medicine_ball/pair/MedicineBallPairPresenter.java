package com.feipulai.host.activity.medicine_ball.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.MedicineBallMore;
import com.feipulai.device.newProtocol.NewProtocolLinker;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.medicine_ball.MedicineBallSetting;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;
import com.feipulai.host.activity.situp.pair.SitUpPairContract;

import java.util.Objects;
import java.util.UUID;

public class MedicineBallPairPresenter extends BasePairPresenter {
    private Context context;
    private MedicineBallSetting setting;
    public MedicineBallPairPresenter(Context context, SitUpPairContract.View paramView) {
        super(context, paramView);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 19) {
            Objects.requireNonNull(paramView);
        }
        this.setting = SharedPrefsUtil.loadFormSource(context, MedicineBallSetting.class);
    }

    @Override
    public void start() {
        RadioManager.getInstance().setOnRadioArrived(this);
        linker = new NewProtocolLinker(machineCode, TARGET_FREQUENCY, this, SettingHelper.getSystemSetting().getHostId());
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
        MedicineBallMore.setFrequency(deviceFrequency,originFrequency,deviceId,SettingHelper.getSystemSetting().getHostId()
                );
//        SettingHelper.getSystemSetting().getUseChannel(),
//                SettingHelper.getSystemSetting().getHostId(), deviceId, setting.getPointsScopeArray()[deviceId - 1] - 42
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }

    public static class Characteristic {
        final static public UUID HEART_RATE_MEASUREMENT   = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
        final static public UUID MANUFACTURER_STRING      = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
        final static public UUID MODEL_NUMBER_STRING      = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
        final static public UUID FIRMWARE_REVISION_STRING = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
        final static public UUID APPEARANCE               = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb");
        final static public UUID BODY_SENSOR_LOCATION     = UUID.fromString("00002a38-0000-1000-8000-00805f9b34fb");
        final static public UUID BATTERY_LEVEL            = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    }
}
