package com.feipulai.exam.activity.standjump.more;

import android.content.Intent;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.activity.standjump.StandJumpSettingActivity;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
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

        setFaultEnable(standJumpSetting.isPenalize());
        setNextClickStart(false);

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

    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return standJumpSetting.getTestCount();
        }
    }

    @Override
    public int setTestDeviceCount() {
        return standJumpSetting.getTestDeviceCount();
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        if (standJumpSetting.isFullReturn()) {
            if (sex == Student.MALE) {
                return result >= standJumpSetting.getManFull() * 10;
            } else {
                return result >= standJumpSetting.getWomenFull() * 10;
            }
        }
        return false;
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, StandJumpSettingActivity.class);
    }

    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {
        StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), pair.getBaseDevice().getDeviceId());
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StandJumpManager.startTest(SettingHelper.getSystemSetting().getHostId(), pair.getBaseDevice().getDeviceId());
    }

    @Override
    protected void confirmResult(int pos) {

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
                    startActivity(new Intent(this, StandJumpPairActivity.class));
                }

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
                stuPair.setFullMark(isResultFullReturn(stuPair.getStudent().getSex(), stuPair.getResult()));
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
        toastSpeak(deviceDetails.get(deviceId - 1).getStuDevicePair().getStudent().getSpeakStuName() + "开始测试");


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
