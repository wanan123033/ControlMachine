package com.feipulai.host.activity.vccheck.pair;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.feipulai.device.manager.VolleyBallManager;
import com.feipulai.device.newProtocol.NewProtocolLinker;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.pair.SitUpPairContract;
import com.feipulai.host.activity.situp.pair.SitUpPairPresenter2;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
@Deprecated
public class NewVCPairPresenter extends SitUpPairPresenter2 {

    private Context context;
    public VolleyBallManager deviceManager;

    public NewVCPairPresenter(Context context, SitUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }

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
//        setting.setAutoPair(isAutoPair);

    }

    protected int getDeviceSum() {
//        return setting.getSpDeviceCount();
        return 4;
    }

    protected boolean isAutoPair() {
//        return setting.isAutoPair();
        return true;
    }

    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency(SettingHelper.getSystemSetting().getUseChannel(),
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
//        setting.setPairNum(deviceId);
//        SharedPrefsUtil.save(context, setting);
    }

    @Override
    public void onNewDeviceConnect() {
        super.onNewDeviceConnect();
        Log.e("TAG", "------------------------onNewDeviceConnect");
    }

    @Override
    public void saveSettings() {
//        SharedPrefsUtil.save(context, setting);
    }

}
