package com.feipulai.host.activity.medicine_ball;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BasePersonTestActivity;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.vccheck.TestState;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.SharedPrefsUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.host.activity.medicine_ball.MedicineConstant.END_TEST;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.GET_SCORE_RESPONSE;
import static com.feipulai.host.activity.medicine_ball.MedicineConstant.SELF_CHECK_RESPONSE;

/**
 * 实心球测试
 * Created by Pengjf on 2018/8/20
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * <p>
 * 针对一对多时，设备id处理需考虑，
 */
public class MedicineBallTestActivity extends BasePersonTestActivity{
    private static final String TAG = "MedicineBallActivity";
    private SerialDeviceManager mSerialManager;
    private int PROMPT_TIMES = 0;
    private TestState testState = TestState.UN_STARTED;
    private Handler mHandler = new MedicineBallHandler(this);
    private boolean checkFlag = false;
    private ScheduledExecutorService executorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }
    private void init() {
        mSerialManager = SerialDeviceManager.getInstance();
	    mSerialManager.setRS232ResiltListener(resultImpl);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
    }

    /**
     * 经试验证明 每5 s 发送空闲命令会导致收不到测试结果
     * 所以不需要循环发送空闲命令
     */
    private void sendEmptyCommand() {
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));

    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
//        stuPair = baseStuPair;
        //因为新测试人员过来时需要重新初始化
        checkFlag = false;
        testState = TestState.UN_STARTED;
        decideBegin();
        //更新设备状态
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        updateDevice(baseDevice);
    }

    /**
     * 此处有必要针对多个设备进行修改
     *
     * @return
     */
    @Override
    public List<BaseDeviceState> findDevice() {
        int maxDevice = SharedPrefsUtil.getValue(this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MEDICINE_BALL_TEST_NUMBER, 1);
        List<BaseDeviceState> deviceStates = new ArrayList<>();
        for (int i = 0; i < maxDevice; i++) {
            deviceStates.add(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, i + 1));
        }
        return deviceStates;
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
                    BaseStuPair pair = new BaseStuPair();
                    BaseDeviceState device = new BaseDeviceState();
                    //TODO 设备ID 需要讨论
                    device.setDeviceId(1);
                    pair.setBaseDevice(device);
                    activity.disposeCheck(selfCheckResult, pair);
                    break;

                case GET_SCORE_RESPONSE:
                    MedicineBallResult result = (MedicineBallResult) msg.obj;
                    BaseStuPair basePair = new BaseStuPair();
                    basePair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_END,1));
                    int beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(activity,"SXQ","beginPoint","0"));
                    activity.onResultArrived(result.getResult()*10+beginPoint*10, result.isFault(), basePair);
                    break;
                case END_TEST:
                    BaseDeviceState device1 = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
                    activity.updateDevice(device1);
                    activity.toastSpeak("测试结束");
                    activity.mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
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
    private void disposeCheck(MedicineBallSelfCheckResult selfCheckResult, BaseStuPair stuPair) {
        boolean isInCorrect = selfCheckResult.isInCorrect();
        BaseDeviceState deviceState = stuPair.getBaseDevice();
        if (isInCorrect) {
            PROMPT_TIMES ++ ;
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES< 4) //只做两次提醒
            toastSpeak("测量杆出现异常");
            deviceState.setState(BaseDeviceState.STATE_ERROR);

        } else{
            PROMPT_TIMES = 0 ;
            checkFlag = true;
            if (testState == TestState.UN_STARTED){
                deviceState.setState(BaseDeviceState.STATE_NOT_BEGAIN);
            }else {
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
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));

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
//                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));
                    toastSpeak("开始测试");
                    //设置基点
//                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_SET_BASE_POINT));
//                    //零点距离
//                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_SET_START_POINT));

                    // 开始测试后,会收到1次终端自检结果(终端自检结果可能在开始测试响应之前,也可能在开始测试响应之后)
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_START));

                    executorService.shutdown();
                }
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);

        //方案二 此处可以选择 延迟4.5秒 因为观察可知自检是4s返回一次

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (checkFlag && testState == TestState.UN_STARTED) {
//                    testState = TestState.WAIT_RESULT;
//                   //updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));
//                    toastSpeak("开始测试");
//                    //设置基点
//                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_SET_BASE_POINT));
//                    //零点距离
//                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_SET_START_POINT));
//
//                    // 开始测试后,会收到1次终端自检结果(终端自检结果可能在开始测试响应之前,也可能在开始测试响应之后)
//                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                            SerialConfigs.CMD_MEDICINE_BALL_START));
//                }
//            }
//        }, 4500);

    }


    @Override
    public void switchToFreeTest() {
        startActivity(new Intent(this, MedicineBallFaceIDActivity.class));
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
    });
}
