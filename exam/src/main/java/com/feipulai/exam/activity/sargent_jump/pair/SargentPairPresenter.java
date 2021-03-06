package com.feipulai.exam.activity.sargent_jump.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.newProtocol.NewProtocolLinker;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SargentPairPresenter extends SitPullUpPairPresenter {

    private Context context;
    private SargentSetting setting;
    private SitPushUpManager sitPushUpManager;

    public SargentPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, SargentSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SARGENT, PushUpSetting.WIRELESS_TYPE);
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
        return setting.getSpDeviceCount();
    }

    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        sitPushUpManager.setFrequencyMG(
                SettingHelper.getSystemSetting().getUseChannel(),
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }

}
