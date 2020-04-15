package com.feipulai.exam.activity.sargent_jump.more_device;

import android.os.Handler;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SargentJumpResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.SargentJumpImpl;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import static com.feipulai.device.manager.SargentJumpMore.CMD_SARGENT_JUMP_EMPTY;
import static com.feipulai.device.manager.SargentJumpMore.CMD_SARGENT_JUMP_START;
import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;

public class SargentTestGroupActivity extends BaseMoreGroupActivity {
    private static final String TAG = "SargentGroupTestActy";
    private SargentSetting sargentSetting;

    private int[] deviceState;
    private final int SEND_EMPTY = 1;
    private int runUp;

    @Override
    protected void initData() {
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new SargentSetting();
        }
        super.initData();

        Logger.i(TAG + ":sargentSetting ->" + sargentSetting.toString());
        setDeviceCount(sargentSetting.getSpDeviceCount());
        deviceState = new int[sargentSetting.getSpDeviceCount()];
        for (int i = 0; i < deviceState.length; i++) {
            deviceState[i] = 0;
        }
        runUp = sargentSetting.getRunUp();
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        sendEmpty();
        setNextClickStart(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (int i = 0; i < deviceState.length; i++) {
            deviceState[i] = 0;
        }
    }

    @Override
    public int setTestDeviceCount() {
        return sargentSetting.getSpDeviceCount();
    }

    @Override
    public int setTestCount() {
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return sargentSetting.getTestTimes();
        }
    }

    @Override
    public int setTestPattern() {
        return sargentSetting.getTestPattern();
    }

    @Override
    public void toStart(int pos) {
        BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        pair.setTestTime(System.currentTimeMillis()+"");
        updateDevice(pair.getBaseDevice());
        byte[] cmd = CMD_SARGENT_JUMP_START;
        cmd[4] = (byte) pair.getBaseDevice().getDeviceId();
        cmd[6] = 0x01;
        cmd[7] = 0x03;
        cmd[8] = (byte) sum(cmd, 8);
    }



    public void sendEmpty() {
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
                    deviceState[i] -= 1;
                }

            }


        }
        for (DeviceDetail detail : deviceDetails) {
            byte[] cmd = CMD_SARGENT_JUMP_EMPTY;
            cmd[4] = (byte) detail.getStuDevicePair().getBaseDevice().getDeviceId();
            cmd[6] = 0x01;
            cmd[7] = 0x02;
            cmd[8] = (byte) sum(cmd, 8);
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    cmd));
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);

    }

    private int sum(byte[] cmd, int index) {
        int sum = 0;
        for (int i = 2; i < index; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    private SargentJumpImpl resultImpl = new SargentJumpImpl(new SargentJumpImpl.SargentJumpListener() {
        @Override
        public void onResultArrived(SargentJumpResult result) {
            Message msg = mHandler.obtainMessage();
            msg.obj = result;
            msg.what = GET_SCORE_RESPONSE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onStopTest() {

        }

        @Override
        public void onSelfCheck() {

        }

        @Override
        public void onFree(int deviceId) {
            deviceState[deviceId-1] = 1;
        }

        @Override
        public void onMatch(SargentJumpResult match) {


        }
    });

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SCORE_RESPONSE:
                    SargentJumpResult result = (SargentJumpResult) msg.obj;
                    for (DeviceDetail detail : deviceDetails) {
                        if (detail.getStuDevicePair().getBaseDevice().getDeviceId() == result.getDeviceId()) {
                            int dbResult = result.getScore() * 10;
                            if (runUp == 1) {
                                onResultArrived(dbResult, detail.getStuDevicePair());
                            } else {
                                if (detail.getStuDevicePair().getBaseHeight() == 0) {
                                    detail.getStuDevicePair().setBaseHeight(dbResult);
                                } else {
                                    onResultArrived(dbResult - detail.getStuDevicePair().getBaseHeight(), detail.getStuDevicePair());
                                }
                            }

                        }
                    }
                    break;
                case SEND_EMPTY:
                    sendEmpty();
                    break;
            }

            return false;
        }
    });

    private void onResultArrived(int result, BaseStuPair stuPair) {
        Logger.i("摸高"+stuPair.getStudent()+result);
        if (result < 0 || result > (sargentSetting.getBaseHeight() + 116) * 10) {
            toastSpeak("数据异常，请重测");
            return;
        }
        if (stuPair == null || stuPair.getStudent() == null)
            return;
        stuPair.setResult(result);
        stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
        if (sargentSetting.isFullReturn()) {
            if (stuPair.getStudent().getSex() == Student.MALE) {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
            } else {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
            }
        }
        updateTestResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
