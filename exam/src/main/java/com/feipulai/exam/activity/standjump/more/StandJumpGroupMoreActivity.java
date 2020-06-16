package com.feipulai.exam.activity.standjump.more;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.StandJumpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.standjump.StandJumpSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by zzs on  2019/11/14
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpGroupMoreActivity extends BaseMoreGroupActivity implements StandJumpRadioFacade.StandJumpResponseListener {

    private StandJumpSetting jumpSetting;
    private StandJumpRadioFacade facade;

    @Override
    protected void initData() {
        jumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (jumpSetting == null)
            jumpSetting = new StandJumpSetting();
        super.initData();

        setFaultEnable(jumpSetting.isPenalize());
        if (!jumpSetting.isPenalize()) {
            setNextClickStart(false);
        }
//        setDeviceCount(jumpSetting.getTestDeviceCount());
        facade = new StandJumpRadioFacade(deviceDetails, jumpSetting, this);
    }

    @Override
    public int setTestDeviceCount() {
        return jumpSetting.getTestDeviceCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.life("StandJumpGroupMoreActivity onResume");
        jumpSetting = SharedPrefsUtil.loadFormSource(this, StandJumpSetting.class);
        if (jumpSetting != null) {
            facade.setStandJumpSetting(jumpSetting);
        }
        updateAdapterTestCount();
        facade.resume();
        RadioManager.getInstance().setOnRadioArrived(facade);

    }

    @Override
    public void toStart(int pos) {
        int deviceId = deviceDetails.get(pos).getStuDevicePair().getBaseDevice().getDeviceId();
        Student student = deviceDetails.get(pos).getStuDevicePair().getStudent();
        LogUtils.operation("立定跳远开始测试:deviceId="+deviceId+",student="+student.toString());
        StandJumpManager.setLeisure(SettingHelper.getSystemSetting().getHostId(), deviceId);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        StandJumpManager.startTest(SettingHelper.getSystemSetting().getHostId(), deviceId);
    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return jumpSetting.getTestCount();
        }
    }

    @Override
    public int setTestPattern() {
        return jumpSetting.getTestPattern();
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
                updateTestResult(stuPair);
            }
        });
    }

    @Override
    public void StartDevice(final int deviceId) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (deviceDetails.get(deviceId - 1).getStuDevicePair().getStudent() != null) {
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

    public boolean isResultFullReturn(int sex, int result) {
        if (jumpSetting.isFullReturn()) {
            if (sex == Student.MALE) {
                return result >= jumpSetting.getManFull() * 10;
            } else {
                return result >= jumpSetting.getWomenFull() * 10;
            }
        }
        return false;
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
