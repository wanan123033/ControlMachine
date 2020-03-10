package com.feipulai.host.activity.pullup.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.host.activity.pullup.setting.PullUpSetting;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.pair.BasePairPresenter;
import com.feipulai.host.activity.situp.pair.SitUpPairContract;

import java.util.Objects;

/**
 * Created by pengjf on 2019/10/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PullUpPairPresenter extends BasePairPresenter {

    private Context context;
    private PullUpSetting setting;
    private PullUpManager pullUpManager;

    public PullUpPairPresenter(Context context, SitUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }

        setting = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class);
        pullUpManager = new PullUpManager();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int targetFrequency) {
        pullUpManager.setFrequency(originFrequency,
                deviceId, SettingHelper.getSystemSetting().getUseChannel());
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
        return setting.getDeviceSum();
    }

    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }
}
