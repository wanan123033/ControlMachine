package com.feipulai.exam.activity.volleyball;

import android.os.Message;

import com.feipulai.common.jump_rope.task.GetReadyCountDownTimer;
import com.feipulai.common.jump_rope.task.TestingCountDownTimer;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.VolleyBallManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.VolleyBallResult;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class VolleyBallTestFacade implements SerialDeviceManager.RS232ResiltListener {

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

    private VolleyBallManager deviceManager = new VolleyBallManager();
    private VolleyBallSetting setting;
    private boolean isSetDevice = false;

    public VolleyBallTestFacade(int hostId, VolleyBallSetting setting, Listener listener) {
        this.listener = listener;
        this.setting = setting;
        this.hostId = hostId;
        executor = Executors.newCachedThreadPool();
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
        deviceDetector = new VolleyBallDetector();
        deviceDetector.startDetect();
    }

    // 重置任务
    private void reset() {
        testState = WAIT_BEGIN;
        mGetReadyCountDownTimer = new GetReadyCountDownTimer(5000,
                1000, hostId,
                new GetReadyCountDownTimer.onGetReadyTimerListener() {

                    @Override
                    public void beforeTick(long tick) {
                        listener.onGetReadyTimerTick(tick);
                    }

                    @Override
                    public void afterTick(long tick) {
                    }

                    @Override
                    public void onFinish() {
                        listener.onGetReadyTimerFinish();
                        deviceManager.startTest();
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
                            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "结束", 12, 1, false, true);
                        } else {
                            listener.onTestingTimerTick(tick);
                            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), String.format("%3d", tick), 12, 1, false, true);
                        }
                    }
                });
        executor.execute(mTestingCountDownTimer);
    }

    // 开始测试 可以用作 重新开始
    public void startTest() {
        stopTimers();

        // 给设备发送开始后结束,在获取成绩的时候就不会一直有之前的成绩存在了
        deviceManager.startTest();
        deviceManager.stopTest();

        ledManager.showString(hostId, "", 0, 0, true, true);

        reset();
        executor.execute(mGetReadyCountDownTimer);
    }

    public void stopTest() {
        stopTimers();

        testState = FINISHED;
        deviceManager.stopTest();
        deviceManager.getScore();
    }

    public void abandonTest() {
        testState = WAIT_BEGIN;
        stopTimers();
        deviceManager.stopTest();
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
            case SerialConfigs.VOLLEYBALL_SET_DEVICE_RESPONSE:
                isSetDevice = true;
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

    //    public void setTimeLimit(int testTime) {
//        this.timeLimit = testTime;
//        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
//    }
    public void setVolleySetting(VolleyBallSetting setting) {
        this.setting = setting;
        SerialDeviceManager.getInstance().setRS232ResiltListener(this);
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
                        if (testState == TESTING) {
                            deviceManager.getScore();
                        } else {
                            deviceManager.emptyCommand();
                            if (!isSetDevice) {
                                deviceManager.setDeviceMode(setting.getTestPattern(),
                                        setting.getTestPattern() == 0 ? VolleyBallSetting.ANTIAIRCRAFT_POLE : VolleyBallSetting.WALL_POLE,
                                        setting.getTestPattern() == 0 ? 1 : 0);
                            }

                        }
                        int count = missCount.addAndGet(1);
                        if (count >= 10) {
                            // 认为设备已经断开了连接
                            listener.onDeviceConnectState(VolleyBallManager.VOLLEY_BALL_DISCONNECT);
                        }
                        try {
                            Thread.sleep(100);
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

    public interface Listener {

        void onDeviceConnectState(int state);

        void onGetReadyTimerTick(long tick);

        void onGetReadyTimerFinish();

        void onTestingTimerTick(long tick);

        void onTestingTimerFinish();

        void onScoreArrived(VolleyBallResult result);
    }

}
