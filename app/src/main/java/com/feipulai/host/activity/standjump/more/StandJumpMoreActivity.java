package com.feipulai.host.activity.standjump.more;

import android.content.Intent;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseMoreActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.standjump.StandJumpSetting;
import com.feipulai.host.activity.standjump.StandJumpSettingActivity;
import com.orhanobut.logger.Logger;

import butterknife.OnClick;

/**
 * Created by zzs on  2019/10/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpMoreActivity extends BaseMoreActivity implements StandJumpRadioFacade.StandJumpResponseListener {

    private StandJumpRadioFacade facade;
    private StandJumpSetting standJumpSetting;

    @Override
    protected void initData() {
        standJumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (standJumpSetting == null)
            standJumpSetting = new StandJumpSetting();
        super.initData();

        facade = new StandJumpRadioFacade(deviceDetails, standJumpSetting, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        standJumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (standJumpSetting != null) {
            facade.setStandJumpSetting(standJumpSetting);
        }
        facade.setDeviceList(deviceDetails);
        updateAdapterTestCount();
        facade.resume();
        RadioManager.getInstance().setOnRadioArrived(facade);
        setNextClickStart(false);
    }


    @Override
    public int setTestDeviceCount() {
        return standJumpSetting.getTestDeviceCount();
    }


    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, StandJumpSettingActivity.class);
    }


    @Override
    public void sendTestCommand(BaseStuPair pair, int index) {
        StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), pair.getBaseDevice().getDeviceId());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pair.setStartTime(DateUtil.getCurrentTime());
        StandJumpManager.startTest(SettingHelper.getSystemSetting().getHostId(), pair.getBaseDevice().getDeviceId());
    }


    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair,R.id.img_AFR})
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
                    startActivity(new Intent(this, StandJumpPairActivity.class));
                }

                break;
            case R.id.img_AFR:
                showAFR();
                break;
        }
    }


    @Override
    public void refreshDeviceState(final int deviceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshDevice(deviceId);
            }
        });

    }

    @Override
    public void getResult(final BaseStuPair stuPair) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stuPair.setEndTime(DateUtil.getCurrentTime());
                updateResult(stuPair);
            }
        });
    }

    @Override
    public void StartDevice(int deviceId) {
        Logger.i("zzs=============StartDevice");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //未添加考生避免串机使用
        if (deviceDetails.size() >= deviceId && deviceDetails.get(deviceId - 1).getStuDevicePair().getStudent() != null) {
            toastSpeak(deviceDetails.get(deviceId - 1).getStuDevicePair().getStudent().getSpeakStuName() + "开始测试");

        }


    }

    @Override
    public void endDevice(final BaseDeviceState deviceState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateDevice(deviceState);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        facade.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facade.finish();
        facade = null;
        RadioManager.getInstance().setOnRadioArrived(null);
    }
}
