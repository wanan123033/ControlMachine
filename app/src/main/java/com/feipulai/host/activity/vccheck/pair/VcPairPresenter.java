package com.feipulai.host.activity.vccheck.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.newProtocol.NewProtocolLinker;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.host.activity.jump_rope.check.CheckUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;
import com.feipulai.host.activity.situp.pair.SitUpPairContract;
import com.feipulai.host.activity.situp.setting.SitUpSetting;
import com.feipulai.host.config.TestConfigs;

import java.util.Objects;

/**
 * Created by pengjf on 2019/10/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VcPairPresenter extends BasePairPresenter {

    private Context context;
    private SitUpSetting setting;
    private SitPushUpManager sitPushUpManager;
    private int VERSION = 363;
    public VcPairPresenter(Context context, SitUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }

        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
    }


    @Override
    public void setFrequency(int deviceId, int originFrequency, int targetFrequency) {
        if (SerialConfigs.USE_VERSION >= VERSION) {
            sitPushUpManager.setFrequencyNewFHL(SettingHelper.getSystemSetting().getUseChannel(),
                    originFrequency,
                    deviceId,
                    SettingHelper.getSystemSetting().getHostId());
        } else {
            sitPushUpManager.setFrequencyFHL(SettingHelper.getSystemSetting().getUseChannel(),
                    originFrequency,
                    deviceId,
                    SettingHelper.getSystemSetting().getHostId());
        }

    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }

    @Override
    protected int getDeviceSum() {
//        return setting.getDeviceSum();
        return 4;
    }

    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
//        return false;
    }
}
