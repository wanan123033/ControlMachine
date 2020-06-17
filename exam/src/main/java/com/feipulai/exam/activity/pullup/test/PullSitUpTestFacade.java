package com.feipulai.exam.activity.pullup.test;

import android.os.Message;

import com.feipulai.common.jump_rope.task.GetReadyCountDownTimer;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.utils.FileUtils;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 引体向上与仰卧起坐合并
 */
public class PullSitUpTestFacade implements RadioManager.OnRadioArrivedListener,
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
    private SitPushUpManager sitUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
    private volatile boolean linking;
    private List<SitPushUpStateResult> sitUpLists = new ArrayList<>();
    public PullSitUpTestFacade(int hostId, Listener listener) {
        this.hostId = hostId;
        TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
        RadioChannelCommand command = new RadioChannelCommand(TARGET_FREQUENCY);
        LogUtils.normal(command.getCommand().length + "---" + StringUtility.bytesToHexString(command.getCommand()) + "---引体向上切频指令");
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
                        sitUpManager.startTest((int) tick, 0);
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
                        tmpTime = DateUtil.getCurrentTime();
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
        sitUpManager.endTest();
    }

    public void abandonTest() {
        testState = WAIT_BEGIN;
        stopTimers();
        deviceManager.endTest(1);
        sitUpManager.endTest();
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

        switch (msg.what) {

            case SerialConfigs.PULL_UP_GET_STATE:
                if (testState == TESTING || testState == FINISHED) {
                    PullUpStateResult result = (PullUpStateResult) msg.obj;
                    FileUtils.log(result.toString());
                    if (result.getDeviceId() != 1) {
                        return;
                    }
//                    listener.onScoreArrived(result);
                    if (tmpResult == null || result.getResult() != tmpResult.getResult()) {
//                        String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(result.getResult());
//                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                        tmpResult = result;
                        updateResult(tmpResult);
                    }

                }

                // 收到了,就证明连接正常(重新连接了或者连接本身就是正常的)
                deviceDetector.missCount.getAndSet(0);
                listener.onDeviceConnectState(PullUpManager.STATE_FREE);
                break;
            case SerialConfigs.PULL_UP_GET_ANGLE_DATA:
                if (testState == TESTING || testState == FINISHED) {
                    SitPushUpStateResult result = (SitPushUpStateResult) msg.obj;
                    if (result.getDeviceId() != 2) {
                        return;
                    }
                    sitUpLists.add(result);
                }

                break;
        }
    }


    private int tmp;
    private long tmpTime;
    private void updateResult(PullUpStateResult pull) {
        tmpTime = DateUtil.getCurrentTime()-tmpTime;
        if (getHeightSitResult() && getLowSitResult()){
            tmp++;
            pull.setValidCountNum(tmp);
            listener.onScoreArrived(tmpResult);
            String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(tmpResult.getResult());
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
            tmpTime = DateUtil.getCurrentTime();
            sitUpLists.clear();
        }else {
            //此次操作违规
        }

    }

    private boolean getHeightSitResult(){
        for (SitPushUpStateResult sitUpList : sitUpLists) {
            if (sitUpList.getHightState() == 1 && sitUpList.getAngle()> 45){
                return true;
            }
        }
        return false;
    }

    private boolean getLowSitResult(){

        for (SitPushUpStateResult sitUpList : sitUpLists) {
            if (sitUpList.getHightState() == 0 && sitUpList.getAngle() < -10){
                return true;
            }
        }

//        int k = 0;
//        for (int i = 0; i < sitUpLists.size(); i++) {
//            for (int j = 0; j < sitUpLists.size()-i-1; j++) {
//                if (sitUpLists.get(j).getResult()< sitUpLists.get(j+1).getResult()){
//                    k = sitUpLists.get(j).getResult();
//                }else {
//                    k = sitUpLists.get(j+1).getResult();
//                }
//            }
//        }
        return false;
    }

    public void setLinking(boolean linking) {
        if (!linking){
            RadioManager.getInstance().setOnRadioArrived(this);
            RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(TARGET_FREQUENCY)));
        }
        this.linking = linking;

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
                        if (!linking){
                            deviceManager.getState(1);
                            sitUpManager.getSitUpHandAngle(2);
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
