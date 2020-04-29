package com.feipulai.exam.activity.sitreach.more_device;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitReachManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SitReachWirelessResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.activity.sitreach.SitReachSettingActivity;
import com.feipulai.exam.activity.sitreach.more_device.pair.SitReachPairActivity;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import butterknife.OnClick;

public class SitReachMoreActivity extends BaseMoreActivity {
    private int[] deviceState = {};
    private SitReachSetting setting;
    private boolean using;
    private final static int GET_STATE = 1;
    private final static int GET_RESULT = 2;
    private SitReachManager manager;
    private static final String TAG = "SitReachMoreActivity";
    private boolean backFlag = false;//推杆返回
    private boolean [] resultUpdate  ;//成绩更新
    private final static int DISCONNECT_TIME = 3;
    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
        if (null == setting) {
            setting = new SitReachSetting();
        }
        super.initData();
        manager = new SitReachManager(SitReachManager.PROJECT_CODE_SIT_REACH);
        resultUpdate = new boolean[setting.getTestDeviceCount()];
    }

    @Override
    protected void onResume() {
        super.onResume();
        setting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
        if (null == setting) {
            setting = new SitReachSetting();
        }
        Logger.i(TAG + ":medicineBallSetting ->" + setting.toString());
//        setDeviceCount(setting.getSpDeviceCount());
        deviceState = new int[setting.getTestDeviceCount()];
        resultUpdate = new boolean[setting.getTestDeviceCount()];
        for (int i = 0; i < deviceState.length; i++) {

            deviceState[i] = 0;//连续5次检测不到认为掉线
            resultUpdate[i] = true;
        }
        RadioManager.getInstance().setOnRadioArrived(sitReachRadio);
        getState();
        ledShow();
    }

    private void getState() {
        Log.i(TAG, "james_send_getState");
        for (int i = 0; i < setting.getTestDeviceCount(); i++) {
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
            int hostId = SettingHelper.getSystemSetting().getHostId();
            int deviceId = detail.getStuDevicePair().getBaseDevice().getDeviceId();
            manager.getState(deviceId, hostId);
        }
        mHandler.sendEmptyMessageDelayed(GET_STATE, 1000);
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
        pair.setTestTime(DateUtil.getCurrentTime() + "");
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        int id = pair.getBaseDevice().getDeviceId();
        sendStart((byte) id);
        using = true;
    }

    private void sendStart(byte id) {
        Log.i(TAG, "james_send_sendStart");
        manager.startTest(SettingHelper.getSystemSetting().getHostId(), id);

    }

    private void sendStop(int id) {
        Log.i(TAG, "james_send_sendStop");
        manager.endTest(id, SettingHelper.getSystemSetting().getHostId());
    }

    private void sendFree(int deviceId) {
        Log.i(TAG, "james_send_sendFree");
        manager.setEmpty(deviceId, SettingHelper.getSystemSetting().getHostId());
    }

    @OnClick({R.id.txt_led_setting, R.id.tv_device_pair})
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
                    startActivity(new Intent(this, SitReachPairActivity.class));
                }

                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {

                case GET_STATE:
                    getState();
                    break;
                case GET_RESULT:
                    SitReachWirelessResult result = (SitReachWirelessResult) msg.obj;
                    setTxtEnable(result.getDeviceId(),false);
                    for (DeviceDetail detail : deviceDetails) {
                        if (detail.getStuDevicePair().getBaseDevice().getDeviceId() == result.getDeviceId()) {
                            onResultArrived(result.getCapacity(), detail.getStuDevicePair());
                            break;
                        }

                    }
                    break;
            }

            return false;
        }
    });


    SitReachRadioImpl sitReachRadio = new SitReachRadioImpl(new SitReachRadioImpl.DisposeListener() {
        @Override
        public void onResultArrived(final SitReachWirelessResult result) {
            deviceState[result.getDeviceId() - 1] = DISCONNECT_TIME;//联机正常
            if (result.getState() == 4) {//测试结束
                sendStop(result.getDeviceId());
                if (result.getCapacity() > -200 && result.getCapacity() < 700 && resultUpdate[result.getDeviceId()-1]) {
                    Message msg = mHandler.obtainMessage();
                    msg.obj = result;
                    msg.what = GET_RESULT;
                    mHandler.sendMessage(msg);
                    resultUpdate[result.getDeviceId()-1] = false;
                }
                backFlag = true;
            }
            if (backFlag && result.getState() == 1) {
                sendFree(result.getDeviceId());
                backFlag = false;
                resultUpdate[result.getDeviceId()-1] = true;//标记可以下次测试
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTxtEnable(result.getDeviceId(),true);
                    }
                });

            }
        }

        @Override
        public void onStopTest() {

        }

        @Override
        public void onStarTest(int deviceId) {
            toastSpeak("开始测试");
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        using = false;
        if (stuPair == null || stuPair.getStudent() == null)
            return;
        if (setting.isFullReturn()) {
            if (stuPair.getStudent().getSex() == Student.MALE) {
                stuPair.setFullMark(result >= setting.getManFull() * 10);
            } else {
                stuPair.setFullMark(result >= setting.getWomenFull() * 10);
            }
        }
        stuPair.setEndTime(DateUtil.getCurrentTime() + "");
        stuPair.setResult(result);
        updateResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RadioManager.getInstance().setOnRadioArrived(null);
    }
}
