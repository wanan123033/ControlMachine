package com.feipulai.exam.activity.situp.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.Objects;

/**
 * Created by James on 2019/1/18 0018.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SitUpPairPresenter extends SitPullUpPairPresenter {

    private Context context;
    private SitUpSetting setting;
    private SitPushUpManager sitPushUpManager;

    public SitUpPairPresenter(Context context, SitPullUpPairContract.View view) {
        super(context, view);
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(view);
        }
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        sitPushUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
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
        sitPushUpManager.setFrequency(ItemDefault.CODE_YWQZ,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context,setting);
    }

}
