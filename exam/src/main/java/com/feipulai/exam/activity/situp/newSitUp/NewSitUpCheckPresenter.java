package com.feipulai.exam.activity.situp.newSitUp;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.manager.ShoulderManger;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.ShoulderResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.exam.activity.jump_rope.base.check.AbstractRadioCheckPresenter;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.Calendar;
import java.util.Locale;

public class NewSitUpCheckPresenter extends AbstractRadioCheckPresenter<SitUpSetting> {
    private final SitPushUpManager deviceManager;
    private int countForSetAngle = 20;
    private int machineCode = TestConfigs.sCurrentItem.getMachineCode();
    private ShoulderManger shoulderManger;
    private boolean syncTime;

    public NewSitUpCheckPresenter(Context context, RadioCheckContract.View<SitUpSetting> view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
        shoulderManger = new ShoulderManger();
    }

    @Override
    protected SitUpSetting getSetting() {
        return setting;
    }

    @Override
    protected int getTestPattern() {
        return setting.getGroupMode();
    }

    @Override
    protected int getDeviceSumFromSetting() {
        return setting.getDeviceSum();
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
    protected void endTest() {
        deviceManager.endTest();
    }

    @Override
    public void onGettingState(int position) {
        deviceManager.getState(position + 1, setting.getAngle());
        if (countForSetAngle++ % 20 == 0) {
            deviceManager.setBaseline(SitPushUpManager.PROJECT_CODE_SIT_UP, setting.getAngle());
        }
        shoulderManger.getState(position + 1, systemSetting.getHostId());
        if (!syncTime) {
            shoulderManger.syncTime(systemSetting.getHostId(), getTime(),setting.isShowLed()? 1: 0);
            shoulderManger.getTime(position+1,systemSetting.getHostId());

        }
    }

    @Override
    public void onRadioArrived(Message msg) {
        int what = msg.what;
        if ((machineCode == ItemDefault.CODE_YWQZ || machineCode == ItemDefault.CODE_SGBQS) && what == SerialConfigs.SIT_UP_GET_STATE) {
            SitPushUpStateResult stateResult = (SitPushUpStateResult) msg.obj;
            setState(stateResult);
        }

        if (machineCode == ItemDefault.CODE_YWQZ && what == SerialConfigs.NEW_SIT_UP_SHOULDER_SYNC_TIME) {
            syncTime = true;
        }
        if (machineCode == ItemDefault.CODE_YWQZ && what == SerialConfigs.NEW_SIT_UP_SHOULDER_CONNECT) {
            ShoulderResult result = (ShoulderResult) msg.obj;
            if (result.getBattery()< 10) {
                BaseDeviceState originState = pairs.get(result.getDeviceId() - 1).getBaseDevice();
                originState.setState(BaseDeviceState.STATE_LOW_BATTERY);
                view.updateSpecificItem(result.getDeviceId()  - 1);
            }
        }
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
        if (((machineCode == ItemDefault.CODE_YWQZ || machineCode == ItemDefault.CODE_SGBQS) && deviceState != SitPushUpManager.STATE_FREE)
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
    public void changeBadDevice() {
        facade.pause();
    }

    @Override
    public void cancelChangeBad() {
        facade.resume();
    }

    /**
     * 返回当前时间精确到毫秒 不要年月日
     *
     * @return
     */
    public int getTime() {
        Calendar Cld = Calendar.getInstance();
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);
        return HH * 60 * 60 * 1000 + mm * 60 * 1000 + SS * 1000 + MI;
    }
}
