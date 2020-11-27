package com.feipulai.host.activity.situp.check;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.base.check.AbstractRadioCheckPresenter;
import com.feipulai.host.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.situp.setting.SitUpSetting;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.util.Locale;

public class SitUpCheckPresenter
        extends AbstractRadioCheckPresenter<SitUpSetting>
        implements SitPullLinker.SitPullPairListener {

    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private SitPullLinker linker;
    private final SitPushUpManager deviceManager;
    private int countForSetAngle = 20;

    public SitUpCheckPresenter(Context context, RadioCheckContract.View<SitUpSetting> view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
    }

    @Override
    protected void displayCheckedInLED(Student student, int deviceId, RoundResult lastResult) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
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
        view.showToast(context.getString(R.string.no_reply_received_hint));
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
            Logger.i("SitPushUpStateResult===" + stateResult.toString());
            setState(stateResult);
        }
    }

//    public abstract void setFrequency(int deviceId, int originFrequency, int deviceFrequency);

    private void setState(PullUpStateResult stateResult) {
        setState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft());
    }

    private void setState(SitPushUpStateResult stateResult) {
        setState(stateResult.getDeviceId(), stateResult.getState(), stateResult.getBatteryLeft());
    }

    private void setState(int deviceId, int deviceState, int batteryLeft) {
        if (deviceId > getDeviceSumFromSetting()
                || mCurrentConnect[deviceId] != 0 // 状态没变,一直是连接的
                ) {
            return;
        }
        // 必须为空闲状态
        if (((machineCode == ItemDefault.CODE_YWQZ||machineCode == ItemDefault.CODE_SGBQS) && deviceState != SitPushUpManager.STATE_FREE)
                || (machineCode == ItemDefault.CODE_YTXS && deviceState != PullUpManager.STATE_FREE)) {
            return;
        }

        BaseDeviceState originState = pairs.get(deviceId - 1).getBaseDevice();
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

    @Override
    protected SitUpSetting getSetting() {
        return setting;
    }

    @Override
    protected int getDeviceSumFromSetting() {
        return setting.getDeviceSum();
    }

    @Override
    protected void endTest() {
        deviceManager.endTest();
    }

    @Override
    protected void onDeviceDisconnect(int position) {
    }

    @Override
    public void onGettingState(int position) {
        deviceManager.getState(position + 1, setting.getAngle());
        if (countForSetAngle++ % 20 == 0) {
            deviceManager.setBaseline(SitPushUpManager.PROJECT_CODE_SIT_UP, setting.getAngle());
        }
    }


    public void setFrequency(int deviceId, int originFrequency, int deviceFrequency) {
        deviceManager.setFrequency( SettingHelper.getSystemSetting().getUseChannel(),
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    public void dealConflict() {
        pairs.get(focusPosition).getBaseDevice().setState(BaseDeviceState.STATE_STOP_USE);
    }
}
