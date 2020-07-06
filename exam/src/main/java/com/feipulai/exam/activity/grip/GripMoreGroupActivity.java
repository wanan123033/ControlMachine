package com.feipulai.exam.activity.grip;

import android.os.Handler;
import android.os.Message;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.GripManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.DeviceDetail;

/**
 * Created by pengjf on 2020/7/1.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripMoreGroupActivity extends BaseMoreGroupActivity {
    private GripSetting setting;
    private int[] deviceState = {};
    private boolean using;
    private final static int GET_STATE = 1;
    private final static int GET_RESULT = 2;
    private static final String TAG = "GripMoreGroupActivity";
    @Override
    protected void initData() {
        super.initData();
        setting = SharedPrefsUtil.loadFormSource(this, GripSetting.class);
        if (null == setting) {
            setting = new GripSetting();
        }
        getState();
        RadioManager.getInstance().setOnRadioArrived(gripRadio);
    }

    private void getState() {
        for (int i = 0; i < deviceState.length; i++) {
            BaseDeviceState baseDevice = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            if (deviceState[i] == 0) {
                if (baseDevice.getState() != BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_ERROR);
                    updateDevice(baseDevice);
                }

            } else {
                if (baseDevice.getState() == BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                    updateDevice(baseDevice);
                }
                deviceState[i] -= 1;

            }


        }

        for (DeviceDetail detail : deviceDetails) {
            //主机查询子机
            GripManager.sendCommand(SettingHelper.getSystemSetting().getHostId(),
                    detail.getStuDevicePair().getBaseDevice().getDeviceId(), 0x02);
        }
        mHandler.sendEmptyMessageDelayed(GET_STATE, 1000);
    }

    @Override
    public void toStart(int pos) {
        BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        pair.setTestTime(DateUtil.getCurrentTime()+"");
        updateDevice(pair.getBaseDevice());
        GripManager.sendCommand(SettingHelper.getSystemSetting().getHostId(),
                pair.getBaseDevice().getDeviceId(), 0x03);
    }

    @Override
    public int setTestDeviceCount() {
        return setting.getDeviceSum();
    }

    @Override
    public int setTestCount() {
        return setting.getTestRound();
    }

    @Override
    public int setTestPattern() {
        return setting.getTestPattern();
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case GET_STATE:
                    getState();
                    break;
                case GET_RESULT:
                    GripWrapper result = (GripWrapper) msg.obj;
                    for (DeviceDetail detail : deviceDetails) {
                        if (detail.getStuDevicePair().getBaseDevice().getDeviceId() == result.getDeviceId()) {

                            onResultArrived(result.getResult(), detail.getStuDevicePair());
                        }

                    }
                    break;
            }

            return false;
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        if (stuPair == null || stuPair.getStudent() == null)
            return;
        stuPair.setEndTime(DateUtil.getCurrentTime() + "");
        result = result * 100;//握力需要乘100
        stuPair.setResult(result);
        updateTestResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));
    }

    private WirelessVitalListener gripRadio = new WirelessVitalListener(new WirelessVitalListener.WirelessListener() {
        @Override
        public void onResult(int id, int state, int result, int power) {
            deviceState[id - 1] = 5;
//            powerState[deviceId - 1] = power;
            if (result > 0 && state == 4) {
                Message msg = mHandler.obtainMessage();
                msg.what = GET_RESULT;
                GripWrapper gripWrapper = new GripWrapper(id, result);
                msg.obj = gripWrapper;
                mHandler.sendMessage(msg);
            }
            if (state != 2 && state != 3) {
                GripManager.sendCommand(SettingHelper.getSystemSetting().getHostId(), id, 0x05);
            }
        }

        @Override
        public void onStop() {

        }
    });
}
