package com.feipulai.exam.activity.grip;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.GripManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.grip.pair.GripPairActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.utils.LogUtils;

import butterknife.OnClick;

/**
 * 握力
 * Created by pengjf on 2020/6/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripMoreActivity extends BaseMoreActivity {
    private GripSetting setting;
    private int[] deviceState = {};
    private boolean using;
    private final static int GET_STATE = 1;
    private final static int GET_RESULT = 2;
    private boolean[] resultUpdate;//成绩更新
    private static final String TAG = "GripMoreActivity";
    private boolean isResume = true;

    @Override
    public void setRoundNo(Student student, int roundNo) {

    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, GripSetting.class);
        if (setting == null)
            setting = new GripSetting();
        super.initData();
        getState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setting = SharedPrefsUtil.loadFormSource(this, GripSetting.class);
        if (setting == null)
            setting = new GripSetting();
        LogUtils.operation("项目设置" + setting.toString());
        deviceState = new int[setting.getDeviceSum()];
        resultUpdate = new boolean[setting.getDeviceSum()];
        for (int i = 0; i < deviceState.length; i++) {

            deviceState[i] = 0;//连续5次检测不到认为掉线
            resultUpdate[i] = true;
        }
        RadioManager.getInstance().setOnRadioArrived(gripRadio);
        isResume = true;
        ledShow();
    }

    private void getState() {
        if (isResume) {
            Log.i(TAG, "send_empty");
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

//                if (tempPower[i] != powerState[i]) {
//                    deviceDetails.get(i).getStuDevicePair().setPower(powerState[i]);
//                    updateDevice(baseDevice);
//                    tempPower[i] = powerState[i];
//                }
                }


            }

            for (DeviceDetail detail : deviceDetails) {
                //主机查询子机
                GripManager.sendCommand(SettingHelper.getSystemSetting().getHostId(),
                        detail.getStuDevicePair().getBaseDevice().getDeviceId(), 0x02);
            }
        }
        mHandler.sendEmptyMessageDelayed(GET_STATE, 1000);
    }

    @Override
    public int setTestCount() {
        return setting.getTestRound();
    }

    @Override
    public int setDeviceCount() {
        return setting.getDeviceSum();
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        return false;
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, GripSettingActivity.class));
    }

    @Override
    protected void sendTestCommand(BaseStuPair pair, int index) {
        LogUtils.operation("、开始测试:index=" + index + ",pair=" + pair.toString());
        pair.setTestTime(DateUtil.getCurrentTime() + "");
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        int id = pair.getBaseDevice().getDeviceId();
        sendStart(id);
        using = true;
    }

    private void sendStart(int deviceId) {
        GripManager.sendCommand(SettingHelper.getSystemSetting().getHostId(),
                deviceId, 0x03);
    }

    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair,R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_led_setting:
                if (using) {
                    toastSpeak("测试中,不允许修改设置");
                } else {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                }

                break;
            case R.id.tv_device_pair:
                if (using) {
                    toastSpeak("测试中,不允许修改设置");
                } else {
                    startActivity(new Intent(this, GripPairActivity.class));
                }

                break;
            case R.id.img_AFR:
                Log.e("TAG","=============");
                showAFR();
                break;
        }
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
        using = false;
        if (stuPair == null || stuPair.getStudent() == null)
            return;
        stuPair.setEndTime(DateUtil.getCurrentTime() + "");
        result = result * 100;//握力需要乘100
        stuPair.setResult(result);
        stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
        updateResult(stuPair);
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

    @Override
    protected void onStop() {
        using = false;
        super.onStop();
        isResume = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        RadioManager.getInstance().setOnRadioArrived(null);
    }
}
