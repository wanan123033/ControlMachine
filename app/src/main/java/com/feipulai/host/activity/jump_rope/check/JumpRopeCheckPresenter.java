package com.feipulai.host.activity.jump_rope.check;


import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.host.activity.jump_rope.base.InteractUtils;
import com.feipulai.host.activity.jump_rope.base.check.AbstractRadioCheckPresenter;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.feipulai.host.utils.ResultDisplayUtils;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class JumpRopeCheckPresenter
        extends AbstractRadioCheckPresenter<JumpRopeSetting>
        implements JumpRopeCheckContract.Presenter {

    private JumpRopeCheckContract.View view;
    private JumpRopeManager mJumpRopeManager;

    // 冲突的设备出厂id,数组下标-->设备id,对应数值!=0表示冲突的出厂id
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public JumpRopeCheckPresenter(Context context, JumpRopeCheckContract.View<JumpRopeSetting> view) {
        super(context, view);
        this.view = view;
        setting = SharedPrefsUtil.loadFormSource(context, JumpRopeSetting.class);
        mJumpRopeManager = new JumpRopeManager();
    }

    @Override
    public void changeDeviceGroup() {
        facade.pause();
        int currentGroup = setting.getDeviceGroup();
        currentGroup = (currentGroup + 1) % SerialConfigs.GROUP_NAME.length;
        setting.setDeviceGroup(currentGroup);
        mJumpRopeManager.endTest(hostId, currentGroup + 1);
        mLEDManager.resetLEDScreen(hostId, TestConfigs.machineNameMap.get(ItemDefault.CODE_TS));

        for (StuDevicePair pair : pairs) {
            JumpDeviceState deviceState = (JumpDeviceState) pair.getBaseDevice();
            deviceState.setFactoryId(JumpDeviceState.INVALID_FACTORY_ID);
            deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
        }
        view.showChangeDeviceGroup(currentGroup);
        facade.resume();
    }

    @Override
    public void changeBadDevice() {
        facade.pause();
        mLinking = true;
        executor.execute(new LinkTask());
        view.showChangeBadDialog();
    }

    @Override
    public void killAllDevices() {
        facade.pause();
        mJumpRopeManager.kill(SettingHelper.getSystemSetting().getHostId(), 0, setting.getDeviceGroup() + 1, 0);
        JumpDeviceState deviceState;
        for (StuDevicePair pair : pairs) {
            deviceState = (JumpDeviceState) pair.getBaseDevice();
            deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
            deviceState.setFactoryId(-1);
        }
        facade.resume();
        view.updateAllItems();
    }

    class LinkTask implements Runnable {
        @Override
        public void run() {
            while (mLinking) {
                mJumpRopeManager.link(SettingHelper.getSystemSetting().getUseChannel(), hostId, focusPosition + 1, 0x06, setting.getDeviceGroup() + 1);
            }
        }
    }

    @Override
    protected JumpRopeSetting getSetting() {
        return SharedPrefsUtil.loadFormSource(context, JumpRopeSetting.class);
    }

    @Override
    protected int getDeviceSumFromSetting() {
        return setting.getDeviceSum();
    }

    @Override
    protected void displayCheckedInLED(Student student, int deviceId, RoundResult lastResult) {
        mLEDManager.showString(hostId, student.getStudentName(), 5, 0, true, false);
        if (lastResult == null) {
            mLEDManager.showString(hostId, SerialConfigs.GROUP_NAME[setting.getDeviceGroup()] + deviceId + "号手柄", 4, 1, false, true);
        } else {
            String displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
            mLEDManager.showString(hostId, SerialConfigs.GROUP_NAME[setting.getDeviceGroup()] + deviceId + "号手柄", 4, 1, false, false);
            mLEDManager.showString(hostId, "已有成绩:" + displayResult, 2, 3, false, true);
        }
    }

    @Override
    protected String getStringToShow(BaseDeviceState deviceState, int position) {
        if (pairs.get(position).getStudent() == null) {
            return null;
        }
        StuDevicePair stuPair = pairs.get(position);
        BaseDeviceState jumpRopState = stuPair.getBaseDevice();
        String studentName = InteractUtils.getStrWithLength(stuPair.getStudent().getStudentName(), 6);
        return String.format(Locale.CHINA, "%-1s%-3d", SerialConfigs.GROUP_NAME[setting.getDeviceGroup()], jumpRopState.getDeviceId())
                + studentName;
    }

    @Override
    protected void endTest() {
        mJumpRopeManager.endTest(hostId, setting.getDeviceGroup() + 1);
    }

    @Override
    public void cancelChangeBad() {
        mLinking = false;
        facade.resume();
    }

    @Override
    public void settingChanged() {
        super.settingChanged();
    }

    @Override
    public void saveSetting() {
        SharedPrefsUtil.save(context, setting);
    }

    @Override
    public void dealConflict() {
        JumpDeviceState deviceState = (JumpDeviceState) pairs.get(focusPosition).getBaseDevice();
        deviceState.setFactoryId(JumpDeviceState.INVALID_FACTORY_ID);
        deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
        mJumpRopeManager.kill(hostId, deviceState.getDeviceId(), setting.getDeviceGroup() + 1, 0);
    }

    @Override
    public void onGettingState(int position) {
        mJumpRopeManager.getJumpRopeState(hostId, position + 1, setting.getDeviceGroup() + 1);
    }

    @Override
    protected void onDeviceDisconnect(int position) {
        JumpDeviceState deviceState = (JumpDeviceState) pairs.get(position).getBaseDevice();
        deviceState.setFactoryId(JumpDeviceState.INVALID_FACTORY_ID);
    }

    @Override
    public void onRadioArrived(Message msg) {
        switch (msg.what) {
            case SerialConfigs.JUMPROPE_RESPONSE:
                JumpRopeResult result = (JumpRopeResult) msg.obj;
                // Log.i("JumpRopeResult", result.toString());
                if (!mLinking) {
                    setDeviceState(result);
                } else {
                    // 如果是正在故障更换的配对过程中,而此处收到的是配对成功的信息,停止配对,提示配对成功,并取消配弹框
                    if (result.getHandGroup() == setting.getDeviceGroup() + 1
                            && result.getHandId() == focusPosition + 1
                            && result.getState() == JumpRopeManager.JUMP_ROPE_SPARE) {
                        StuDevicePair pair = pairs.get(focusPosition);
                        JumpDeviceState deviceState = (JumpDeviceState) pair.getBaseDevice();
                        deviceState.setFactoryId(result.getFactoryId());
                        mLinking = false;
                        facade.resume();
                        view.changeBadSuccess();
                    }
                }
                break;
        }
    }


    public void setDeviceState(JumpRopeResult result) {
        // 1.记录连接成功的手柄ID,并更新配对情况状态
        // 如果收到的是正在测试范围内的手柄,显示即可
        int deviceId = result.getHandId();

        // 不同组 非当前范围内手柄
        if (setting.getDeviceGroup() != result.getHandGroup() - 1
                || deviceId > setting.getDeviceSum()
                || mCurrentConnect[deviceId] != 0
                || SettingHelper.getSystemSetting().getHostId() != result.getHostId()//不同主机号
                || result.getState() != JumpRopeManager.JUMP_ROPE_SPARE) {
            return;
        }

        // 这里的handId在RecyclerView中的关系是对应的--->index = deviceId - 1
        JumpDeviceState originState = (JumpDeviceState) pairs.get(deviceId - 1).getBaseDevice();

        //Log.i("james","deviceId:" + deviceId + "\t\tfacttoryId:" + result.getFactoryId());
        //暂停使用和冲突状态需要保留下来
        if (originState.getState() == BaseDeviceState.STATE_STOP_USE
                || originState.getState() == BaseDeviceState.STATE_CONFLICT) {
            return;
        }

        int factoryId = result.getFactoryId();
        // Log.i("james", "deviceId:" + deviceId + "\tfactoryId:" + factoryId + "\tisBinded:" + originState.isBinded());

        int newState = BaseDeviceState.STATE_FREE;
        boolean lowBattery = result.getBatteryLeftPercent() <= 10;

        // 冲突
        if (factoryId != originState.getFactoryId() && originState.getFactoryId() != JumpDeviceState.INVALID_FACTORY_ID) {
            newState = BaseDeviceState.STATE_CONFLICT;
        } else if (lowBattery) {
            newState = BaseDeviceState.STATE_LOW_BATTERY;
        }
        if (newState != originState.getState()) {
            originState.setFactoryId(factoryId);
            originState.setState(newState);
            view.updateSpecificItem(deviceId - 1);
        }
        mCurrentConnect[deviceId]++;
    }

}
