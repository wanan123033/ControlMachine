package com.feipulai.exam.activity.medicineBall;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.MedicineBallResult;
import com.feipulai.device.serial.beans.MedicineBallSelfCheckResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseGroupTestActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feipulai.exam.activity.medicineBall.MedicineConstant.END_TEST;
import static com.feipulai.exam.activity.medicineBall.MedicineConstant.GET_SCORE_RESPONSE;
import static com.feipulai.exam.activity.medicineBall.MedicineConstant.SELF_CHECK_RESPONSE;

public class MedicineBallGroupActivity extends BaseGroupTestActivity {
    private MedicineBallSetting medicineBallSetting;
    private SerialDeviceManager mSerialManager;
    private int PROMPT_TIMES = 0;
    private TestState testState = TestState.UN_STARTED;
    private Handler mHandler = new MedicineBallHandler(this);
    private boolean checkFlag = false;
    private ScheduledExecutorService executorService;
//    private ScheduledExecutorService checkService;
    private static final int DELAY = 0X1000;
    private static final int UPDATEDEVICE = 0X1001;
    //????????????????????????
    private BaseStuPair baseStuPair;
    private boolean startFlag;

    @Override
    protected int isShowPenalizeFoul() {
        return medicineBallSetting.isPenalizeFoul() ? View.VISIBLE : View.GONE;
    }

    @Override
    public void initData() {
//        checkService = Executors.newSingleThreadScheduledExecutor();
        medicineBallSetting = SharedPrefsUtil.loadFormSource(this, MedicineBallSetting.class);
        if (null == medicineBallSetting) {
            medicineBallSetting = new MedicineBallSetting();
        }

        mSerialManager = SerialDeviceManager.getInstance();
        mSerialManager.setRS232ResiltListener(resultImpl);
        updateDevice(new BaseDeviceState(BaseDeviceState.STATE_NOT_BEGAIN, 1));
        setTestType(1);
//        if (medicineBallSetting.isPenalize()) {
//            setFaultEnable(true);
//        }
        sendCheck();
        sendFree();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //????????????
//        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                SerialConfigs.CMD_MEDICINE_BALL_SET_BASE_POINT));
//        //????????????
//        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
//                SerialConfigs.CMD_MEDICINE_BALL_SET_START_POINT));
    }

