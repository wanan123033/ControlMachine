package com.feipulai.exam.activity.situp.newSitUp;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.exam.activity.jump_rope.base.check.AbstractRadioCheckPresenter;
import com.feipulai.exam.activity.jump_rope.base.check.RadioCheckContract;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

import java.util.Locale;

public class NewSitUpCheckPresenter extends AbstractRadioCheckPresenter<SitUpSetting> {
    private final SitPushUpManager deviceManager;
    private int countForSetAngle = 20;
    public NewSitUpCheckPresenter(Context context, RadioCheckContract.View<SitUpSetting> view) {
        super(context, view);
        setting = SharedPrefsUtil.loadFormSource(context, SitUpSetting.class);
        deviceManager = new SitPushUpManager(SitPushUpManager.PROJECT_CODE_SIT_UP);
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
    }

    @Override
    public void onRadioArrived(Message msg) {

    }

    @Override
    public void changeBadDevice() {

    }

    @Override
    public void cancelChangeBad() {

    }
}
