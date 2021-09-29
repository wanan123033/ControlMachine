package com.feipulai.host.activity.sporttime;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.newProtocol.NewProtocolLinker;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.SitPullUpPairContract;
import com.feipulai.host.activity.situp.SitPullUpPairPresenter;
import com.feipulai.host.activity.situp.pair.SitUpPairContract;
import com.feipulai.host.activity.situp.pair.SitUpPairPresenter;


import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SportTimerPairPresenter extends SitUpPairPresenter {

    private Context context;
    private SportTimerSetting setting;
    private SportTimerManger manager;

    public SportTimerPairPresenter(Context context, SitUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, SportTimerSetting.class);
        if (setting == null)
            setting = new SportTimerSetting();
        manager = new SportTimerManger();
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

    protected int getDeviceSum() {
        return setting.getDeviceCount();
    }

    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    public void setFrequency(int deviceId, int hostId, int targetFrequency) {

        manager.setFrequency(deviceId,targetFrequency,hostId,SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }

}
