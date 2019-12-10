package com.feipulai.exam.activity.volleyball;

import android.os.Message;

import com.feipulai.common.jump_rope.task.GetReadyCountDownTimer;
import com.feipulai.common.jump_rope.task.TestingCountDownTimer;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.VolleyBallManager;
import com.feipulai.device.manager.VolleyBallRadioManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.VolleyBallCheck;
import com.feipulai.device.serial.beans.VolleyBallResult;
import com.feipulai.device.serial.beans.VolleyPair868Result;
import com.feipulai.device.serial.beans.VolleyPairResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VolleyBallTestFacade implements SerialDeviceManager.RS232ResiltListener, RadioManager.OnRadioArrivedListener {

    private ExecutorService executor;
    private VolleyBallDetector deviceDetector;
    private GetReadyCountDownTimer mGetReadyCountDownTimer;
    private TestingCountDownTimer mTestingCountDownTimer;
    private LEDManager ledManager = new LEDManager();
    private Listener listener;
    private int hostId;
    // 状态  WAIT_BGIN--->TESTING--->FINISHED---->WAIT_BGIN
    private static final int WAIT_BEGIN = 0x0;// 等待开始测试
    private static final int TESTING = 0x1;// 测试过程中
    private static final int FINISHED = 0x2;// 测试过程中
    protected volatile int testState = WAIT_BEGIN;

    public VolleyBallManager deviceManager;
    private VolleyBallSetting setting;
    protected volatile boolean mLinking;
    private SitPullLinker linker;


    public VolleyBallTestFacade(int hostId, VolleyBallSetting setting, Listener listener) {
        this.listener = listener;
        this.setting = setting;
        this.hostId = hostId;

        deviceManager = new VolleyBallManager(setting.getType());
        executor = Executors.newCachedThreadPool();
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
        RadioManager.getInstance().setOnRadioArrived(this);
        deviceDetector = new VolleyBallDetector();
        deviceDetector.startDetect();
    }

    public void setLinker(SitPullLinker linker) {
        this.linker = linker;
    }


    // 重置任务
    private void reset() {
        testState = WAIT_BEGIN;
        mGetReadyCountDownTimer = new GetReadyCountDownTimer(5000,
                1000, hostId,
                new GetReadyCountDownTimer.onGetReadyTimerListener() {

                    @Override
                    public void beforeTick(long tick) {

                        if (tick != 0) {
                            VolleyBallRadioManager.getInstance().startTime(hostId, 1, (int) tick, setting.getTestTime());
                        }

                        listener.onGetReadyTimerTick(tick);
                    }

                    @Override
                    public void afterTick(long tick) {
                    }

                    @Override
                    public void onFinish() {
                        listener.onGetReadyTimerFinish();
                        deviceManager.startTest(hostId, 1, 0, setting.getTestTime());
                        testState = TESTING;
                        tmpResult = null;
                        String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(0);
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                        if (setting.getTestTime() != VolleyBallSetting.NO_TIME_LIMIT) {
                            startTestTimer();
                        }
                    }
                });
    }

    private void startTestTimer() {
        mTestingCountDownTimer = new TestingCountDownTimer(
                setting.getTestTime() * 1000, 1000, 0, 10,
                new TestingCountDownTimer.OnTestingCountDownListener() {

                    @Override
                    public void onTick(long tick) {
                        if (tick == 0) {
                            listener.onTestingTimerFinish();
                            stopTest();
                            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "结束 ", 10, 3, false, true);
                        } else {
                            listener.onTestingTimerTick(tick);
                            byte[] data = new byte[16];
                            try {
                                byte[] resultData = DateUtil.formatTime(tick * 1000, "mm:ss").getBytes("GB2312");
//                                int srcX = LEDManager.isInt(DateUtil.formatTime(tick * 1000, "mm:ss")) ? 12 : 10;
                                System.arraycopy(resultData, 0, data, 10, resultData.length);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 3, false, true);
                        }
                    }
                });
        executor.execute(mTestingCountDownTimer);
    }

    // 开始测试 可以用作 重新开始
    public void startTest() {
        stopTimers();

        // 给设备发送开始后结束,在获取成绩的时候就不会一直有之前的成绩存在了
        deviceManager.startTest(hostId, 1, 0, setting.getTestTime());
        deviceManager.stopTest(hostId, 1, setting.getTestTime());

        ledManager.showString(hostId, "", 0, 0, true, true);

        reset();
        executor.execute(mGetReadyCountDownTimer);
    }

    public void stopTest() {
        stopTimers();
        testState = FINISHED;
        deviceManager.stopTest(hostId, 1, setting.getTestTime());
        deviceManager.getScore(hostId, 1);
    }

    public void abandonTest() {
        testState = WAIT_BEGIN;
        stopTimers();
        deviceManager.stopTest(hostId, 1, setting.getTestTime());
        VolleyBallRadioManager.getInstance().deviceFree(hostId, 1);
        ledManager.showString(hostId, "", 0, 0, true, true);
    }

    private void stopTimers() {
        if (mGetReadyCountDownTimer != null) {
            mGetReadyCountDownTimer.cancel();
        }
        if (mTestingCountDownTimer != null) {
            mTestingCountDownTimer.cancel();
        }
    }

    public void stopTotally() {
        stopTimers();
        deviceDetector.stopDetect();
        executor.shutdownNow();
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
        SerialDeviceManager.getInstance().close();
    }

    private volatile VolleyBallResult tmpResult;

    @Override
    public void onRS232Result(final Message msg) {
        switch (msg.what) {
//            case SerialConfigs.VOLLEYBALL_CHECK_RESPONSE:
//                listener.checkDevice((VolleyBallCheck) msg.obj);
//                break;
            case SerialConfigs.VOLLEYBALL_RESULT_RESPONSE:
                if (testState == TESTING || testState == FINISHED) {
                    VolleyBallResult result = (VolleyBallResult) msg.obj;
                    listener.onScoreArrived(result);
                    if (tmpResult == null || result.getResult() != tmpResult.getResult()) {
                        String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(result.getResult());
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                        tmpResult = result;
                    }
                }
                break;
            // case SerialConfigs.VOLLEYBALL_EMPTY_RESPONSE:
            // case SerialConfigs.VOLLEYBALL_START_RESPONSE:
            // case SerialConfigs.VOLLEYBALL_STOP_RESPONSE:
            //     break;
        }
        // 收到了,就证明连接正常(重新连接了或者连接本身就是正常的)
        deviceDetector.missCount.getAndSet(0);
        listener.onDeviceConnectState(VolleyBallManager.VOLLEY_BALL_CONNECT);
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (mLinking && linker.onRadioArrived(msg)) {
            return;
        }
        if (msg.obj instanceof VolleyPair868Result) {
            VolleyPair868Result pair868Result = (VolleyPair868Result) msg.obj;
            if (pair868Result.getDeviceId() != 1)
                return;

            if (pair868Result.getState() == VolleyPair868Result.STATE_TIMING || pair868Result.getState() == VolleyPair868Result.STATE_TIME_END
                    || pair868Result.getState() == VolleyPair868Result.STATE_COUNTING || pair868Result.getState() == VolleyPair868Result.STATE_COUNT_END) {
                VolleyBallResult result = new VolleyBallResult();
                result.setResult(pair868Result.getScore());
                listener.onScoreArrived(result);
                if (testState != WAIT_BEGIN && (tmpResult == null || result.getResult() != tmpResult.getResult())) {
                    String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(result.getResult());
                    ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                    tmpResult = result;
                }
            }

        }
        if (msg.obj instanceof VolleyPair868Result || msg.obj instanceof VolleyPairResult) {
            deviceDetector.missCount.getAndSet(0);
            listener.onDeviceConnectState(VolleyBallManager.VOLLEY_BALL_CONNECT);
        }
        if (msg.what == SerialConfigs.VOLLEY_BALL_SELFCHECK && msg.obj instanceof VolleyPair868Result) {
            VolleyPair868Result result = (VolleyPair868Result) msg.obj;
            VolleyBallCheck check = new VolleyBallCheck();
            check.setPositionList(result.getPositionList());
            check.setDeviceId(result.getDeviceId());
            check.setPoleNum(result.getPoleNum());
            check.setDeviceType(result.getDeviceType());
            listener.checkDevice(check);
        }
    }


    public void setVolleySetting(VolleyBallSetting setting) {
        this.setting = setting;
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
        RadioManager.getInstance().setOnRadioArrived(this);
    }


    private class VolleyBallDetector {

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private AtomicInteger missCount = new AtomicInteger();
        private volatile boolean detecting = true;

        private void startDetect() {
            detecting = true;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (detecting) {
                        // 测试过程中不断获取成绩
                        if (setting.getType() == 0) {
                            if (testState == TESTING) {
                                deviceManager.getScore(hostId, 1);
                            } else {
                                deviceManager.emptyCommand();
                            }
                        } else {
                            deviceManager.getScore(hostId, 1);
                        }

                        int count = missCount.addAndGet(1);
                        if (count >= 10) {
                            // 认为设备已经断开了连接
                            listener.onDeviceConnectState(VolleyBallManager.VOLLEY_BALL_DISCONNECT);
                        }
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        private void stopDetect() {
            detecting = false;
            executor.shutdown();
        }

    }

    public void checkDevice() {
        deviceManager.checkDevice(hostId, 1);
    }

    public interface Listener {

        void onDeviceConnectState(int state);

        void onGetReadyTimerTick(long tick);

        void onGetReadyTimerFinish();

        void onTestingTimerTick(long tick);

        void onTestingTimerFinish();

        void onScoreArrived(VolleyBallResult result);

        void checkDevice(VolleyBallCheck check);
    }

}
