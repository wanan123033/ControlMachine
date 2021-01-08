package com.feipulai.exam.activity.situp.base_check;

import android.content.Context;
import android.os.Message;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.activity.jump_rope.base.check.AbstractRadioCheckPresenter;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.Locale;

public abstract class SitPullUpCheckPresenter<Setting>
        extends AbstractRadioCheckPresenter<Setting>
        implements SitPullLinker.SitPullPairListener {

    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private SitPullLinker linker;

    public SitPullUpCheckPresenter(Context context, RadioCheckContract.View<Setting> view) {
        super(context, view);
    }

    @Override
    protected void displayCheckedInLED(Student student, int deviceId, RoundResult lastResult) {
        int hostId = systemSetting.getHostId();
        mLEDManager.showString(hostId, student.getLEDStuName(), 5, 0, true, false);
        if (lastResult == null) {
            mLEDManager.showString(hostId, deviceId + "号设备", 4, 1, false, true);
        } else {
            mLEDManager.showString(hostId, deviceId + "号设备", 4, 1, false, false);
            String displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
            mLEDManager.showString(hostId, "已有成绩:" + displayResult, 2, 3, false, true);
        }
    }

    public synchronized void onNewDeviceConnect() {
        cancelChangeBad();
        view.changeBadSuccess();
    }

    public synchronized void onNoPairResponseArrived() {
        view.showToast("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void changeBadDevice() {
        if (linker == null) {
            linker = new SitPullLinker(machineCode, TARGET_FREQUENCY, this);
        }
        facade.pause();
        linker.startPair(focusPosition + 1);
        view.showChangeBadDialog();
        mLinking = true;
    }

    @Override
    public void cancelChangeBad() {
        mLinking = false;
        if (linker != null) {
            linker.cancelPair();
        }
        facade.resume();
    }

    @Override
    public void onRadioArrived(Message msg) {
        if (mLinking && linker.onRadioArrived(msg)) {
            return;
        }
        int what = msg.what;
        if ((machineCode == ItemDefault.CODE_YWQZ||machineCode == ItemDefault.CODE_SGBQS) && what == SerialConfigs.SIT_UP_GET_STATE) {
            SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
            setState(stateResult);
        } else if (machineCode == ItemDefault.CODE_YTXS && what == SerialConfigs.PULL_UP_GET_STATE) {
            PullUpStateResult stateResult = (PullUpStateResult) msg.obj;
            setState(stateResult);
        } else if (machineCode == ItemDefault.CODE_FWC && what == SerialConfigs.PUSH_UP_GET_STATE) {
            SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
            setState(stateResult);
        }
    }

    public abstract void setFrequency(int deviceId, int originFrequency, int deviceFrequency);

    private void setState(PullUpStateResult stateResult) {
        setState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft());
    }

    private void setState(SitPushUpStateResult stateResult) {
        setState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft());
    }

    private void setState(int deviceId, int deviceState, int batteryLeft) {
        if (deviceId > getDeviceSumFromSetting()

                ) {
            return;
        }
        // 必须为空闲状态
        if (((machineCode == ItemDefault.CODE_YWQZ||machineCode == ItemDefault.CODE_SGBQS) && deviceState != SitPushUpManager.STATE_FREE)
                || (machineCode == ItemDefault.CODE_YTXS && deviceState != PullUpManager.STATE_FREE)) {
            return;
        }

        BaseDeviceState originState = pairs.get(deviceId - 1).getBaseDevice();
        originState.setDisconnectCount(0);
        if (originState.getState() == BaseDeviceState.STATE_STOP_USE) {
            return;
        }
        int newState = BaseDeviceState.STATE_FREE;
        newState = batteryLeft <= 10 ? BaseDeviceState.STATE_LOW_BATTERY : newState;
        if (newState != originState.getState()) {
            originState.setState(newState);
            view.updateSpecificItem(deviceId - 1);
        }
        mCurrentConnect[deviceId]++;
    }

    @Override
    protected String getStringToShow(BaseDeviceState deviceState, int position) {
        if (pairs.get(position).getStudent() == null) {
            return null;
        }
        StuDevicePair stuPair = pairs.get(position);
        String studentName = InteractUtils.getStrWithLength(stuPair.getStudent().getStudentName(), 6);
        return String.format(Locale.CHINA, "%-3d", deviceState.getDeviceId()) + studentName;
    }

}
