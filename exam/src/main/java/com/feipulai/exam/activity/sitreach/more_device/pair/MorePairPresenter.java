package com.feipulai.exam.activity.sitreach.more_device.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitReachManager;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by pengjf on 2020/4/17.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MorePairPresenter extends SitPullUpPairPresenter {
    private Context context;
    private SitReachSetting setting ;
    private SitReachManager manager;
    public MorePairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, SitReachSetting.class);
        manager = new SitReachManager(SitReachManager.PROJECT_CODE_SIT_REACH);
    }

    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }

    @Override
    protected int getDeviceSum() {
        return setting.getTestDeviceCount();
    }

    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        manager.setFrequency(deviceFrequency,deviceId, SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context,setting);
    }
}
