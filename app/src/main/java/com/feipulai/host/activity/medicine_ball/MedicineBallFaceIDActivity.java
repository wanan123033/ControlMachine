package com.feipulai.host.activity.medicine_ball;

import android.os.Handler;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BasePersonFaceIDActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.vccheck.TestState;
import com.feipulai.host.entity.RoundResult;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.greendao.annotation.NotNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.host.activity.medicine_ball.MedicineConstant.END_TEST;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.GET_SCORE_RESPONSE;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.SELF_CHECK_RESPONSE;


public class MedicineBallFaceIDActivity extends BasePersonFaceIDActivity {
    private static final String TAG = "MedicineBallFaceIDActiv";
    private LEDManager mLEDManager;
    private SerialDeviceManager mSerialManager;
    private Handler mHandler = new MedicineBallHandler(this);
    private TestState testState = TestState.UN_STARTED;
    private BaseStuPair stuPair;
    private boolean checkFlag = false;
    private ScheduledExecutorService executorService;
    private int PROMPT_TIMES = 0;

    @Override
    public BaseDeviceState findDevice() {
        return new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
    }

    @Override
    public void sendTestCommand(@NotNull BaseStuPair baseStuPair) {
        LogUtils.operation("???????????????:"+baseStuPair.toString());
        stuPair = baseStuPair;
        if (mSerialManager == null) {
            mSerialManager = SerialDeviceManager.getInstance();
            mSerialManager.setRS232ResiltListener(resultImpl);

        }
        if (mLEDManager == null)
            mLEDManager = new LEDManager();
        //??????????????????
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));

        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
        decideBegin();
        checkFlag = false;
        //??????????????????
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        updateDevice(baseDevice);
    }

//    @Override
//    public String setUnit() {
//        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getUnit())) {
//            return "cm";
//        }
//        return TestConfigs.sCurrentItem.getUnit();
//    }

    public static class MedicineBallHandler extends Handler {
        WeakReference<MedicineBallFaceIDActivity> weakReference;

        private MedicineBallHandler(MedicineBallFaceIDActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MedicineBallFaceIDActivity activity = weakReference.get();
            if (activity == null)
                return;
            switch (msg.what) {

                case SELF_CHECK_RESPONSE:
                    MedicineBallSelfCheckResult selfCheckResult = (MedicineBallSelfCheckResult) msg.obj;
                    LogUtils.operation("???????????????:"+selfCheckResult.toString());
                    BaseStuPair pair = new BaseStuPair();
                    BaseDeviceState device = new BaseDeviceState();
                    //TODO ??????ID ????????????
                    device.setDeviceId(1);
                    pair.setBaseDevice(device);
                    activity.disposeCheck(selfCheckResult);
                    break;

                case GET_SCORE_RESPONSE:
                    MedicineBallResult result = (MedicineBallResult) msg.obj;
                    LogUtils.operation("?????????????????????:"+result.toString());
                    BaseStuPair basePair = new BaseStuPair();
                    basePair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
                    int beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(activity,"SXQ","beginPoint","0"));
                    activity.onResultArrived(result.getResult() * 10+beginPoint*10, result.isFault());
                    break;
                case END_TEST:
                    BaseDeviceState device1 = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
                    LogUtils.operation("????????????????????????:"+device1);
                    activity.updateDevice(device1);
                    activity.toastSpeak("????????????");
//                    activity.mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_EMPTY));

                    sendEmptyMessageDelayed(11, 5000);
                    break;
                case 11:
                    LogUtils.operation("??????????????????????????????");
                    activity.restart();
                    break;
                default:
                    break;
            }

        }

    }

    private void restart() {
        //?????????????????????
        decideBegin();
        testState = TestState.UN_STARTED;
        refreshTxt(null);
    }

    /**
     * ????????????
     *
     * @param result
     * @param fault
     */
    private void onResultArrived(int result, boolean fault) {
        if (testState == TestState.WAIT_RESULT) {
            LogUtils.operation("?????????????????????:result="+result+"----fault="+fault);
            stuPair.setResult(result);
            stuPair.setResultState(fault ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);

            //????????????
            if (fault) {
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "??????", 5, 1, true, true);
            } else {
                String text = result + "cm";
                mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), text, 5, 1, true, true);

            }

            updateResult(stuPair);
            BaseDeviceState deviceState = stuPair.getBaseDevice();
            deviceState.setState(BaseDeviceState.STATE_END);
            updateDevice(stuPair.getBaseDevice());
            // ??????????????????
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_MEDICINE_BALL_STOP));
            //??????????????????
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                    SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
        }
    }

    /**
     * ????????????
     *
     * @param selfCheckResult ????????????
     */
    private void disposeCheck(MedicineBallSelfCheckResult selfCheckResult) {
        LogUtils.operation("?????????????????????:"+selfCheckResult.toString());
        boolean isInCorrect = selfCheckResult.isInCorrect();
        BaseDeviceState deviceState = stuPair.getBaseDevice();
        if (isInCorrect) {
            PROMPT_TIMES++;
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) //??????????????????
                toastSpeak("?????????????????????");
            deviceState.setState(BaseDeviceState.STATE_ERROR);

        } else {
            PROMPT_TIMES = 0;
            checkFlag = true;
            deviceState.setState(testState == TestState.UN_STARTED ?
                    BaseDeviceState.STATE_NOT_BEGAIN : BaseDeviceState.STATE_ONUSE);
        }
        updateDevice(deviceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.operation("MedicineBallFaceIDActivity onDestroy");
        mHandler.removeCallbacksAndMessages(null);
        if (!executorService.isShutdown())
            executorService.shutdown();
    }

    private void decideBegin() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (checkFlag && testState == TestState.UN_STARTED) {
                    testState = TestState.WAIT_RESULT;

                    //????????????
//                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_SET_BASE_POINT));
//                    //????????????
//                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_SET_START_POINT));
                    // ????????????
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_START));
                    toastSpeak("????????????");
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));

                    executorService.shutdown();
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

    }

    private MedicineBallResultImpl resultImpl = new MedicineBallResultImpl(new MedicineBallResultImpl.MainThreadDisposeListener() {
        @Override
        public void onResultArrived(MedicineBallResult result) {
            Message msg = mHandler.obtainMessage();
            msg.obj = result;
            msg.what = GET_SCORE_RESPONSE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onStopTest() {
            mHandler.sendEmptyMessage(END_TEST);
        }

        @Override
        public void onSelfCheck(MedicineBallSelfCheckResult selfCheckResult) {
            Message msg = mHandler.obtainMessage();
            msg.obj = selfCheckResult;
            msg.what = SELF_CHECK_RESPONSE;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onFree() {

        }
    });

}
