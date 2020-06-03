package com.feipulai.host.activity.medicine_ball;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.person.BasePersonTestActivity;
import com.feipulai.host.activity.vccheck.TestState;
import com.feipulai.host.entity.RoundResult;
import com.orhanobut.logger.examlogger.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.host.activity.medicine_ball.MedicineConstant.END_TEST;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.GET_SCORE_RESPONSE;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.SELF_CHECK_RESPONSE;

/**
 * 实心球测试
 * Created by Pengjf on 2018/8/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MedicineBallTestActivity extends BasePersonTestActivity {
    private static final String TAG = "MedicineBallActivity";
    private int PROMPT_TIMES = 0;
    private TestState testState = TestState.UN_STARTED;
    private Handler mHandler = new MedicineBallHandler(this);
    private boolean checkFlag = false;
    private ScheduledExecutorService executorService;
    private static final int START_DEVICE = 0X1002;
    private static final int DEVICE_CHECK = 0X1001;
    private int startCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setTestType(1);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
        sendCheck();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SerialDeviceManager.getInstance().setRS232ResiltListener(resultImpl);
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
    }


    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        if (baseStuPair.getStudent() == null) {
            toastSpeak("当前无学生测试");
            return;
        }
//        stuPair = baseStuPair;
        //因为新测试人员过来时需要重新初始化
        testState = TestState.UN_STARTED;
        decideBegin();
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                SerialConfigs.CMD_MEDICINE_BALL_FAST_EMPTY));
        //更新设备状态
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        updateDevice(baseDevice);
    }

    @Override
    public void gotoItemSetting() {
        IntentUtil.gotoActivity(this, MedicineBallSettingActivity.class);
    }

    @Override
    public void stuSkip() {
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        sendFree();
    }

    private void sendCheck() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!checkFlag){
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR,1));
                }
                if (startCount == 6){//因为下位机每4s会发送一次信息
                    sendFree();
                }
                startCount++;
                mHandler.sendEmptyMessage(DEVICE_CHECK);
            }
        },1000);
    }

    private void sendFree() {
        LogUtils.normal(SerialConfigs.CMD_MEDICINE_BALL_EMPTY.length+"---"+ StringUtility.bytesToHexString(SerialConfigs.CMD_MEDICINE_BALL_EMPTY)+"---实心球空闲指令");
        SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
    }
    public static class MedicineBallHandler extends Handler {
        WeakReference<MedicineBallTestActivity> weakReference;

        private MedicineBallHandler(MedicineBallTestActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MedicineBallTestActivity activity = weakReference.get();
            if (activity == null)
                return;
            switch (msg.what) {

                case SELF_CHECK_RESPONSE:
                    MedicineBallSelfCheckResult selfCheckResult = (MedicineBallSelfCheckResult) msg.obj;
                    activity.disposeCheck(selfCheckResult);
                    break;

                case GET_SCORE_RESPONSE:
                    MedicineBallResult result = (MedicineBallResult) msg.obj;
                    BaseStuPair basePair = new BaseStuPair();
                    basePair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
                    int beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(activity, "SXQ", "beginPoint", "0"));
                    activity.onResultArrived(result.getResult() * 10 + beginPoint * 10, result.isFault(), basePair);
                    break;
                case END_TEST:
                    BaseDeviceState device1 = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
                    activity.updateDevice(device1);
                    activity.toastSpeak("测试结束");
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
                    break;
                case START_DEVICE:
                    activity.toastSpeak("开始测试");
                    break;
                case DEVICE_CHECK:
                    activity.sendCheck();
                    break;
                default:
                    break;
            }

        }
    }


    /**
     * 处理自检
     *
     * @param selfCheckResult 自检校验
     */
    private void disposeCheck(MedicineBallSelfCheckResult selfCheckResult) {
        boolean isInCorrect = selfCheckResult.isInCorrect();
        BaseDeviceState deviceState = new BaseDeviceState();
        if (isInCorrect) {
            PROMPT_TIMES++;
            checkFlag = false;
            sendFree();
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_FAST_EMPTY));
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) {
                int[] errors = selfCheckResult.getIncorrectPoles();
                for (int i = 1; i < errors.length + 1; i++) {
                    if (errors[i - 1] == 1) {
                        int e = errors[i] + 1;
                        toastSpeak(String.format("%s测量杆出现异常", "第" + e));
                    }
                }

            }
            setBegin(0);
            deviceState.setState(BaseDeviceState.STATE_ERROR);

        } else {
            PROMPT_TIMES = 0;
            checkFlag = true;
            if (testState == TestState.UN_STARTED) {
                deviceState.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                setBegin(1);
            } else {
                deviceState.setState(BaseDeviceState.STATE_ONUSE);
            }

        }

        updateDevice(deviceState);
    }


    private void onResultArrived(int result, boolean fault, BaseStuPair stuPair) {
        if (testState == TestState.WAIT_RESULT) {
            stuPair.setResult(result);
            stuPair.setResultState(fault ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);
            updateResult(stuPair);
            updateDevice(stuPair.getBaseDevice());
            // 发送结束命令
            SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));
            pair.setEndTime(DateUtil.getCurrentTime());
            testState = TestState.UN_STARTED;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
    }


    /**
     * 终端自检结果可能在开始测试响应之前,也可能在开始测试响应之后
     * 所以轮询查看是否需要开始测试
     */
    private synchronized void decideBegin() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (checkFlag && testState == TestState.UN_STARTED) {
                    testState = TestState.WAIT_RESULT;
                    SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_START));
                    mHandler.sendEmptyMessageDelayed(START_DEVICE,1000);
                    setBegin(0);
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
            checkFlag = true;
            startCount = 0 ;
        }
    });
}
