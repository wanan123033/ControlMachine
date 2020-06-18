package com.feipulai.host.activity.sitreach.more.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitReachManager;
import com.feipulai.device.newProtocol.NewProtocolLinker;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.sitreach.SitReachSetting;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;
import com.feipulai.host.activity.situp.pair.SitUpPairContract;

import java.util.Objects;

public class SitReachPairPresenter extends BasePairPresenter {
    private Context context;
    private SitReachSetting setting;
    SitReachManager sitReachManager;
    public SitReachPairPresenter(Context context, SitUpPairContract.View paramView) {
        super(context, paramView);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 19) {
            Objects.requireNonNull(paramView);
        }
        this.setting = SharedPrefsUtil.loadFormSource(context, SitReachSetting.class);
        sitReachManager = new SitReachManager(SitReachManager.PROJECT_CODE_SIT_REACH);
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
        sitReachManager.setFrequency(deviceFrequency,deviceId,SettingHelper.getSystemSetting().getHostId()
                );
//        SettingHelper.getSystemSetting().getUseChannel(),
//                SettingHelper.getSystemSetting().getHostId(), deviceId, setting.getPointsScopeArray()[deviceId - 1] - 42
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }
}
