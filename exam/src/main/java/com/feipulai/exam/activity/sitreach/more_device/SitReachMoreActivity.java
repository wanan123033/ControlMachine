package com.feipulai.exam.activity.sitreach.more_device;

import android.content.Intent;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.medicineBall.MedicineBallSettingActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreActivity;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.activity.sitreach.SitReachSettingActivity;
import com.feipulai.exam.activity.sitreach.more_device.pair.SitReachPairActivity;

import butterknife.OnClick;

public class SitReachMoreActivity extends BaseMoreActivity {
    private static final String TAG = "MedicineMoreActivity";
    private int[] deviceState = {};
    private SitReachSetting setting;


    private final int SEND_EMPTY = 1;
    private int beginPoint;
    private boolean using;

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
        if (null == setting) {
            setting = new SitReachSetting();
        }
        super.initData();

    }
    @Override
    public int setTestCount() {
        return setting.getTestCount();
    }

    @Override
    public int setTestDeviceCount() {
        return setting.getTestDeviceCount();
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return setting.isFullReturn();
    }

    @Override
    public void gotoItemSetting() {
        if (using) {
            toastSpeak("正在测试中,不能设置");
            return;
        }
        startActivity(new Intent(this, SitReachSettingActivity.class));
    }

    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {

    }

    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_led_setting:
                if (isUse()) {
                    toastSpeak("测试中,不允许修改设置");
                } else {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                }

                break;
            case R.id.tv_device_pair:
                if (isUse()) {
                    toastSpeak("测试中,不允许修改设置");
                } else {
                    startActivity(new Intent(this, SitReachPairActivity.class));
                }

                break;
        }
    }
}
