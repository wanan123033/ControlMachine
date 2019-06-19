package com.feipulai.exam.activity.pullup.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PullUpPairPresenter extends SitPullUpPairPresenter {

    private Context context;
    private PullUpSetting setting;
    private PullUpManager pullUpManager;

    public PullUpPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, PullUpSetting.class);
        pullUpManager = new PullUpManager();
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    protected int getDeviceSum() {
        return setting.getDeviceSum();
    }

    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        pullUpManager.setFrequency(originFrequency, deviceId, deviceFrequency);
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context,setting);
    }

}
