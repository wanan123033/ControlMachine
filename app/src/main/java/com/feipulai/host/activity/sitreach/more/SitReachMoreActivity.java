package com.feipulai.host.activity.sitreach.more;

import android.content.Intent;
import android.view.View;

import com.feipulai.device.manager.SitReachManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseMoreActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.sitreach.SitReachSettingActivity;
import com.feipulai.host.activity.sitreach.more.pair.SitReachPairActivity;

import butterknife.OnClick;

public class SitReachMoreActivity extends BaseMoreActivity {
    private int[] deviceState = {};
    private boolean using;
    private final static int GET_STATE = 1;
    private final static int GET_RESULT = 2;
    private SitReachManager manager;
    private static final String TAG = "SitReachMoreActivity";
    private boolean backFlag = false;//推杆返回
    private boolean [] resultUpdate  ;//成绩更新
    private final static int DISCONNECT_TIME = 3;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    @Override
    public int setTestDeviceCount() {
        return 1;
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, SitReachSettingActivity.class));
    }

    @Override
    public void sendTestCommand(BaseStuPair pair, int index) {

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
