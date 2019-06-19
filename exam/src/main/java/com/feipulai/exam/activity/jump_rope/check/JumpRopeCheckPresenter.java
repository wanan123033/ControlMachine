package com.feipulai.exam.activity.jump_rope.check;

import android.content.Context;
import android.os.Message;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.JumpRopeManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.JumpRopeResult;
import com.feipulai.exam.activity.jump_rope.base.check.AbstractRadioCheckPresenter;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.JumpDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.setting.JumpRopeSetting;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.utils.ResultDisplayUtils;

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
    private int expectCurrentGroup;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public JumpRopeCheckPresenter(Context context, JumpRopeCheckContract.View<JumpRopeSetting> view) {
        super(context, view);
        this.view = view;
        setting = SharedPrefsUtil.loadFormSource(context, JumpRopeSetting.class);
        mJumpRopeManager = new JumpRopeManager();
        expectCurrentGroup = setting.getDeviceGroup();
    }

    @Override
    public void changeDeviceGroup() {
        facade.pause();
        int currentGroup = setting.getDeviceGroup();
        currentGroup = (currentGroup + 1) % SerialConfigs.GROUP_NAME.length;
        setting.setDeviceGroup(currentGroup);
        expectCurrentGroup = currentGroup;
        mJumpRopeManager.endTest(systemSetting.getHostId(), currentGroup + 1);
        mLEDManager.resetLEDScreen(systemSetting.getHostId(), TestConfigs.machineNameMap.get(ItemDefault.CODE_TS));

        for (StuDevicePair pair : pairs) {
            JumpDeviceState deviceState = (JumpDeviceState) pair.getBaseDevice();
            deviceState.setFactoryId(JumpDeviceState.INVALID_FACTORY_ID);
            deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
        }
        view.showChangeDeviceGroup(currentGroup);
        facade.resume();
    }

    @Override
    public void settingChanged() {
        // 只有设备分组变了
        if (pairs != null && pairs.size() == getDeviceSumFromSetting() && expectCurrentGroup != setting.getDeviceGroup()) {
            // 清掉所有的设备出厂编号
            for (StuDevicePair pair : pairs) {
                JumpDeviceState deviceState = (JumpDeviceState) pair.getBaseDevice();
                deviceState.setFactoryId(JumpDeviceState.INVALID_FACTORY_ID);
            }
        }
        super.settingChanged();
    }

    @Override
    public void changeBadDevice() {
        view.showChangBadWarning();
    }

    class LinkTask implements Runnable {
        @Override
        public void run() {
            while (mLinking) {
                mJumpRopeManager.link(systemSetting.getHostId(), focusPosition + 1, 0x06, setting.getDeviceGroup() + 1);
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
        int hostId = systemSetting.getHostId();
        mLEDManager.showString(systemSetting.getHostId(), student.getStudentName(), 5, 0, true, false);
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
        return String.format(Locale.CHINA,"%-1s%-3d", SerialConfigs.GROUP_NAME[setting.getDeviceGroup()],jumpRopState.getDeviceId())
                + studentName;
    }

    @Override
    protected void endTest() {
        mJumpRopeManager.endTest(systemSetting.getHostId(), setting.getDeviceGroup() + 1);
    }

    @Override
    public void cancelChangeBad() {
        mLinking = false;
        facade.resume();
    }

    @Override
    public void saveSetting() {
        SharedPrefsUtil.save(context, setting);
    }

    @Override
    public void dealConflict() {
        JumpDeviceState deviceState = (JumpDeviceState) pairs.get(focusPosition).getBaseDevice();
        mJumpRopeManager.kill(systemSetting.getHostId(), deviceState.getDeviceId(), setting.getDeviceGroup() + 1,0);
        deviceState.setFactoryId(JumpDeviceState.INVALID_FACTORY_ID);
        deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
        view.updateSpecificItem(focusPosition);
    }

    @Override
    public void killAllDevices() {
        facade.pause();
        mJumpRopeManager.kill(systemSetting.getHostId(), 0, setting.getDeviceGroup() + 1, 0);
        JumpDeviceState deviceState;
        for (StuDevicePair pair : pairs) {
            deviceState = (JumpDeviceState) pair.getBaseDevice();
            deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
        }
        facade.resume();
        view.updateAllItems();
    }

    @Override
    public void changeBadDevice(boolean killCurrent) {
        JumpDeviceState deviceState = (JumpDeviceState) pairs.get(focusPosition).getBaseDevice();
        if(killCurrent){
            mJumpRopeManager.kill(systemSetting.getHostId(), deviceState.getDeviceId(), setting.getDeviceGroup() + 1,0);
        }
        facade.pause();
        mLinking = true;
        executor.execute(new LinkTask());
        view.showChangeBadDialog();
    }

    @Override
    public void onGettingState(int position) {
        mJumpRopeManager.getJumpRopeState(systemSetting.getHostId(), position + 1, setting.getDeviceGroup() + 1);
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
        int deviceId = result.getHandId();

        if (setting.getDeviceGroup() != result.getHandGroup() - 1// 不同组
                || deviceId > setting.getDeviceSum()// 非当前范围内手柄
                || mCurrentConnect[deviceId] != 0// 已经设置过状态
                || result.getState() != JumpRopeManager.JUMP_ROPE_SPARE/*非空闲手柄状态*/) {
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
