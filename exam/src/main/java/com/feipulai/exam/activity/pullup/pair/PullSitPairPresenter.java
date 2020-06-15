package com.feipulai.exam.activity.pullup.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

public class PullSitPairPresenter extends SitPullUpPairPresenter {
    private PullUpManager pullUpManager;
    private SitPushUpManager sitPushUpManager;
    private Context context;
    private PullUpSetting setting;

    public PullSitPairPresenter(Context context, SitPullUpPairContract.View paramView) {
        super(context, paramView);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 19) {
            Objects.requireNonNull(paramView);
        }
        this.setting = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class);
        this.pullUpManager = new PullUpManager();
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP_HAND);
    }

    @Override
    public void start() {
        linker = new PullSitLinker(machineCode, TARGET_FREQUENCY, this);
        linker.startPair(1);
        super.start();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    protected int getDeviceSum() {

        return 2;
    }

    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        if (deviceId == 1){
            pullUpManager.setFrequency(originFrequency, deviceId, deviceFrequency);
        }else {
            sitPushUpManager.setFrequency( deviceFrequency,
                    originFrequency,
                    deviceId,
                    SettingHelper.getSystemSetting().getHostId());
        }
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
    }
}