    @Override
    public int setTestCount() {
//        SystemSetting setting = SettingHelper.getSystemSetting();
//        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),baseStuPair.getStudent().getStudentCode());
//        if (setting.isResit() || studentItem.getMakeUpType() == 1){
//            return baseStuPair.getTestNo();
//        }
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return medicineBallSetting.getTestTimes();
        }
    }

    @Override
    public void gotoItemSetting() {
        startActivity(new Intent(this, MedicineBallSettingActivity.class));
    }

    @Override
    public void startTest(BaseStuPair baseStuPair) {
        LogUtils.operation("?????????????????????:" + baseStuPair.getStudent().toString());
        startFlag = true;
        this.baseStuPair = baseStuPair;
        this.baseStuPair.setTestTime(System.currentTimeMillis() + "");
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                SerialConfigs.CMD_MEDICINE_BALL_FAST_EMPTY));
        testState = TestState.UN_STARTED;
        decideBegin();
        //??????????????????
        BaseDeviceState baseDevice = baseStuPair.getBaseDevice();
        if (baseDevice != null)
            baseDevice.setState(BaseDeviceState.STATE_FREE);
        else
            baseDevice = new BaseDeviceState(BaseDeviceState.STATE_FREE, 1);
        baseStuPair.setBaseDevice(baseDevice);
        updateDevice(baseDevice);
        baseStuPair.setTestTime(DateUtil.getCurrentTime() + "");
    }

    @Override
    public int setTestPattern() {

        return medicineBallSetting.getTestPattern();
    }


    public static class MedicineBallHandler extends Handler {
        WeakReference<MedicineBallGroupActivity> weakReference;

        private MedicineBallHandler(MedicineBallGroupActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MedicineBallGroupActivity activity = weakReference.get();
            if (activity == null)
                return;
            switch (msg.what) {

                case SELF_CHECK_RESPONSE:
                    MedicineBallSelfCheckResult selfCheckResult = (MedicineBallSelfCheckResult) msg.obj;
                    LogUtils.operation("?????????????????????:" + selfCheckResult.toString());
                    BaseStuPair pair = new BaseStuPair();
                    BaseDeviceState device = new BaseDeviceState();
                    device.setDeviceId(1);
                    pair.setBaseDevice(device);
                    activity.disposeCheck(selfCheckResult, pair);
                    break;

                case GET_SCORE_RESPONSE:
                    MedicineBallResult result = (MedicineBallResult) msg.obj;
                    LogUtils.operation("?????????????????????:" + result.toString());
                    BaseStuPair basePair = new BaseStuPair();
                    basePair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_END, 1));
                    int beginPoint = Integer.parseInt(SharedPrefsUtil.getValue(activity, "SXQ", "beginPoint", "0"));
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
                    LogUtils.all("????????????????????????");
                    break;
                case DELAY:
                    LogUtils.all("?????????????????????");
                    activity.sendFree();
                    break;
                case UPDATEDEVICE:
                    LogUtils.all("???????????????????????????");
                    BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_ERROR, 0);
                    activity.updateDevice(deviceState);
                    activity.sendFree();
                    break;
                default:
                    break;
            }

        }
    }

    private void sendFree() {
        LogUtils.serial("???????????????????????????:"    + StringUtility.bytesToHexString(SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
        mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
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

    /**
     * ????????????
     */
    private void disposeCheck(MedicineBallSelfCheckResult selfCheckResult, BaseStuPair stuPair) {
        boolean isInCorrect = selfCheckResult.isInCorrect();
        BaseDeviceState deviceState = stuPair.getBaseDevice();
        if (isInCorrect) {
            startFlag = false;
            checkFlag = false;
            PROMPT_TIMES++;
            //??????????????????
            LogUtils.serial("???????????????????????????:" +  StringUtility.bytesToHexString(SerialConfigs.CMD_MEDICINE_BALL_EMPTY));
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_EMPTY));

            if (PROMPT_TIMES >= 2 && PROMPT_TIMES < 4) {
                int[] errors = selfCheckResult.getIncorrectPoles();
                for (int i = 1; i < errors.length + 1; i++) {
                    if (errors[i - 1] == 1) {
                        toastSpeak(String.format("%s?????????????????????", "???" + i));
                    }
                }
            }
            deviceState.setState(BaseDeviceState.STATE_ERROR);
//            mHandler.sendEmptyMessageDelayed(DELAY,1000);
        } else {
            PROMPT_TIMES = 0;
            checkFlag = true;
            if (deviceState.getState() == BaseDeviceState.STATE_ERROR) {
                sendFree();
            }
            if (testState == TestState.UN_STARTED) {
                deviceState.setState(BaseDeviceState.STATE_NOT_BEGAIN);
                setBeginTxt(1);
            } else {
                deviceState.setState(BaseDeviceState.STATE_ONUSE);
            }
            if (testState == TestState.WAIT_RESULT && startFlag) {
                toastSpeak("????????????");
                startFlag = false;
            }
        }
        updateDevice(deviceState);

    }



    private void onResultArrived(int result, boolean fault, BaseStuPair stuPair) {

        stuPair.setEndTime(DateUtil.getCurrentTime() + "");
        stuPair.setResult(result);
        if (testState == TestState.WAIT_RESULT) {
            if (medicineBallSetting.isFullReturn()) {
                if (baseStuPair.getStudent().getSex() == Student.MALE) {
                    stuPair.setFullMark(result >= Integer.parseInt(medicineBallSetting.getMaleFull()) * 10);
                } else {
                    stuPair.setFullMark(result >= Integer.parseInt(medicineBallSetting.getFemaleFull()) * 10);
                }
            }

            stuPair.setResultState(fault ? RoundResult.RESULT_STATE_FOUL : RoundResult.RESULT_STATE_NORMAL);
            updateTestResult(stuPair);
            updateDevice(stuPair.getBaseDevice());
            // ??????????????????
            LogUtils.serial("?????????????????????:"  + StringUtility.bytesToHexString(SerialConfigs.CMD_MEDICINE_BALL_STOP));
            mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232, SerialConfigs.CMD_MEDICINE_BALL_STOP));

            testState = TestState.UN_STARTED;
        }
    }

    private void showValidResult(final int result, final boolean fault, final BaseStuPair stuPair) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????????");
        builder.setNegativeButton("???", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ??????????????????
                LogUtils.serial("?????????????????????:"  + StringUtility.bytesToHexString(SerialConfigs.CMD_MEDICINE_BALL_STOP));
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
        mHandler.removeCallbacksAndMessages(null);
        if (executorService != null && !executorService.isShutdown())
            executorService.shutdown();
        mSerialManager.setRS232ResiltListener(null);
//        checkService.shutdown();

    }

    /**
     * ???????????????????????????????????????????????????,????????????????????????????????????
     * ??????????????????????????????????????????
     */
    private void decideBegin() {
        Log.i("MedicineBallImpl", "decideBegin====");
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (checkFlag && testState == TestState.UN_STARTED) {
                    Log.i("MedicineBallImpl", "decideBegin");
                    testState = TestState.WAIT_RESULT;
//                    updateDevice(new BaseDeviceState(BaseDeviceState.STATE_ONUSE, 1));
                    // ???????????????,?????????1?????????????????????(???????????????????????????????????????????????????,????????????????????????????????????)
                    mSerialManager.sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,
                            SerialConfigs.CMD_MEDICINE_BALL_START));

                    executorService.shutdown();
                }
            }
        }, 500, 500, TimeUnit.MILLISECONDS);


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
        }

    });
}

