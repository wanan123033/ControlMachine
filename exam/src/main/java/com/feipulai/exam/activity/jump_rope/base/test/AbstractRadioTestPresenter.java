package com.feipulai.exam.activity.jump_rope.base.test;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.feipulai.common.jump_rope.facade.CountTimingTestFacade;
import com.feipulai.common.jump_rope.task.GetDeviceStatesTask;
import com.feipulai.common.jump_rope.task.LEDContentGenerator;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.activity.jump_rope.DeviceDispatcher;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractRadioTestPresenter<Setting>
        implements RadioTestContract.Presenter,
        GetDeviceStatesTask.OnGettingDeviceStatesListener,
        RadioManager.OnRadioArrivedListener,
        CountTimingTestFacade.Listener,
        LEDContentGenerator,
        Handler.Callback {

    private static final int FINAL_RESULT_GOT = 0x5;
    protected static final int INVALID_PIV = -100;
    protected CountTimingTestFacade facade;

    // 状态  WAIT_BGIN--->TEST_COUNTING--->TESTING--->WAIT_MACHINE_RESULTS--->WAIT_CONFIRM_RESULTS---->WAIT_BGIN
    protected static final int WAIT_BGIN = 0x0;// 等待开始测试
    protected static final int TEST_COUNTING = 0x1;// 等待开始测试
    protected static final int TESTING = 0x2;// 测试过程中
    protected static final int WAIT_MACHINE_RESULTS = 0x3;// 测试结束,等待获取机器成绩
    protected static final int WAIT_CONFIRM_RESULTS = 0x4;// 已获取到机器成绩,等待用户点击确认成绩(此时成绩已经保存)
    protected volatile int testState = WAIT_BGIN;

    protected volatile int[] currentConnect;
    protected volatile int[] endGetResultPairs;//结束后缓存收到数据设备
    private String testDate;
    protected List<StuDevicePair> pairs;
    protected Setting setting;
    protected SystemSetting systemSetting;
    private Context context;
    protected RadioTestContract.View<Setting> view;
    protected int[] deviceIdPIV;
    protected boolean mLinking;
    private DeviceDispatcher deviceDispatcher;
    protected int focusPosition;
    protected Handler handler;
    private HandlerThread handlerThread;

    protected AbstractRadioTestPresenter(Context context, RadioTestContract.View<Setting> view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void start() {
        pairs = TestCache.getInstance().getTestingPairs();
        setting = getSetting();
        deviceDispatcher = new DeviceDispatcher(TestConfigs.getMaxTestCount(context));
        systemSetting = SettingHelper.getSystemSetting();
        int size = pairs.size();
        int possibleMaxDeviceId = pairs.get(size - 1).getBaseDevice().getDeviceId();
        currentConnect = new int[possibleMaxDeviceId + 1];
        endGetResultPairs = new int[possibleMaxDeviceId + 1];
        // 记录每个设备id在recyclerview中的位置  deviceIdPIV[deviceId] = piv
        deviceIdPIV = new int[possibleMaxDeviceId + 1];
        Arrays.fill(deviceIdPIV, INVALID_PIV);
        int piv = 0;
        for (StuDevicePair pair : pairs) {
            deviceIdPIV[pair.getBaseDevice().getDeviceId()] = piv;
            piv++;
        }

        handlerThread = new HandlerThread("handlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), this);

        view.initView(pairs, setting);

        CountTimingTestFacade.Builder builder = new CountTimingTestFacade.Builder();
        facade = builder.countStartTime(getCountStartTime())
                .countFinishTime(getCountFinishTime())
                .testTime(getTestTimeFromSetting())
                .hostId(systemSetting.getHostId())
                .setLEDContentGenerator(this)
                .setOnGettingDeviceStatesListener(this)
                .setSize(size)
                .build();
        facade.setListener(this);
        startTest();
        RadioManager.getInstance().setOnRadioArrived(this);
    }

    @Override
    public void startTest() {
        Logger.i("开始测试,测试考生设备信息:" + pairs.toString());
        testDate = System.currentTimeMillis() + "";
        resetDevices();
        view.setViewForStart();
        facade.start();
        testState = TEST_COUNTING;
        ToastUtils.showShort("测试开始,请勿退出当前界面");
        onTestStarted();
    }

    @Override
    public void stopNow() {
        facade.stopTotally();
        handlerThread.quit();
    }

    @Override
    public void quitTest() {
        stopNow();
        resetDevices();
        view.quitTest();
    }

    @Override
    public void restartTest() {
        facade.stop();
        resetDeviceResults();
        testState = WAIT_BGIN;
        startTest();
    }

    private void resetDeviceResults() {
        for (StuDevicePair pair : pairs) {
            BaseDeviceState deviceState = pair.getBaseDevice();
            if (deviceState.getState() == BaseDeviceState.STATE_STOP_USE) {
                deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
            }
            pair.setDeviceResult(null);
        }
        view.updateStates();
    }

    @Override
    public boolean checkFinalResults() {
        return InteractUtils.checkFinalResults(pairs);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {

            case FINAL_RESULT_GOT:
                if (checkFinalResults()) {
                    Logger.i("获取到最终成绩,考生设备信息:" + pairs.toString());
                    testState = WAIT_CONFIRM_RESULTS;
                    onMachineResultArrived();
                    view.showForConfirmResults();
                    view.showWaitFinalResultDialog(false);
                } else {
                    // 继续等待最终成绩
                    handler.sendEmptyMessageDelayed(FINAL_RESULT_GOT, 2000);
                }
                return true;
        }
        return false;
    }

    @Override
    public void confirmResults() {
        onResultConfirmed();
        resetDevices();
        Logger.i("用户手动点击确认成绩,考生设备信息:" + pairs.toString());
        // view.tickInUI("");

        for (StuDevicePair pair : pairs) {
            if (pair.getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT) {
                view.showDisconnectForConfirmResults();
                return;
            }
        }
        saveResults();// 必须先保存成绩,再分配设备
        dispatchDevices();
        view.enableChangeBad(true);
    }

    @Override
    public void saveResults() {
        Logger.i("保存成绩,考生设备信息:" + pairs.toString());
        // 必须在状态切换之前保存成绩
        InteractUtils.saveResults(pairs, testDate);
        InteractUtils.uploadResults();
        testState = WAIT_BGIN;
        resetDeviceResults();
        view.enableFinishTest(true);
        view.enableConfirmResults(false);
    }

    @Override
    public void dispatchDevices() {
        boolean sucess = deviceDispatcher.dispatchDevice(pairs, getGroupModeFromSetting());
        view.updateStates();
        if (!sucess) {
            finishTest();
        } else {
            view.enableStartTest(true);
        }
    }

    @Override
    public void finishTest() {
        view.finishTest();
        new LEDManager().clearScreen(TestConfigs.sCurrentItem.getMachineCode(), systemSetting.getHostId());
    }

    @Override
    public int stateOfPosition(int position) {
        return pairs.get(position).getBaseDevice().getState();
    }

    @Override
    public void setFocusPosition(int position) {
        focusPosition = position;
    }

    @Override
    public void onStateRefreshed() {
        // 用来更新断开连接状态
        int oldState;
        for (int i = 0; i < pairs.size(); i++) {
            StuDevicePair pair = pairs.get(i);
            BaseDeviceState deviceState = pair.getBaseDevice();
            oldState = deviceState.getState();
            if ((currentConnect[deviceState.getDeviceId()] == 0 && endGetResultPairs[deviceState.getDeviceId()] == 0)
                    && oldState != BaseDeviceState.STATE_DISCONNECT
                    && oldState != BaseDeviceState.STATE_STOP_USE) {
                deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
                view.updateSpecificItem(i);
            }
        }
        int length = currentConnect.length;// 必须是这个长度
        currentConnect = new int[length];
    }

    @Override
    public int getDeviceCount() {
        return pairs.size();
    }

    @Override
    public void stopUse() {
        CheckUtils.stopUse(pairs, focusPosition);
        view.updateSpecificItem(focusPosition);
    }

    @Override
    public void resumeUse() {
        CheckUtils.resumeUse(pairs, focusPosition);
        view.updateSpecificItem(focusPosition);
    }

    @Override
    public void onGetReadyTimerTick(long tick) {
        view.tickInUI(tick + "");
        testCountDown(tick);
    }

    @Override
    public void onGetReadyTimerFinish() {
        view.enableStopUse(true);
        view.tickInUI("开始");
        testState = TESTING;
    }

    @Override
    public void onTestingTimerTick(final long tick) {
        view.tickInUI(DateUtil.formatTime(tick * 1000, "mm:ss"));
        if (tick <= 5) {
            view.enableStopRestartTest(false);
        } else {
            view.enableStopRestartTest(true);
        }
    }

    @Override
    public void onTestingTimerFinish() {
        view.tickInUI("结束");
        testState = WAIT_MACHINE_RESULTS;
        view.showWaitFinalResultDialog(true);
        Logger.i("计时结束,测试考生设备信息:" + pairs.toString());
        handler.sendEmptyMessageDelayed(FINAL_RESULT_GOT, 2000);
    }

    @Override
    public String generate(int position) {
        return InteractUtils.generateLEDTestString(pairs, position);
    }

    protected abstract int getCountStartTime();

    protected abstract int getCountFinishTime();

    protected abstract Setting getSetting();

    protected abstract int getTestTimeFromSetting();

    protected abstract void resetDevices();

    protected abstract int getGroupModeFromSetting();

    protected abstract void testCountDown(long tick);

    protected abstract void onResultConfirmed();

    protected abstract void onTestStarted();

    protected abstract void onMachineResultArrived();

}
