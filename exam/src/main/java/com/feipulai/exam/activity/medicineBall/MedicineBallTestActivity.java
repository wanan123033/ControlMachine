package com.feipulai.exam.activity.medicineBall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BasePersonTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

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
    private ScheduledExecutorService executorService;
    private static final int DELAY = 0X1000;
    private static final int UPDATE_DEVICE = 0X1001;
    private static final int START_DEVICE = 0X1002;
    private ScheduledExecutorService checkService;
    private Student student;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.life("MedicineBallTestActivity onCreate");
        init();
    }

    @Override
    protected int isShowPenalizeFoul() {
        return medicineBallSetting.isPenalizeFoul() ? View.VISIBLE : View.GONE;
    }

    private void init() {
        checkService = Executors.newSingleThreadScheduledExecutor();
        mSerialManager = SerialDeviceManager.getInstance();

        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        setTestType(1);
        sendCheck();
        sendFree();
    }

    @Override
    public void stuSkip() {
        sendFree();
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.life("MedicineBallTestActivity onResume");
        mSerialManager.setRS232ResiltListener(resultImpl);
        //????????????
//        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                SerialConfigs.CMD_MEDICINE_BALL_SET_BASE_POINT));
//        //????????????
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
//        if (medicineBallSetting.isPenalize()) {
//
//            setFaultEnable(true);
//        }

    }

    @Override
    public void sendTestCommand(BaseStuPair baseStuPair) {
        student = baseStuPair.getStudent();
        if (student == null) {
            toastSpeak("??????????????????");
            return;
        }
        testState = TestState.UN_STARTED;
        decideBegin();
//        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                SerialConfigs.CMD_MEDICINE_BALL_FAST_EMPTY));
        //??????????????????
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        baseStuPair.setTestTime(System.currentTimeMillis() + "");
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
                    Logger.i("result ===?????????" + result.getResult() + "sweepPoint" + result.getSweepPoint());
                    if (result.getSweepPoint() < 2) {
                        activity.showValidResult(result.getResult() * 10 + beginPoint * 10, result.isFault(), basePair);
                    } else {
                        activity.onResultArrived(result.getResult() * 10 + beginPoint * 10, result.isFault(), basePair);
                    }
                    break;
                case END_TEST:
                    BaseDeviceState device1 = new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1);
                    activity.updateDevice(device1);
                    activity.toastSpeak("????????????");
                    break;
                case DELAY:
                    activity.sendFree();
                    break;
                case UPDATE_DEVICE:
                    BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_ERROR, 0);
                    activity.updateDevice(deviceState);
                    activity.sendFree();
                    break;
                case START_DEVICE:
                    activity.toastSpeak("????????????");
                    break;
                default:
                    break;
            }

        }
    }

    private void sendCheck() {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!checkFlag) {
                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR, 1));
                }
            }
        }, 4000);
    }

    private void sendFree() {
        LogUtils.serial("?????????????????????" + StringUtility.bytesToHexString(SerialConfigs.CMD_MEDICINE_BALL_EMPTY) + "---");
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
    }

    /**
     * ????????????
     *
     * @param selfCheckResult ????????????
     */
    private void disposeCheck(MedicineBallSelfCheckResult selfCheckResult, BaseStuPair stuPair) {
        boolean isInCorrect = selfCheckResult.isInCorrect();
        BaseDeviceState deviceState = stuPair.getBaseDevice();
        if (isInCorrect) {
            PROMPT_TIMES++;
            sendFree();
            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) {
                checkFlag = false;
                int[] errors = selfCheckResult.getIncorrectPoles();
                for (int i = 1; i < errors.length + 1; i++) {
                    if (errors[i - 1] == 1) {
                        int e = errors[i] + 1;
                        toastSpeak(String.format("%s?????????????????????", "???" + e));
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
                deviceState.setState(BaseDeviceState.STATE_FREE);
                setBegin(1);
            } else {
                deviceState.setState(BaseDeviceState.STATE_ONUSE);
            }

        }

        updateDevice(deviceState);
    }


    private void onResultArrived(int result, boolean fault, BaseStuPair stuPair) {
        stuPair.setEndTime(DateUtil.getCurrentTime() + "");
        stuPair.setResult(result);
        if (testState == TestState.WAIT_RESULT) {
            if (medicineBallSetting.isFullReturn()) {
                if (stuPair.getStudent().getSex() == Student.MALE) {
                    stuPair.setFullMark(result >= Integer.parseInt(medicineBallSetting.getMaleFull()) * 10);
                } else {
                    stuPair.setFullMark(result >= Integer.parseInt(medicineBallSetting.getFemaleFull()) * 10);
                }
            }

            stuPair.setResultState(fault ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);
            updateResult(stuPair);
            updateDevice(stuPair.getBaseDevice());
            // ??????????????????
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));

            testState = TestState.UN_STARTED;


        }
    }

    private void showValidResult(final int result, final boolean fault, final BaseStuPair stuPair) {
        LogUtils.all("??????????????????????????????" + result);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????");
        builder.setNegativeButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ??????????????????
                LogUtils.serial( "?????????????????????" + StringUtility.bytesToHexString(SerialConfigs.CMD_MEDICINE_BALL_STOP) + "---");
                mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));
                testState = TestState.UN_STARTED;
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onResultArrived(result, fault, stuPair);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.life("MedicineBallTestActivity onDestroy");
        mHandler.removeCallbacksAndMessages(null);
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
        mSerialManager.close();
        checkService.shutdown();
    }


    /**
     * ???????????????????????????????????????????????????,????????????????????????????????????
     * ??????????????????????????????????????????
     */
    private void decideBegin() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (checkFlag && testState == TestState.UN_STARTED) {
                    testState = TestState.WAIT_RESULT;
//                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));
                    // ???????????????,?????????1?????????????????????(???????????????????????????????????????????????????,????????????????????????????????????)
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_START));
                    pair.setTestTime(DateUtil.getCurrentTime() + "");
                    mHandler.sendEmptyMessageDelayed(START_DEVICE, 1000);

                    setBegin(0);
                    executorService.shutdown();
                }
            }
        }, 10, 500, TimeUnit.MILLISECONDS);


    }

    @Override
    public void gotoItemSetting() {
        LogUtils.all("???????????????????????????");
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
            checkFlag = true;
        }
    });
}
