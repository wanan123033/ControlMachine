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
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import static com.feipulai.exam.activity.sargent_jump.Constants.GET_SCORE_RESPONSE;

public class SargentTestGroupActivity extends BaseMoreGroupActivity {
    private static final String TAG = "SargentGroupTestActy";
    private SargentSetting sargentSetting;
    @Override
    public int setTestCount() {
        return sargentSetting.getSpDeviceCount();
    }
    private int[] deviceState ;
    //SARGENT JUMP
    public byte[] CMD_SARGENT_JUMP_EMPTY = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 00, 00, 00, 00, 00, 00, 0x12, 0x27, 0x0d};
    public byte[] CMD_SARGENT_JUMP_START = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x01, 00, 00, 00, 00, 00, 0x13, 0x27, 0x0d};
//    public byte[] CMD_SARGENT_JUMP_STOP = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x02, 00, 00, 00, 00, 00, 0x14, 0x27, 0x0d};
//    public byte[] CMD_SARGENT_JUMP_GET_SCORE = {0X54, 0X44, 00, 0X10, 01, 0x01, 00, 0x04, 00, 00, 00, 00, 00, 0x16, 0x27, 0x0d};
    private final int SEND_EMPTY = 1;

    @Override
    public void initData() {
        sargentSetting = SharedPrefsUtil.loadFormSource(this, SargentSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new SargentSetting();
        }
        Logger.i(TAG + ":sargentSetting ->" + sargentSetting.toString());
        setDeviceCount(sargentSetting.getSpDeviceCount());
        deviceState = new int[sargentSetting.getSpDeviceCount()];
        for (int i = 0; i < deviceState.length; i++) {
            deviceState[i] = 1;
        }
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        sendEmpty();
    }

    @Override
    public int setTestPattern() {
        return sargentSetting.getTestPattern();
    }

    @Override
    public void toStart(int pos) {
        BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        byte[] cmd = CMD_SARGENT_JUMP_START;
        cmd[4] = (byte) pair.getBaseDevice().getDeviceId();
        cmd[13] = (byte) sum(cmd);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    public void sendEmpty() {
        for (int i = 0; i < deviceState.length; i++) {
            BaseDeviceState baseDevice = deviceDetails.get(i).getStuDevicePair().getBaseDevice();
            if (deviceState[i] == 0) {

                if (baseDevice.getState() != BaseDeviceState.STATE_ERROR){
                    baseDevice.setState(BaseDeviceState.STATE_ERROR);
                    updateDevice(baseDevice);
                }

            } else {
                if (baseDevice.getState() == BaseDeviceState.STATE_ERROR) {
                    baseDevice.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                    updateDevice(baseDevice);
                }

            }

            deviceState[i] = 0;
        }
        for (DeviceDetail detail : deviceDetails) {
            byte[] cmd = CMD_SARGENT_JUMP_EMPTY;
            cmd[4] = (byte) detail.getStuDevicePair().getBaseDevice().getDeviceId();
            cmd[13] = (byte) sum(cmd);
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    cmd));
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 3000);

    }

    private int sum(byte[] cmd) {
        int sum = 0;
        for (int i = 2; i <= 12; i++) {
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
            deviceState[deviceId] = 1;
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
                            if (detail.getStuDevicePair().getBaseDevice().getState() == BaseDeviceState.STATE_ONUSE) {
                                int dbResult = result.getScore() * 10;
                                onResultArrived(dbResult, detail.getStuDevicePair());
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
        if (result < sargentSetting.getBaseHeight() * 10 || result > (sargentSetting.getBaseHeight() + 116) * 10) {
            toastSpeak("数据异常，请重测");
            return;
        }
        if (stuPair == null || stuPair.getStudent() == null)
            return;
        if (sargentSetting.isFullReturn()) {
            if (stuPair.getStudent().getSex() == Student.MALE) {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getMaleFull()) * 10);
            } else {
                stuPair.setFullMark(stuPair.getResult() >= Integer.parseInt(sargentSetting.getFemaleFull()) * 10);
            }
        }
        stuPair.setResult(result);
        stuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
        updateTestResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));

    }

}