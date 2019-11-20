package com.feipulai.exam.activity.volleyball.more_devices;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.VolleyBallManager;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallPairPresenter extends SitPullUpPairPresenter {

    private Context context;
    private VolleyBallSetting setting;
    public VolleyBallManager deviceManager;

    public VolleyBallPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, VolleyBallSetting.class);
        deviceManager = new VolleyBallManager(setting.getType());
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
        deviceManager.setFrequency(
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
        setting.setPairNum(deviceId);
        SharedPrefsUtil.save(context,setting);
    }

    @Override
    public void onNewDeviceConnect() {
        super.onNewDeviceConnect();
        Log.e("TAG","------------------------onNewDeviceConnect");
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context,setting);
    }

}
