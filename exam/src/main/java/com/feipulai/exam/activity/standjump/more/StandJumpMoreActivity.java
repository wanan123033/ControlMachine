package com.feipulai.exam.activity.standjump.more;

import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.activity.standjump.StandJumpSettingActivity;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import java.util.List;

import butterknife.OnClick;

/**
 * Created by zzs on  2019/10/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpMoreActivity extends BaseMoreActivity implements StandJumpRadioFacade.StandJumpResponseListener {

    private StandJumpRadioFacade facade;
    private StandJumpSetting standJumpSetting;
    @Override
    protected void initViews() {
        standJumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (standJumpSetting == null)
            standJumpSetting = new StandJumpSetting();
        super.initViews();
    }
    @Override
    protected void initData() {

        super.initData();
        setFaultEnable(standJumpSetting.isPenalizeFoul());
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

        facade.resume();
        RadioManager.getInstance().setOnRadioArrived(facade);
        boolean isUser = false;
        for (DeviceDetail detail : deviceDetails) {
            if (detail.getStuDevicePair().getStudent() != null) {
                isUser = true;
            }
        }
        if (!isUser) {
            updateAdapterTestCount();
        }

        for (int i = 1; i <= standJumpSetting.getTestDeviceCount(); i++) {

            int testPoints = standJumpSetting.getTestPointsArray()[i - 1];
            int scope = standJumpSetting.getPointsScopeArray()[i - 1];
            if (scope > 0) {
                StandJumpManager.setPoints(SettingHelper.getSystemSetting().getHostId(), i, scope - 42);
            } else {
                StandJumpManager.setPoints(SettingHelper.getSystemSetting().getHostId(), i, (testPoints * 100 + 50 - 8) - 42);
            }


        }
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
    public int setDeviceCount() {
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
        LogUtil.logDebugMessage("sendTestCommand");
        StandJumpManager.startTest(SettingHelper.getSystemSetting().getHostId(), pair.getBaseDevice().getDeviceId());
        pair.setTestTime(System.currentTimeMillis() + "");
    }

    @Override
    public void stuSkip(int pos) {
        super.stuSkip(pos);
        LogUtil.logDebugMessage("stuSkip");
        StandJumpManager.endTest(SettingHelper.getSystemSetting().getHostId(), pos + 1);
        StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), pos + 1);
    }

    @Override
    protected void confirmResult(int pos) {

    }

    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair, R.id.img_AFR})
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
        if (deviceDetails.get(deviceId - 1).getStuDevicePair().getStudent() == null) {
            return;
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

    @Override
    public void setRoundNo(Student student, int roundNo) {

    }
}
