package com.feipulai.exam.activity.pushUp;

import android.os.Message;
import android.util.Log;

import com.feipulai.common.jump_rope.task.GetReadyCountDownTimer;
import com.feipulai.common.jump_rope.task.TestingCountDownTimer;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PushUpResiltListener implements SerialDeviceManager.RS232ResiltListener, RadioManager.OnRadioArrivedListener {

    private ExecutorService executor;
    private PushUpDetector deviceDetector;
    private GetReadyCountDownTimer mGetReadyCountDownTimer;
    private TestingCountDownTimer mTestingCountDownTimer;
    private LEDManager ledManager = new LEDManager();
    private Listener listener;
    private int timeLimit;
    private int hostId;
    // 状态  WAIT_BGIN--->TESTING--->FINISHED---->WAIT_BGIN
    private static final int WAIT_BEGIN = 0x0;// 等待开始测试
    private static final int TESTING = 0x1;// 测试过程中
    private static final int FINISHED = 0x2;// 测试过程中
    protected volatile int testState = WAIT_BEGIN;

    public SitPushUpManager deviceManager;
    private PushUpSetting setting;
    private volatile int intervalCount = 0;
    private SitPullLinker linker;
    protected volatile boolean mLinking;
    private volatile long lastResponseTime;

    public PushUpResiltListener(int hostId, PushUpSetting setting, Listener listener) {
        this.listener = listener;
        this.timeLimit = setting.getTestTime();
        this.hostId = hostId;
        this.setting = setting;
        executor = Executors.newFixedThreadPool(3);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_PUSH_UP, setting.getTestType());
        if (setting.getTestType() == PushUpSetting.WIRED_TYPE) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(this);
        } else {
            RadioManager.getInstance().setOnRadioArrived(this);
        }
        startDetect();
    }

    public void setLinker(SitPullLinker linker) {
        this.linker = linker;
    }

    // 重置任务
    private void reset() {
        intervalCount = 0;
        testState = WAIT_BEGIN;
        mGetReadyCountDownTimer = new GetReadyCountDownTimer(5000,
                1000, hostId,
                new GetReadyCountDownTimer.onGetReadyTimerListener() {

                    @Override
                    public void beforeTick(long tick) {
                        deviceManager.startTest((int) tick, /*timeLimit == PushUpSetting.NO_TIME_LIMIT ? 3600 : */timeLimit);
                        listener.onGetReadyTimerTick(tick);
                    }

                    @Override
                    public void afterTick(long tick) {
                    }

                    @Override
                    public void onFinish() {
                        listener.onGetReadyTimerFinish();
                        testState = TESTING;
                        tmpResult = null;
                        lastResponseTime = System.currentTimeMillis();
                        String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(0);
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                        if (timeLimit != PushUpSetting.NO_TIME_LIMIT) {
                            startTestTimer();
                        }
                    }
                });
    }

    private void startTestTimer() {
        mTestingCountDownTimer = new TestingCountDownTimer(
                timeLimit * 1000, 1000, 0, 10,
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
        lastResponseTime = System.currentTimeMillis();
        timeout();
    }

    // 开始测试 可以用作 重新开始
    public void startTest(int tick) {
        stopTimers();
        // 给设备发送开始后结束,在获取成绩的时候就不会一直有之前的成绩存在了
        deviceManager.startTest(tick, timeLimit);
        deviceManager.endTest();
        ledManager.showString(hostId, "", 0, 0, true, true);
        reset();
        executor.execute(mGetReadyCountDownTimer);
    }

    public void stopTest() {
        stopTimers();
        testState = FINISHED;
        deviceManager.endTest();
    }

    public void abandonTest() {
        testState = WAIT_BEGIN;
        stopTimers();
        deviceManager.endTest();
        ledManager.showString(hostId, "", 0, 0, true, true);
    }

    /**
     * 停止计时
     */
    private void stopTimers() {
        if (mGetReadyCountDownTimer != null) {
            mGetReadyCountDownTimer.cancel();
        }
        if (mTestingCountDownTimer != null) {
            mTestingCountDownTimer.cancel();
        }
    }

    /**
     * 停止
     */
    public void stopTotally() {
        stopTimers();
        deviceDetector.stopDetect();
        executor.shutdownNow();
        if (setting.getTestType() == PushUpSetting.WIRED_TYPE) {
            SerialDeviceManager.getInstance().setRS232ResiltListener(this);
            SerialDeviceManager.getInstance().close();
        }
    }


    private volatile SitPushUpStateResult tmpResult;

    @Override
    public void onRadioArrived(Message msg) {
        int testType = setting.getTestType();
        if (testType != PushUpSetting.WIRELESS_TYPE) {
            return;
        }

        if (mLinking && linker.onRadioArrived(msg)) {
            return;
        }
        switch (msg.what) {

            case SerialConfigs.PUSH_UP_GET_STATE:
                if (testState == TESTING || testState == FINISHED) {
                    SitPushUpStateResult result = (SitPushUpStateResult) msg.obj;
                    // Logger.i("PUSH_UP_GET_STATE====>" + result.toString());
                    updateResult(result);
                }
                break;
        }
        // 收到了,就证明连接正常(重新连接了或者连接本身就是正常的)
        deviceDetector.missCount.getAndSet(0);
        listener.onDeviceConnectState(SitPushUpManager.STATE_FREE);
    }

    @Override
    public void onRS232Result(final Message msg) {
        int testType = setting.getTestType();
        if (testType != PushUpSetting.WIRED_TYPE) {
            return;
        }
        switch (msg.what) {

            case SerialConfigs.PUSH_UP_GET_STATE:
                if (testState == TESTING || testState == FINISHED) {
                    SitPushUpStateResult result = (SitPushUpStateResult) msg.obj;
                    // Logger.i("PUSH_UP_GET_STATE====>" + result.toString());
                    updateResult(result);
                }
                break;
        }
        // 收到了,就证明连接正常(重新连接了或者连接本身就是正常的)
        deviceDetector.missCount.getAndSet(0);
        listener.onDeviceConnectState(SitPushUpManager.STATE_FREE);
    }

    private void updateResult(SitPushUpStateResult result) {
        boolean isTimeOut = timeout();
        if (tmpResult == null || result.getResult() != tmpResult.getResult()) {
            intervalCount = (isTimeOut == true && setting.getTimeoutDispose() == 1) ? intervalCount + 1 : intervalCount;
            listener.onScoreArrived(result, intervalCount);
            String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(result.getResult());
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
            if (intervalCount > 0) {
                String intervaLed = "超时:" + ResultDisplayUtils.getStrResultForDisplay(intervalCount);
                ledManager.showString(SettingHelper.getSystemSetting().getHostId(), intervaLed, 1, 2, false, true);
            }
            tmpResult = result;
            lastResponseTime = System.currentTimeMillis();
        }
    }

    private boolean timeout() {
        if (testState != TESTING) {
            return false;
        }
        if (setting.getTestTime() != 0 || setting.getIntervalTime() <= 0 || (setting.getTestType() == PushUpSetting.WIRELESS_TYPE
                && setting.getDeviceSum() > 1)) {
            return false;
        }
        Log.i("james", lastResponseTime + "");
        if (lastResponseTime != 0 && System.currentTimeMillis() - lastResponseTime > setting.getIntervalTime() * 1000) {
            listener.onTimeOut();
            if (setting.getTimeoutDispose() == 0) {
                SoundPlayUtils.play(12);
                listener.onTestingTimerFinish();
                stopTest();
                ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "结束", 12, 1, false, true);
            }
            return true;
        }
        return false;
    }

    /**
     * 设置测试时间
     */
    public void setTimeLimit(PushUpSetting setting) {
        this.setting = setting;
        this.timeLimit = setting.getTestTime();
    }

    /**
     * 开启检测设备状态
     */
    public void startDetect() {
        deviceDetector = null;
        deviceDetector = new PushUpDetector();
        deviceDetector.startDetect();
    }

    private class PushUpDetector {

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private AtomicInteger missCount = new AtomicInteger();
        private volatile boolean detecting = true;

        private void startDetect() {
            detecting = true;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (detecting) {
                        deviceManager.getState(setting.getTestType() == PushUpSetting.WIRED_TYPE ? 0 : 1);
                        int count = missCount.addAndGet(1);
//                        if (count >= 10) {
//                            // 认为设备已经断开了连接
//                            listener.onDeviceConnectState(SitPushUpManager.STATE_DISCONNECT);
//                        }
                        if (count >= 3) {
                            // 认为设备已经断开了连接
                            listener.onDeviceConnectState(SitPushUpManager.STATE_DISCONNECT);
                        }
                        try {
                            Thread.sleep(1000);
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

        void onScoreArrived(SitPushUpStateResult result, int intervalCount);

        void onTimeOut();
    }

}
