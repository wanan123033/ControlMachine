package com.feipulai.exam.activity.medicineBall.more_device;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.MedicineBallNewResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.medicineBall.MedicineBallSetting;
import com.feipulai.exam.activity.medicineBall.MedicineConstant;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.sargent_jump.more_device.BaseMoreGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.DeviceDetail;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import static com.feipulai.exam.activity.medicineBall.MedicineConstant.CMD_MEDICINE_BALL_EMPTY;
import static com.feipulai.exam.activity.medicineBall.MedicineConstant.CMD_MEDICINE_BALL_SET_EMPTY;
import static com.feipulai.exam.activity.medicineBall.MedicineConstant.CMD_MEDICINE_BALL_START;
import static com.feipulai.exam.activity.medicineBall.MedicineConstant.GET_SCORE_RESPONSE;

public class BallGroupMoreActivity extends BaseMoreGroupActivity {
    private static final String TAG = "BallGroupMoreActivity";
    private MedicineBallSetting sargentSetting;
    private int [] deviceState = new int[4];
    private final int SEND_EMPTY = 1;
    private int beginPoint;
    @Override
    protected void initData() {
        sargentSetting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (null == sargentSetting) {
            sargentSetting = new MedicineBallSetting();
        }
        Logger.i(TAG + ":sargentSetting ->" + sargentSetting.toString());
        sargentSetting.setSpDeviceCount(4);
        setDeviceCount(sargentSetting.getSpDeviceCount());
        deviceState = new int[sargentSetting.getSpDeviceCount()];
        for (int i = 0; i < deviceState.length; i++) {
            deviceState[i] = 0;
        }
        beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(this, "SXQ", "beginPoint", "0"));
        RadioManager.getInstance().setOnRadioArrived(resultImpl);
        sendEmpty();
    }

    private void sendEmpty() {
        Log.i(TAG, "james_send_empty");
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
            byte[] cmd = CMD_MEDICINE_BALL_EMPTY;
            cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
            cmd[6] = (byte) detail.getStuDevicePair().getBaseDevice().getDeviceId();
            cmd[19] = (byte) sum(cmd, 19);
            RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                    cmd));
        }
        mHandler.sendEmptyMessageDelayed(SEND_EMPTY, 1000);
    }

    private int sum(byte[] cmd, int end) {
        int sum = 0;
        for (int i = 1; i < end; i++) {
            sum += cmd[i] & 0xff;
        }
        return sum;
    }

    @Override
    public void toStart(int pos) {
        BaseStuPair pair = deviceDetails.get(pos).getStuDevicePair();
        pair.getBaseDevice().setState(BaseDeviceState.STATE_ONUSE);
        updateDevice(pair.getBaseDevice());
        sendStart((byte) pair.getBaseDevice().getDeviceId());
    }

    private void sendStart(byte id) {
        byte[] cmd = CMD_MEDICINE_BALL_START;
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = id;
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
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

    private void sendFree(int deviceId) {
        byte[] cmd = CMD_MEDICINE_BALL_SET_EMPTY;
        cmd[5] = (byte) SettingHelper.getSystemSetting().getHostId();
        cmd[6] = (byte) deviceId;
        cmd[19] = (byte) sum(cmd, 19);
        RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,
                cmd));
    }

    private boolean isInCorrect;
    private int PROMPT_TIMES;
    private void disposeDevice(MedicineBallNewResult result) {
        isInCorrect = result.isInCorrect();
        if (isInCorrect){
            PROMPT_TIMES++;
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) {
                int[] errors = result.getIncorrectPoles();
                for (int i = 1; i < errors.length + 1; i++) {
                    if (errors[i - 1] == 1) {
                        int e = errors[i] + 1;
                        toastSpeak(String.format("%s测量杆出现异常", "第" + e));
                    }
                }

            }
            deviceState[result.getDeviceId() - 1] = 0;//出现异常
            if (result.getState() == 1){
                sendFree(result.getDeviceId());
            }
        }else {
            PROMPT_TIMES = 0;
            deviceState[result.getDeviceId() - 1] = 5;
        }

    }

    private void onResultArrived(int result, BaseStuPair stuPair) {
        if (result < beginPoint * 10 || result >  5000* 10) {
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
        updateTestResult(stuPair);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_END, stuPair.getBaseDevice().getDeviceId()));

    }


    private MedicineBallImpl resultImpl = new MedicineBallImpl(new MedicineBallImpl.MainThreadDisposeListener() {
        @Override
        public void onResultArrived(MedicineBallNewResult result) {
            Log.i(TAG,result.toString());
            if (result.getState() == 2){
                Message msg = mHandler.obtainMessage();
                msg.obj = result;
                msg.what = MedicineConstant.GET_SCORE_RESPONSE;
                mHandler.sendMessage(msg);
                sendFree(result.getDeviceId());
            }else if (result.getState() == 0){
                disposeDevice(result);
            }
        }

        @Override
        public void onStopTest() {

        }

        @Override
        public void onStarTest(int deviceId) {

        }
    });

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case GET_SCORE_RESPONSE:
                    MedicineBallNewResult result = (MedicineBallNewResult) msg.obj;
                    for (DeviceDetail detail : deviceDetails) {
                        if (detail.getStuDevicePair().getBaseDevice().getDeviceId() == result.getDeviceId()) {
                            int dbResult = result.getResult() * 10+beginPoint * 10;
                            detail.getStuDevicePair().setResultState(result.isFault()? RoundResult.RESULT_STATE_FOUL:RoundResult.RESULT_STATE_NORMAL);
                            onResultArrived(dbResult, detail.getStuDevicePair());

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


}
