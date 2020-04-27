package com.feipulai.exam.activity.pullup.test;

import android.os.Message;

import com.feipulai.common.jump_rope.task.GetReadyCountDownTimer;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.examlogger.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PullUpTestFacade implements RadioManager.OnRadioArrivedListener,
        SitPullLinker.SitPullPairListener {

    private ExecutorService executor;
    private PullUpDetector deviceDetector;
    private GetReadyCountDownTimer mGetReadyCountDownTimer;
    private LEDManager ledManager = new LEDManager();
    private Listener listener;
    private int hostId;
    // 状态  WAIT_BGIN--->TESTING--->FINISHED---->WAIT_BGIN
    private static final int WAIT_BEGIN = 0x0;// 等待开始测试
    private static final int TESTING = 0x1;// 测试过程中
    private static final int FINISHED = 0x2;// 测试过程中
    protected volatile int testState = WAIT_BEGIN;
    private final int TARGET_FREQUENCY;

    private PullUpManager deviceManager = new PullUpManager();
    private SitPullLinker linker;
    private volatile boolean linking;

    public PullUpTestFacade(int hostId, Listener listener) {
        this.hostId = hostId;
        TARGET_FREQUENCY =  SettingHelper.getSystemSetting().getUseChannel();
        RadioChannelCommand command = new RadioChannelCommand(TARGET_FREQUENCY);
        LogUtils.normal(command.getCommand().length+"---"+ StringUtility.bytesToHexString(command.getCommand())+"---引体向上切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(TARGET_FREQUENCY)));
        this.listener = listener;
        executor = Executors.newCachedThreadPool();
        RadioManager.getInstance().setOnRadioArrived(this);
        deviceDetector = new PullUpDetector();
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
                        deviceManager.startTest(1, (int) tick, 0, 0);
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
                        String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(0);
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                    }
                });
    }

    // 开始测试 可以用作 重新开始
    public void startTest() {
        stopTimers();
        ledManager.showString(hostId, "", 0, 0, true, true);
        reset();
        executor.execute(mGetReadyCountDownTimer);
    }

    public void stopTest() {
        stopTimers();
        testState = FINISHED;
        deviceManager.endTest(1);
    }

    public void abandonTest() {
        testState = WAIT_BEGIN;
        stopTimers();
        deviceManager.endTest(1);
        ledManager.showString(hostId, "", 0, 0, true, true);
    }

    private void stopTimers() {
        if (mGetReadyCountDownTimer != null) {
            mGetReadyCountDownTimer.cancel();
        }
    }

    public void stopTotally() {
        stopTimers();
        deviceDetector.stopDetect();
        executor.shutdownNow();
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    private volatile PullUpStateResult tmpResult;

    @Override
    public void onRadioArrived(Message msg) {
        if (linking && linker.onRadioArrived(msg)) {
            return;
        }
        switch (msg.what) {

            case SerialConfigs.PULL_UP_GET_STATE:
                if (testState == TESTING || testState == FINISHED) {
                    PullUpStateResult result = (PullUpStateResult) msg.obj;
                    if (result.getDeviceId() != 1) {
                        return;
                    }
                    listener.onScoreArrived(result);
                    if (tmpResult == null || result.getResult() != tmpResult.getResult()) {
                        String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(result.getResult());
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                        tmpResult = result;
                    }
                }
                // 收到了,就证明连接正常(重新连接了或者连接本身就是正常的)
                deviceDetector.missCount.getAndSet(0);
                listener.onDeviceConnectState(PullUpManager.STATE_FREE);
                break;
        }
    }

    public void link() {
        if (linker == null) {
            linker = new SitPullLinker(ItemDefault.CODE_YTXS, TARGET_FREQUENCY, this);
        }
        linking = true;
        linker.startPair(1);
    }

    public void cancelLinking() {
        linker.cancelPair();
        linking = false;
    }

    @Override
    public void onNoPairResponseArrived() {
        listener.onNoPairResponseArrived();
    }

    @Override
    public void onNewDeviceConnect() {
        listener.onNewDeviceConnect();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int targetFrequency) {
        deviceManager.setFrequency(originFrequency, deviceId, targetFrequency);
    }

    private class PullUpDetector {

        private ExecutorService executor = Executors.newSingleThreadExecutor();
        private AtomicInteger missCount = new AtomicInteger();
        private volatile boolean detecting = true;

        private void startDetect() {
            detecting = true;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (detecting) {
                        if (!linking)
                            deviceManager.getState(1);
                        // 测试过程中不断获取成绩
                        int count = missCount.addAndGet(1);
                        if (count >= 10) {
                            // 认为设备已经断开了连接
                            listener.onDeviceConnectState(PullUpManager.STATE_DISCONNECT);
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

        void onScoreArrived(PullUpStateResult result);

        void onNoPairResponseArrived();

        void onNewDeviceConnect();
    }

}
