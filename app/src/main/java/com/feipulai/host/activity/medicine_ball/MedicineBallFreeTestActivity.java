package com.feipulai.host.activity.medicine_ball;

import android.os.Handler;
import android.os.Message;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.freedom.BaseFreedomTestActivity;
import com.feipulai.host.activity.vccheck.TestState;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.host.activity.medicine_ball.MedicineConstant.END_TEST;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.GET_SCORE_RESPONSE;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.SELF_CHECK_RESPONSE;

public class MedicineBallFreeTestActivity extends BaseFreedomTestActivity {
    private int PROMPT_TIMES = 0;
    private TestState testState = TestState.UN_STARTED;
    private boolean checkFlag = false;
    private boolean startFlag ;
    private ScheduledExecutorService executorService;
    @Override
    protected void onResume() {
        super.onResume();
        SerialDeviceManager.getInstance().setRS232ResiltListener(resultImpl);
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, MedicineBallSettingActivity.class);
    }

    @Override
    public void startTest() {
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                SerialConfigs.CMD_MEDICINE_BALL_FAST_EMPTY));
        startFlag = true ;
        testState = TestState.UN_STARTED;
        decideBegin();
    }

    @Override
    public void stopTest() {
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
        setDeviceState(new BaseDeviceState(BaseDeviceState.STATE_FREE));
    }

    @Override
    protected void onPause() {
        super.onPause();

        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));
        SerialDeviceManager.getInstance().close();
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
            checkFlag = true ;
        }
    });


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {

                case SELF_CHECK_RESPONSE:
                    MedicineBallSelfCheckResult selfCheckResult = (MedicineBallSelfCheckResult) msg.obj;
                    BaseStuPair pair = new BaseStuPair();
                    BaseDeviceState device = new BaseDeviceState();
                    device.setDeviceId(1);
                    pair.setBaseDevice(device);
                    disposeCheck(selfCheckResult, pair);
                    break;

                case GET_SCORE_RESPONSE:
                    MedicineBallResult result = (MedicineBallResult) msg.obj;
                    BaseStuPair basePair = new BaseStuPair();
                    basePair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_END,1));
                    int beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(MedicineBallFreeTestActivity.this,"SXQ","beginPoint","0"));
                    onResultArrived(result.getResult()*10+beginPoint*10, basePair);
                    break;
                case END_TEST:
                    BaseDeviceState device1 = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
                    setDeviceState(device1);
                    toastSpeak("测试结束");
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
                    break;
                default:
                    break;
            }
            return false;
        }
    });


    private synchronized void decideBegin() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (checkFlag && testState == TestState.UN_STARTED) {
                    testState = TestState.WAIT_RESULT;
                    toastSpeak("开始测试");

                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_START));

                    executorService.shutdown();
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);



    }
    private void onResultArrived(int result,  BaseStuPair basePair) {
        basePair.setResult(result);
        settTestResult(basePair);
    }

    /**
     * 处理自检
     *
     * @param selfCheckResult 自检校验
     */
    private void disposeCheck(MedicineBallSelfCheckResult selfCheckResult, BaseStuPair stuPair) {
        boolean isInCorrect = selfCheckResult.isInCorrect();
        BaseDeviceState deviceState = stuPair.getBaseDevice();
        if (isInCorrect) {
            PROMPT_TIMES++;
            checkFlag = false;
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) {
                int[] errors = selfCheckResult.getIncorrectPoles();
                for (int i = 1; i < errors.length+1; i++) {
                    if (errors[i-1] == 1) {
                        int e = errors[i]+1;
                        toastSpeak(String.format("%s测量杆出现异常", "第" + e));
                    }
                }

            }
            if (deviceState.getState() != BaseDeviceState.STATE_ERROR){
                deviceState.setState(BaseDeviceState.STATE_ERROR);
                setDeviceState(deviceState);
            }

        } else {
            PROMPT_TIMES = 0;
            checkFlag = true;
            if (deviceState.getState() == BaseDeviceState.STATE_ERROR){
                SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
            }
            if (testState == TestState.UN_STARTED) {
                deviceState.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                setDeviceState(deviceState);
            } else {
                deviceState.setState(BaseDeviceState.STATE_ONUSE);
                setDeviceState(deviceState);
            }
            if (testState == TestState.WAIT_RESULT && startFlag){
                toastSpeak("开始测试");
                startFlag = false;
            }

        }

    }


}
