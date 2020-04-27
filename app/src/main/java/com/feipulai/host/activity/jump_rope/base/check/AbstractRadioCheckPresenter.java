package com.feipulai.host.activity.jump_rope.base.check;

import android.content.Context;

import com.feipulai.common.jump_rope.facade.GetStateLedFacade;
import com.feipulai.common.jump_rope.task.OnGetStateWithLedListener;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.host.R;
import com.feipulai.host.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.host.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.host.activity.jump_rope.bean.TestCache;
import com.feipulai.host.activity.jump_rope.check.CheckUtils;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.Student;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public abstract class AbstractRadioCheckPresenter<Setting>
        implements RadioCheckContract.Presenter,
        OnGetStateWithLedListener,
        RadioManager.OnRadioArrivedListener {

    protected Context context;
    protected RadioCheckContract.View view;

    protected LEDManager mLEDManager;
    protected GetStateLedFacade facade;
    protected volatile List<StuDevicePair> pairs;
    protected Setting setting;
    protected int focusPosition;
    protected final int TARGET_FREQUENCY;
    protected int[] mCurrentConnect;
    protected volatile boolean mLinking;
    protected int hostId;
    private String machineName;

    public AbstractRadioCheckPresenter(Context context, RadioCheckContract.View<Setting> view) {
        this.view = view;
        this.context = context;
        hostId = SettingHelper.getSystemSetting().getHostId();
        TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
        machineName = TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode());
    }

    @Override
    public void start() {
        TestCache.getInstance().clear();
        setting = getSetting();
        mLEDManager = new LEDManager();
        pairs = CheckUtils.newPairs(getDeviceSumFromSetting());
        mCurrentConnect = new int[pairs.size() + 1];
        view.initView(setting, pairs);
        // 分组模式检录
        RadioManager.getInstance().setOnRadioArrived(this);

        RadioManager.getInstance().sendCommand(new ConvertCommand(new RadioChannelCommand(TARGET_FREQUENCY)));
        facade = new GetStateLedFacade(this);
        mLEDManager.resetLEDScreen(hostId, machineName);// 重新设置LED屏
        facade.resume();
    }

    @Override
    public void refreshEveryThing() {
        TestCache.getInstance().clear();
        focusPosition = 0;
        pairs = CheckUtils.newPairs(getDeviceSumFromSetting());
        view.refreshPairs(pairs);
        view.showStuInfo(null, null);
        mLEDManager.resetLEDScreen(hostId, machineName);// 重新设置LED屏
    }

    protected abstract Setting getSetting();

    protected abstract int getDeviceSumFromSetting();

    protected abstract void displayCheckedInLED(Student student, int deviceId, RoundResult lastResult);

    protected abstract String getStringToShow(BaseDeviceState deviceState, int position);

    protected abstract void endTest();

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
    public void deleteStudent() {
        pairs.get(focusPosition).setStudent(null);
        view.updateSpecificItem(focusPosition);
    }

    @Override
    public void deleteAll() {
        for (StuDevicePair pair : pairs) {
            pair.setStudent(null);
        }
        view.updateAllItems();
    }

    @Override
    public void startTest() {
        // 个人模式,通过获取当前界面的所有考生进行测试
        // 检查所有的已经匹配的手柄是否均为空闲的同时,需要将这些数据都导入到TestCache 中
        List<StuDevicePair> result = new ArrayList<>(pairs.size());
        boolean contaisLowBattery = false;
        Student student;
        BaseDeviceState baseDevice;
        for (StuDevicePair pair : pairs) {
            student = pair.getStudent();
            baseDevice = pair.getBaseDevice();
            if (student != null) {
                int state = baseDevice.getState();
                if (state == BaseDeviceState.STATE_FREE) {
                    result.add(pair);
                } else if (state == BaseDeviceState.STATE_LOW_BATTERY) {
                    result.add(pair);
                    contaisLowBattery = true;
                } else {
                    view.showToast(context.getString(R.string.jump_rope_test_hint_1));
                    return;
                }
            }
        }
        if (result.size() == 0) {
            view.showToast(context.getString(R.string.jump_rope_test_hint_2));
            return;
        }
        TestCache.getInstance().setTestingPairs(result);
        if (contaisLowBattery) {
            view.showLowBatteryStartDialog();
            return;
        }
        view.startTest();
        Logger.i("用户点击开始测试,考生设备配对信息:" + result.toString()
                + "\n设置项信息:" + setting.toString());
    }

    @Override
    public int stateOfPosition(int position) {
        return pairs.get(position).getBaseDevice().getState();
    }

    @Override
    public void finishGetStateAndDisplay() {
        facade.finish();
    }

    @Override
    public void settingChanged() {
        if (pairs != null && pairs.size() != getDeviceSumFromSetting()) {
            List<StuDevicePair> newPairs = CheckUtils.newPairs(getDeviceSumFromSetting());
            for (int i = 0; i < pairs.size(); i++) {
                if (i == newPairs.size()) {
                    break;
                } else {
                    newPairs.set(i, pairs.get(i));
                }
            }
            pairs = newPairs;
            mCurrentConnect = new int[pairs.size() + 1];
            view.refreshPairs(pairs);
        }
    }

    @Override
    public void resumeGetStateAndDisplay() {
        facade.resume();
        RadioManager.getInstance().setOnRadioArrived(this);
    }

    @Override
    public void pauseGetStateAndDisplay() {
        facade.pause();
        RadioManager.getInstance().setOnRadioArrived(null);
    }

    @Override
    public void saveSetting() {
        SharedPrefsUtil.save(context, setting);
    }

    @Override
    public void onCheckIn(Student student) {
        facade.pause();
        // 绑定学生和手柄
        int deviceId = focusPosition + 1;
        // 如果这个人已经绑定了手柄,不允许绑定到新的手柄上
        for (int i = 0; i < pairs.size(); i++) {
            StuDevicePair pair = pairs.get(i);
            Student stu = pair.getStudent();
            if (stu != null && stu.getStudentCode().equals(student.getStudentCode())) {
                view.showToast(context.getString(R.string.jump_rope_bind_hint));
                return;
            }
        }
        RoundResult lastResult = DBManager.getInstance().queryLastScoreByStuCode(student.getStudentCode());
        view.showStuInfo(student, lastResult);
        //绑定手柄
        pairs.get(focusPosition).setStudent(student);

        int oldPosition = focusPosition;
        focusPosition = (oldPosition != pairs.size() - 1) ? focusPosition + 1 : focusPosition;
        if (focusPosition == oldPosition) {
            view.updateSpecificItem(oldPosition);
        } else {
            view.select(focusPosition);
        }
        displayCheckedInLED(student, deviceId, lastResult);

        facade.letDisplayWait3Sec();
        facade.resume();
    }

    @Override
    public void setFocusPosition(int position) {
        focusPosition = position;
    }

    @Override
    public int getDeviceCount() {
        return pairs.size();
    }

    @Override
    public String getStringToShow(int position) {
        StuDevicePair stuPair = pairs.get(position);
        BaseDeviceState deviceState = stuPair.getBaseDevice();
        return getStringToShow(deviceState, position);
    }

    @Override
    public int getHostId() {
        return hostId;
    }


    @Override
    public void onStateRefreshed() {
        int oldState;
        for (int i = 0; i < pairs.size(); i++) {
            StuDevicePair pair = pairs.get(i);
            BaseDeviceState deviceState = pair.getBaseDevice();
            oldState = deviceState.getState();
            if (mCurrentConnect[deviceState.getDeviceId()] == 0
                    && oldState != BaseDeviceState.STATE_DISCONNECT
                    && oldState != BaseDeviceState.STATE_STOP_USE
                    && oldState != BaseDeviceState.STATE_CONFLICT) {
                deviceState.setState(BaseDeviceState.STATE_DISCONNECT);
                onDeviceDisconnect(i);
                view.updateSpecificItem(i);
            }
        }
        mCurrentConnect = new int[getDeviceSumFromSetting() + 1];
        endTest();
    }

    protected abstract void onDeviceDisconnect(int position);

}
