package com.feipulai.exam.activity.basketball.wiress;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

public class BasketBallPairPresenter extends SitPullUpPairPresenter {
    private SitPushUpManager sitPushUpManager;
    private BasketBallSetting setting;
    private Context context;

    public BasketBallPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, BasketBallSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_LZQYQ, PushUpSetting.WIRELESS_TYPE);
    }



    @Override
    public void changeAutoPair(boolean isAutoPair) {

    }

    @Override
    protected int getDeviceSum() {
        return 3;
    }

    @Override
    protected boolean isAutoPair() {
        return false;
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        sitPushUpManager.setFrequencyLZQYQ(
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings() {

    }
}
