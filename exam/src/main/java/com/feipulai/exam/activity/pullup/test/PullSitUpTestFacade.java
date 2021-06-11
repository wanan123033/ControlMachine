package com.feipulai.exam.activity.pullup.test;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.feipulai.common.jump_rope.task.GetReadyCountDownTimer;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.ArmStateResult;
import com.feipulai.device.serial.beans.PullUpStateResult;
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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;

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
    private static final String TAG = "PullSitUpTestFacade";
    private PullUpManager deviceManager = new PullUpManager();
    private SitPushUpManager sitUpManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP_HAND);
    private volatile boolean linking;
    private List<ArmStateResult> sitUpLists = new ArrayList<>();

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
                        tmp = 0;
                        invalid = 0;
                        sitUpLists.clear();
                        listener.onGetReadyTimerFinish();
                        testState = TESTING;
                        tmpResult = null;
//                        sitUpManager.getSitUpHandAngle(2);
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
        tmp = 0;
        invalid = 0;
        LogUtils.operation("开始测试");
    }

    public void stopTest() {
        stopTimers();
        timeCount = 0;
        testState = FINISHED;
        deviceManager.endTest(1);
        sitUpManager.endTest();
        tmp = 0;
        sitUpLists.clear();
        invalid = 0;
        listener.onInvalid(invalid);
        LogUtils.operation("结束测试");
    }

    public void abandonTest() {
        testState = WAIT_BEGIN;
        stopTimers();
        deviceManager.endTest(1);
        sitUpManager.endTest();
        ledManager.showString(hostId, "", 0, 0, true, true);
        tmp = 0;
        invalid = 0;
        sitUpLists.clear();
        LogUtils.operation("放弃测试");
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

    private volatile PullUpStateResult tmpResult = new PullUpStateResult();


    @Override
    public void onRadioArrived(Message msg) {

        switch (msg.what) {

            case SerialConfigs.PULL_UP_GET_STATE:
                if (testState == TESTING || testState == FINISHED) {
                    PullUpStateResult result = (PullUpStateResult) msg.obj;

                    if (result.getDeviceId() != 1) {
                        return;
                    }
//                    tmpResult = result;
//                    listener.onScoreArrived(result);
                    if (tmpResult == null || result.getResult() != tmpResult.getResult()) {
                        tmpResult = result;
//                        updateResult(tmpResult);
                    }

                }

                // 收到了,就证明连接正常(重新连接了或者连接本身就是正常的)
                deviceDetector.missCount.getAndSet(0);
                listener.onDeviceConnectState(PullUpManager.STATE_FREE);
                break;
            case SerialConfigs.PULL_UP_GET_ANGLE_DATA:
                if (testState == TESTING || testState == FINISHED) {
                    ArmStateResult result = (ArmStateResult) msg.obj;
                    if (result.getDeviceId() != 2) {
                        return;
                    }
                    sitUpLists.add(result);
                    if(result.getResult() != hand){
//                        updateResult(result);
                        hand = result.getResult();
                        high = result;
                        updateResult();
                    }

                    LogUtils.normal("手臂检测====" + result.toString());
                }

                break;
        }
    }




    private synchronized void updateResult(PullUpStateResult pull) {
        if (sitUpLists.size() == 0) {
            return;
        }
        high = getHeightSitResult();
        low = getLowSitResult();
        LogUtils.normal(pull.getResult() + "==========引体向上数据变化更新===========" + tmp + "hand==" + hand);
        LogUtils.normal(high.getAngle() + "======高点与低点角度===========" + low.getAngle());
//        int t = hand - handCheck;
        if (Math.abs(high.getAngle() - low.getAngle()) > 55) {
//            if (t > 1) {
//                tmp = high.getResult();
//            } else {
//                tmp++;
//            }
            tmp++;
        } else{
            //此次操作违规
            invalid++;
        }
        pull.getTotalCountNum();
        pull.setValidCountNum(tmp);
        listener.onScoreArrived(pull);
        String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(pull.getResult());
        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
        sitUpLists.clear();
    }

    private volatile int tmp;
    private volatile int invalid;
    private volatile int hand;
    private ArmStateResult high = new ArmStateResult();
    private ArmStateResult low;
    private final int sendPull = 0xf1;
    private volatile int delayPull;
    private  void updateResult(ArmStateResult armState) {
        if (tmpResult == null)
            return;
        high = getHeightSitResult();
        low = getLowSitResult();
        if (Math.abs(high.getAngle() - low.getAngle()) > 55) {
            if (tmpResult.getResult()>tmp){
                tmp = tmpResult.getResult();
                tmpResult.setValidCountNum(armState.getResult()-invalid);
                sitUpLists.clear();
                listener.onScoreArrived(tmpResult);
                String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(tmpResult.getResult());
                ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
            }else {
                //此处可能是还没有收到数值变化，需要进一步判断再隔0.5s判断
                mHandler.sendEmptyMessageDelayed(sendPull,500);
                delayPull = armState.getResult();
            }

        }
    }

    private void updateResult(){
        Observable.zip(getStringObservable(), getIntegerObservable(), new BiFunction<PullUpStateResult, ArmStateResult, Integer>() {
            @Override
            public Integer apply(@NonNull PullUpStateResult s, @NonNull ArmStateResult result) throws Exception {
                if (s.getResult()-tmp>2){
                    invalid += s.getResult()-tmp;
                    LogUtils.normal("invalid:"+invalid);
                }
                tmp = s.getResult();

                return s.getResult();
            }
        }).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer r) throws Exception {
                tmpResult.setValidCountNum(r-invalid);
                listener.onScoreArrived(tmpResult);
                String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(tmpResult.getResult());
                ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
            }
        });
    }
    private Observable<PullUpStateResult> getStringObservable() {
        return Observable.create(new ObservableOnSubscribe<PullUpStateResult>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<PullUpStateResult> e) throws Exception {
                if (!e.isDisposed() && tmpResult!= null) {
                    e.onNext(tmpResult);
                    Log.i("pullArm",tmpResult.toString());
                    LogUtils.normal(tmpResult.toString());
                }
            }
        });
    }

    private Observable<ArmStateResult> getIntegerObservable() {
        return Observable.create(new ObservableOnSubscribe<ArmStateResult>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<ArmStateResult> e) throws Exception {
                if (!e.isDisposed() && high!=null) {
                    e.onNext(high);
                    Log.i("pullArm",high.toString());
                    LogUtils.normal(high.toString());
                }
            }
        });
    }

    private Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case sendPull:
                    if (tmpResult.getResult()>tmp){
                        tmp = tmpResult.getResult();
                    }else {
                        invalid++;
                    }
                    tmpResult.setValidCountNum(delayPull-invalid);
                    sitUpLists.clear();
                    listener.onScoreArrived(tmpResult);
                    String displayInLed = "成绩:" + ResultDisplayUtils.getStrResultForDisplay(tmpResult.getResult());
                    ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                    break;
            }
        }
    };

    private ArmStateResult getHeightSitResult() {
//        for (ArmStateResult armState : sitUpLists) {
//            if (getBitArray(armState.getAngleState())[1]==1 || armState.getAngle()>60){
//                return true;
//            }
//        }
//        return false;

        ArmStateResult k = sitUpLists.get(0);

        for (int i = 0; i < sitUpLists.size() - 1; i++) {
            if (k.getAngle() < sitUpLists.get(i + 1).getAngle()) {
                k = sitUpLists.get(i + 1);

            }
        }
        return k;

    }

    private ArmStateResult getLowSitResult() {

        ArmStateResult k = sitUpLists.get(0);
        for (int i = 0; i < sitUpLists.size() - 1; i++) {
            if (k.getAngle() > sitUpLists.get(i + 1).getAngle()) {
                k = sitUpLists.get(i + 1);
            }
        }
        return k;
    }

    private byte[] getBitArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    public void setLinking(boolean linking) {
        if (!linking) {
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

    private int timeCount = 0;

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
                        if (!linking) {
                            deviceManager.getState(1);
                            if (timeCount % 10 == 0) {
                                sitUpManager.getState(2);
                            }
//                            sitUpManager.getSitUpHandAngle(2);
                            // 测试过程中不断获取成绩
                            int count = missCount.addAndGet(1);
                            timeCount++;
                            if (count >= 5) {
                                // 认为设备已经断开了连接
                                listener.onDeviceConnectState(PullUpManager.STATE_DISCONNECT);
                            }
                            try {
                                Thread.sleep(500);
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

        void onInvalid(int invalid);
    }

}
