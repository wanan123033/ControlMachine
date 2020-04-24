package com.feipulai.exam.activity.sitreach.more_device;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitReachManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SitReachWirelessResult;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sitreach.SitReachSetting;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.entity.Student;

public class SitReachMoreGroupActivity extends BaseMoreGroupActivity {
    private int[] deviceState = {};
    private SitReachSetting setting;
    private boolean using;
    private final static int GET_STATE = 1;
    private final static int GET_RESULT = 2;
    private SitReachManager manager;
    private static final String TAG = "SitReachMoreActivity";
    private boolean backFlag = false;//推杆返回
    private boolean [] resultUpdate  ;//成绩更新
    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SitReachSetting.class);
        if (null == setting) {
            setting = new SitReachSetting();
        }
        resultUpdate = new boolean[setting.getTestDeviceCount()];
        super.initData();
        manager = new SitReachManager(SitReachManager.PROJECT_CODE_SIT_REACH);
        for (int i = 0; i < deviceState.length; i++) {
            deviceState[i] = 0;
            resultUpdate[i] = true;
        }
        getState();
        RadioManager.getInstance().setOnRadioArrived(sitReachRadio);
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
    public void toStart(int pos) {
        Log.i(TAG, "james_send_sendStart");
        using = true;
        BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        pair.setTestTime(DateUtil.getCurrentTime()+"");
        updateDevice(pair.getBaseDevice());
        manager.startTest(SettingHelper.getSystemSetting().getHostId(), pair.getBaseDevice().getDeviceId());
    }

    private void sendFree(int deviceId) {
        Log.i(TAG, "james_send_sendFree");
        manager.setEmpty(deviceId, SettingHelper.getSystemSetting().getHostId());
    }

    @Override
    public int setTestDeviceCount() {
        return setting.getTestDeviceCount();
    }

    @Override
    public int setTestCount() {
        return setting.getTestCount();
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
        updateTestResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));
    }

    SitReachRadioImpl sitReachRadio = new SitReachRadioImpl(new SitReachRadioImpl.DisposeListener() {
        @Override
        public void onResultArrived(final SitReachWirelessResult result) {
            deviceState[result.getDeviceId() - 1] = 5;//联机正常
            if (result.getState() == 4) {//测试结束

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RadioManager.getInstance().setOnRadioArrived(null);
        mHandler.removeCallbacksAndMessages(null);
    }


}
