package com.feipulai.host.activity.sitreach.more;

import android.content.Intent;

import com.feipulai.device.manager.SitReachManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.activity.base.BaseMoreActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.sitreach.SitReachSettingActivity;

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
        return 0;
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, SitReachSettingActivity.class));
    }

    @Override
    public void sendTestCommand(BaseStuPair pair, int index) {

    }
}
