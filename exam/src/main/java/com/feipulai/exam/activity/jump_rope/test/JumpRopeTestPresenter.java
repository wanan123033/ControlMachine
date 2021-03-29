package com.feipulai.exam.activity.jump_rope.test;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.exam.activity.jump_rope.base.test.AbstractRadioTestPresenter;
import com.feipulai.exam.activity.jump_rope.base.test.RadioTestContract;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.orhanobut.logger.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by James on 2019/1/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class JumpRopeTestPresenter
        extends AbstractRadioTestPresenter<JumpRopeSetting> {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private JumpRopeManager deviceManager;

    JumpRopeTestPresenter(Context context, RadioTestContract.View<JumpRopeSetting> view) {
        super(context, view);
        deviceManager = new JumpRopeManager();
        setting = SharedPrefsUtil.loadFormSource(context, JumpRopeSetting.class);
    }

    @Override
    protected int getCountStartTime() {
        return JumpRopeManager.DEFAULT_COUNT_DOWN_TIME;
    }

    @Override
    protected int getCountFinishTime() {
        return 10;
    }

    @Override
    protected JumpRopeSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestTimeFromSetting() {
        return setting.getTestTime();
    }

    @Override
    protected void resetDevices() {
        deviceManager.endTest(systemSetting.getHostId(), setting.getDeviceGroup() + 1);
    }

    @Override
    protected int getGroupModeFromSetting() {
        return setting.getGroupMode();
    }

    @Override
    protected void testCountDown(long tick) {
        if (tick == 1) {
            for (int i = 0; i < 10; i++) {
                deviceManager.countDown(systemSetting.getHostId(), setting.getDeviceGroup() + 1, 1, setting.getTestTime());
            }
        } else {
            deviceManager.countDown(systemSetting.getHostId(), setting.getDeviceGroup() + 1, (int) tick, setting.getTestTime());
        }


    }


    @Override
    public void changeBadDevice() {
        facade.pauseGettingState();
        mLinking = true;
        executor.execute(new LinkTask());
        view.showChangeBadDialog();
    }

    @Override
    public void cancelChangeBad() {
        mLinking = false;
        facade.resumeGettingState();
    }

    class LinkTask implements Runnable {
        @Override
        public void run() {
            while (mLinking) {
                deviceManager.link(systemSetting.getUseChannel(), systemSetting.getHostId(), focusPosition + 1, 0x06, setting.getDeviceGroup() + 1);
            }
        }
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
    public void stopNow() {
        facade.stopTotally();
    }

    @Override
    public void onGettingState(int position) {
        BaseDeviceState deviceState = pairs.get(position).getBaseDevice();
        deviceManager.getJumpRopeState(systemSetting.getHostId(), deviceState.getDeviceId(), setting.getDeviceGroup() + 1);
    }

    public void setDeviceState(JumpRopeResult result) {
        // Log.i("james", "JumpRopeResult:" + result.toString());
        int deviceId = result.getHandId();
        // 不同组 非当前范围内手柄
        if (result.getHandGroup() != setting.getDeviceGroup() + 1
                || deviceId >= deviceIdPIV.length
                || SettingHelper.getSystemSetting().getHostId() != result.getHostId()//不同主机号
                || deviceIdPIV[deviceId] == INVALID_PIV) {
            return;
        }
        int piv = deviceIdPIV[deviceId];
        StuDevicePair pair = pairs.get(piv);
        JumpDeviceState originState = (JumpDeviceState) pair.getBaseDevice();

        if (result.getFactoryId() != originState.getFactoryId()
                || originState.getState() == BaseDeviceState.STATE_STOP_USE) {
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
        switch (result.getState()) {

            case JumpRopeManager.JUMP_ROPE_COUNTING:
                newState = BaseDeviceState.STATE_COUNTING;
                break;

            case JumpRopeManager.JUMP_ROPE_FINISHED:
                newState = BaseDeviceState.STATE_FINISHED;
                break;

            default:
                newState = result.getBatteryLeftPercent() <= 10 ? BaseDeviceState.STATE_LOW_BATTERY : BaseDeviceState.STATE_FREE;
                break;
        }
        originState.setState(newState);
        view.updateSpecificItem(piv);
        currentConnect[deviceId]++;
        if (testState == WAIT_MACHINE_RESULTS || testState == WAIT_CONFIRM_RESULTS) {
            endGetResultPairs[deviceId]++;
        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (msg.what != SerialConfigs.JUMPROPE_RESPONSE) {
            return;
        }
        JumpRopeResult result = (JumpRopeResult) msg.obj;
        Logger.i("JumpRopeResult===>"+ result.toString());
        if (!mLinking) {
            setDeviceState(result);
        } else {
            if (result.getHandGroup() == setting.getDeviceGroup() + 1
                    && result.getHandId() == focusPosition + 1
                    && result.getState() == JumpRopeManager.JUMP_ROPE_SPARE) {
                StuDevicePair pair = pairs.get(focusPosition);
                JumpDeviceState deviceState = (JumpDeviceState) pair.getBaseDevice();
                deviceState.setFactoryId(result.getFactoryId());
                mLinking = false;
                facade.resumeGettingState();
                view.changeBadSuccess();
            }
        }
    }

    @Override
    protected void onResultConfirmed() {
    }

    @Override
    protected void onTestStarted() {
    }

    @Override
    protected void onMachineResultArrived() {
    }

}
