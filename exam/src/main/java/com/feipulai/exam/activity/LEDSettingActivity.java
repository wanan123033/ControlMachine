package com.feipulai.exam.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.led.RunLEDManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.view.baseToolbar.BaseToolbar;

import butterknife.OnClick;

/**
 *  ledManager
 */
public class LEDSettingActivity extends BaseTitleActivity {

    private LEDManager mLEDManager;
    private RunLEDManager runLEDManager;
    private int hostId;
    private int flag;


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_led_setting;
    }

    @Override
    protected void initData() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_ZFP) {
            runLEDManager = new RunLEDManager();
            flag = 0;
        } else {
            mLEDManager = new LEDManager();
            flag = 1;
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("显示屏设置").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hostId = SettingHelper.getSystemSetting().getHostId();
    }

    @OnClick({R.id.btn_led_connect, R.id.btn_led_self, R.id.img_led_luminance_munus, R.id.img_led_luminance_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_led_connect:
                if (flag == 0) {
                    runLEDManager.link(hostId);
                    runLEDManager.resetLEDScreen(hostId);
                } else {
                    mLEDManager.link(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                    String title = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode())
                            + " " + hostId;
                    mLEDManager.showString(hostId, title, 0, true, false, LEDManager.MIDDLE);
                    mLEDManager.showString(hostId, "菲普莱体育", 3, 3, false, true);
                }
                break;

            case R.id.btn_led_self:
                if (flag == 0) {
                    runLEDManager.test(hostId);
                } else {
                    mLEDManager.test(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                }
                break;

            case R.id.img_led_luminance_munus:
                if (flag == 0) {
                    runLEDManager.decreaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                } else {
                    mLEDManager.decreaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                }
                break;

            case R.id.img_led_luminance_add:
                if (flag == 0) {
                    runLEDManager.increaseLightness(hostId);
                } else {
                    mLEDManager.increaseLightness(TestConfigs.sCurrentItem.getMachineCode(), hostId);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
