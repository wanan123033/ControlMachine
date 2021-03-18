package com.feipulai.exam.activity.situp.newSitUp;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.ShoulderManger;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestContract;
import com.feipulai.exam.activity.situp.base_test.SitPullUpTestPresenter;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.config.TestConfigs;

public class NewSitUpTestPresenter extends SitPullUpTestPresenter<SitUpSetting> implements SitPullLinker.SitPullPairListener {

    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    private SitPullLinker linker;
    private SitPullUpTestContract.View<SitUpSetting> view;
    private SitPushUpManager deviceManager;
    private ShoulderManger shoulderManger;
    protected NewSitUpTestPresenter(Context context, SitPullUpTestContract.View<SitUpSetting> view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
        shoulderManger = new ShoulderManger();
        this.view = view;
    }

    @Override
    protected int getCountStartTime() {
        return 5;
    }

    @Override
    protected int getCountFinishTime() {
        return 10;
    }

    @Override
    protected SitUpSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestTimeFromSetting() {
        return setting.getTestTime();
    }

    @Override
    protected void resetDevices() {
        deviceManager.endTest();
        //TODO
    }

    @Override
    protected int getGroupModeFromSetting() {
        return setting.getGroupMode();
    }

    @Override
    protected void testCountDown(long tick) {
        deviceManager.startTest((int) tick, setting.getTestTime());
    }

    @Override
    protected void onResultConfirmed() {
        view.enablePenalize(false);
    }

    @Override
    protected void onTestStarted() {
        for (StuDevicePair pair : pairs) {
            pair.setPenalty(0);
        }
        view.enablePenalize(false);
        //todo  更改工作状态

    }

    @Override
    protected void onMachineResultArrived() {
        // 判罚子类处理
        if (canPenalize()) {
            view.enablePenalize(true);
        }
    }

    protected boolean canPenalize() {
        return setting.isPenalize();
    }

    @Override
    public void onGettingState(int position) {
        BaseDeviceState deviceState = pairs.get(position).getBaseDevice();
        deviceManager.getState(deviceState.getDeviceId(),setting.getAngle());
        //todo
//        shoulderManger.getDeviceState();
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (mLinking && linker.onRadioArrived(msg)) {
            return;
        }
        int what = msg.what;
        if ((machineCode == ItemDefault.CODE_YWQZ) && what == SerialConfigs.SIT_UP_GET_STATE) {
            SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
            setState(stateResult);
        }

        //todo

    }

    private void setState(SitPushUpStateResult stateResult) {
        setState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft(), stateResult);
    }

    private void setState(int deviceId, int deviceState, int batteryLeft, IDeviceResult result) {
        if (deviceId >= deviceIdPIV.length
                || deviceIdPIV[deviceId] == INVALID_PIV) {
            return;
        }

        int piv = deviceIdPIV[deviceId];

        StuDevicePair pair = pairs.get(piv);
        BaseDeviceState originState = pair.getBaseDevice();

        if (originState.getState() == BaseDeviceState.STATE_STOP_USE) {
            return;
        }

        // WAIT_BGIN 和 TEST_COUNTING 状态没有成绩
        if (testState == WAIT_BGIN || testState == TEST_COUNTING) {
            pair.setDeviceResult(null);
        } else if (pair.getStudent() != null) {
            IDeviceResult deviceResult = pair.getDeviceResult();
            int res = deviceResult == null ? 0 : deviceResult.getResult();
            // 成绩只能增加
            if (result.getResult() >= res) {
                pair.setDeviceResult(result);
            }
        }

        int newState;

        if (((machineCode == ItemDefault.CODE_YWQZ) && deviceState == SitPushUpManager.STATE_COUNTING)) {
            newState = BaseDeviceState.STATE_COUNTING;
        } else if (((machineCode == ItemDefault.CODE_YWQZ||machineCode == ItemDefault.CODE_SGBQS) && deviceState == SitPushUpManager.STATE_ENDED)) {
            newState = BaseDeviceState.STATE_FINISHED;
        } else {
            newState = batteryLeft <= 10 ? BaseDeviceState.STATE_LOW_BATTERY : BaseDeviceState.STATE_FREE;
        }
        originState.setState(newState);
        view.updateSpecificItem(piv);
        currentConnect[deviceId]++;
        if (testState == WAIT_MACHINE_RESULTS || testState == WAIT_CONFIRM_RESULTS) {
            endGetResultPairs[deviceId]++;
        }
    }

    @Override
    public void changeBadDevice() {
        facade.pauseGettingState();
        if (linker == null) {
            linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
            linker.startPair(deviceIdPIV[focusPosition]);
        }
        view.showChangeBadDialog();
        mLinking = true;
    }

    @Override
    public void cancelChangeBad() {
        mLinking = false;
        linker.cancelPair();
        facade.resumeGettingState();
    }

    @Override
    public void onNoPairResponseArrived() {
        view.showToast("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void onNewDeviceConnect() {
        cancelChangeBad();
        view.changeBadSuccess();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int targetFrequency) {
        deviceManager.setFrequency(ItemDefault.CODE_YWQZ,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

}
