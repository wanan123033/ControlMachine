package com.feipulai.host.activity.setting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;

import butterknife.OnClick;

/**
 * LED显示屏设置
 * Created by zzs on 2018/7/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class LEDSettingActivity extends BaseTitleActivity {

    private LEDManager mLEDManager;
    private int hostId;


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_led_setting;
    }

    @Override
    protected void initData() {
        hostId = SettingHelper.getSystemSetting().getHostId();
        mLEDManager = new LEDManager();
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.led_setting_title);
    }

    @OnClick({R.id.btn_led_connect, R.id.btn_led_self, R.id.img_led_luminance_munus, R.id.img_led_luminance_add})
    public void onClick(View view) {
        int machineCode = TestConfigs.sCurrentItem.getMachineCode();
        switch (view.getId()) {
            case R.id.btn_led_connect:
                mLEDManager.link(machineCode, hostId,1);
                String machineName = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode());
                mLEDManager.resetLEDScreen(hostId, machineName);
                break;
            case R.id.btn_led_self:
                mLEDManager.test(machineCode, hostId,1);
                break;
            case R.id.img_led_luminance_munus:
                mLEDManager.decreaseLightness(machineCode, hostId,1);
                break;
            case R.id.img_led_luminance_add:
                mLEDManager.increaseLightness(machineCode, hostId,1);
                break;
        }
    }

}
