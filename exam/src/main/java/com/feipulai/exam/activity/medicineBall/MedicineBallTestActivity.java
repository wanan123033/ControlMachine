package com.feipulai.exam.activity.medicineBall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BasePersonTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.exam.activity.medicineBall.MedicineConstant.END_TEST;
import static com.feipulai.exam.activity.medicineBall.MedicineConstant.GET_SCORE_RESPONSE;
import static com.feipulai.exam.activity.medicineBall.MedicineConstant.SELF_CHECK_RESPONSE;

public class MedicineBallTestActivity extends BasePersonTestActivity {
    private static final String TAG = "MedicineBallActivity";
    private MedicineBallSetting medicineBallSetting;
    private SerialDeviceManager mSerialManager;
    private int PROMPT_TIMES = 0;
    private TestState testState = TestState.UN_STARTED;
    private Handler mHandler = new MedicineBallHandler(this);
    private boolean checkFlag = false;
    private boolean startFlag ;
    private ScheduledExecutorService executorService;
    private static final int DELAY = 0X1000;
    private static final int UPDATEDEVICE = 0X1001;
    private ScheduledExecutorService checkService;
    private Student student ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        checkService = Executors.newSingleThreadScheduledExecutor();
        mSerialManager = SerialDeviceManager.getInstance();
        mSerialManager.setRS232ResiltListener(resultImpl);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        setTestType(1);
        sendCheck();
    }

    @Override
    public void stuSkip() {
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //设置基点
//        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                SerialConfigs.CMD_MEDICINE_BALL_SET_BASE_POINT));
//        //零点距离
//        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                SerialConfigs.CMD_MEDICINE_BALL_SET_START_POINT));

    }


    @Override
    public void initData() {
        medicineBallSetting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (null == medicineBallSetting) {
            medicineBallSetting = new MedicineBallSetting();
        }
        Logger.i(TAG + ":medicineBallSetting ->" + medicineBallSetting.toString());
        if (medicineBallSetting.isPenalize()){
            setFaultVisible(true);
            setFaultEnable(false);
        }

    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        setFaultEnable(false);
        student = baseStuPair.getStudent();
        if (student == null){
            toastSpeak("请先添加学生");
            return;
        }
        startFlag = true ;
        testState = TestState.UN_STARTED;
        decideBegin();
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
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
    public int setTestCount() {

        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return medicineBallSetting.getTestTimes();
        }
    }

    @Override
    public boolean isResultFullReturn(int sex, int result) {
        if (medicineBallSetting.isFullReturn()) {
            if (sex == Student.MALE) {
                return result >= Integer.valueOf(medicineBallSetting.getMaleFull()) * 10;
            } else {
                return result >= Integer.valueOf(medicineBallSetting.getFemaleFull()) * 10;
            }
        }
        return false;
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
                    activity.setBegin(1);
                    MedicineBallResult result = (MedicineBallResult) msg.obj;
                    BaseStuPair basePair = new BaseStuPair();
                    basePair.setStudent(activity.student);
                    basePair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
                    int beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(activity, "SXQ", "beginPoint", "0"));
                    if (result.getSweepPoint()< 2){
                        activity.showValidResult(result.getResult() * 10 + beginPoint * 10, result.isFault(), basePair);
                    }else {
                        activity.onResultArrived(result.getResult() * 10 + beginPoint * 10, result.isFault(), basePair);
                    }
                    break;
                case END_TEST:
                    BaseDeviceState device1 = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
                    activity.updateDevice(device1);
                    activity.toastSpeak("测试结束");
                    break;
                case DELAY:
                    activity.sendFree();
                    break;
                case UPDATEDEVICE:
                    BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_ERROR,0);
                    activity.updateDevice(deviceState);
                    activity.sendFree();
                    break;
                default:
                    break;
            }

        }
    }

    private void sendCheck(){
//        checkService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                if (!checkFlag){
//                    mHandler.sendEmptyMessage(UPDATEDEVICE);
//                }
//            }
//        },5000,5000,TimeUnit.MILLISECONDS);
        mHandler.sendEmptyMessage(UPDATEDEVICE);
    }

    private void sendFree(){
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
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
            startFlag = false ;
            checkFlag = false;
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) {
                int[] errors = selfCheckResult.getIncorrectPoles();
                for (int i = 1; i < errors.length+1; i++) {
                    if (errors[i-1] == 1) {
                        int e = errors[i]+1;
                        toastSpeak(String.format("%s测量杆出现异常", "第" + e));
                    }
                }

            }
            setBegin(0);
            deviceState.setState(BaseDeviceState.STATE_ERROR);
//            mHandler.sendEmptyMessageDelayed(DELAY,1000);
        } else {
            PROMPT_TIMES = 0;
            checkFlag = true;
            if (testState == TestState.UN_STARTED) {
                deviceState.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                setBegin(1);
            } else {
                deviceState.setState(BaseDeviceState.STATE_ONUSE);
            }
            if (testState == TestState.WAIT_RESULT && startFlag){
                toastSpeak("开始测试");
                startFlag = false;
                setBegin(0);
            }
        }
        updateDevice(deviceState);

    }


    private void onResultArrived(int result, boolean fault, BaseStuPair stuPair) {
        if (testState == TestState.WAIT_RESULT) {
            if (medicineBallSetting.isFullReturn()) {
                if (stuPair.getStudent().getSex() == Student.MALE) {
                    stuPair.setFullMark(result >= Integer.parseInt(medicineBallSetting.getMaleFull()) * 10);
                } else {
                    stuPair.setFullMark(result >= Integer.parseInt(medicineBallSetting.getFemaleFull()) * 10);
                }
            }
            stuPair.setResult(result);
            stuPair.setResultState(fault ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);
            updateResult(stuPair);
            updateDevice(stuPair.getBaseDevice());
            // 发送结束命令
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));

            testState = TestState.UN_STARTED;

            setFaultVisible(true);
        }
    }

    private void showValidResult(final int result, final boolean fault, final BaseStuPair stuPair){
        AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        builder.setTitle("成绩是否有效");
        builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 发送结束命令
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));
                testState = TestState.UN_STARTED;
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onResultArrived(result,fault,stuPair);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
        mSerialManager.close();
        checkService.shutdown();
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
                    // 开始测试后,会收到1次终端自检结果(终端自检结果可能在开始测试响应之前,也可能在开始测试响应之后)
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_START));

                    executorService.shutdown();
                }
            }
        }, 500, 500, TimeUnit.MILLISECONDS);


    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, MedicineBallSettingActivity.class));
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
            sendFree();
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
}
