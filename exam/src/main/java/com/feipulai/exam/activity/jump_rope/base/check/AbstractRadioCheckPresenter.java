package com.feipulai.exam.activity.jump_rope.base.check;

import android.content.Context;
import android.util.Log;

import com.feipulai.common.jump_rope.facade.GetStateLedFacade;
import com.feipulai.common.jump_rope.task.OnGetStateWithLedListener;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

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
    protected RadioCheckContract.View<Setting> view;

    protected LEDManager mLEDManager;
    protected GetStateLedFacade facade;
    protected volatile List<StuDevicePair> pairs;
    protected SystemSetting systemSetting;
    protected Setting setting;
    protected int focusPosition;
    protected final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    protected int[] mCurrentConnect;
    protected volatile boolean mLinking;
    protected boolean isConstraintStart = false;// 是否强制启动

    public AbstractRadioCheckPresenter(Context context, RadioCheckContract.View<Setting> view) {
        this.view = view;
        this.context = context;
    }

    @Override
    public void start() {
        setting = getSetting();
        systemSetting = SettingHelper.getSystemSetting();
        mLEDManager = new LEDManager();
        if (    SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN
                ||TestCache.getInstance().getTestingPairs() == null || TestCache.getInstance().getTestingPairs().size() == 0) {
            pairs = CheckUtils.newPairs(getDeviceSumFromSetting());
        } else {
            pairs = TestCache.getInstance().getTestingPairs();
        }
        Log.e("PAIR",pairs.toString());

        mCurrentConnect = new int[pairs.size() + 1];
        TestCache.getInstance().init();
        view.initView(systemSetting, setting, pairs);
        // 分组模式检录
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {
            CheckUtils.groupCheck(pairs, TestConfigs.getMaxTestCount(context), getTestPattern());
            // Student student = pairs.get(0).getStudent();
            // view.showStuInfo(student, TestCache.getInstance().getResults().get(student));
        }
        RadioManager.getInstance().setOnRadioArrived(this);
        RadioChannelCommand command = new RadioChannelCommand(TARGET_FREQUENCY);
        LogUtils.normal(command.getCommand().length + "---" + StringUtility.bytesToHexString(command.getCommand()) + "---切频指令");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        facade = new GetStateLedFacade(this);
        facade.setmGetDeviceStatesLoopCount(getDeviceStatesLoopCount());
        facade.resume();
    }

    public int getDeviceStatesLoopCount() {
        return 5;
    }

    protected abstract Setting getSetting();

    //测试模式 0 连续 1 循环
    protected abstract int getTestPattern();

    protected abstract int getDeviceSumFromSetting();

    protected abstract void displayCheckedInLED(Student student, int deviceId, RoundResult lastResult);

    protected abstract String getStringToShow(BaseDeviceState deviceState, int position);

    protected abstract void endTest();

    @Override
    public void showStuInfo(int position) {
        StuDevicePair pair = pairs.get(position);
        Student student = pair.getStudent();
        try {
            if (TestCache.getInstance().getResults() != null) {
                List<RoundResult> resultList = TestCache.getInstance().getResults().get(student);
                view.showStuInfo(student, resultList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public void refreshEveryThing() {
        TestCache.getInstance().init();
        focusPosition = 0;
        pairs = CheckUtils.newPairs(getDeviceSumFromSetting());
        view.refreshPairs(pairs);
        view.showStuInfo(null, null);
        resetLED();
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
    public void deleteStudent() {
        if (systemSetting.getTestPattern() == SystemSetting.GROUP_PATTERN) {
            view.showToast("分组条件下不允许删除考生");
            return;
        }
        pairs.get(focusPosition).setStudent(null);
        view.updateSpecificItem(focusPosition);
    }

    @Override
    public void deleteAll() {
        if (systemSetting.getTestPattern() == SystemSetting.GROUP_PATTERN) {
            view.showToast("分组条件下不允许删除考生");
            return;
        }
        for (StuDevicePair pair : pairs) {
            pair.setStudent(null);
        }
        view.updateAllItems();
    }

    @Override
    public void startTest() {
        if (systemSetting.getTestPattern() == SystemSetting.GROUP_PATTERN) {
            // 分组模式,在组检录时就将所有的数据导入进 TestCache 了
            // 这里只需要检查所有的已经匹配的手柄是否均为空闲
            checkBeforeTest(false);
        } else {
            // 个人模式,通过获取当前界面的所有考生进行测试
            // 检查所有的已经匹配的手柄是否均为空闲的同时,需要将这些数据都导入到TestCache 中
            checkBeforeTest(true);
        }
        TestCache testCache = TestCache.getInstance();
        LogUtils.operation("用户点击开始测试,所有考生信息:" + testCache.getAllStudents()
                + "\n设备配对信息:" + testCache.getTestingPairs()
                + "\n设置项信息:" + setting.toString());
    }

    private void checkBeforeTest(boolean addToCache) {
        List<StuDevicePair> forTestPairs = new ArrayList<>(pairs.size());
        boolean contaisLowBattery = false;
        List<Student> students = null;
        if (addToCache) {
            students = new ArrayList<>(pairs.size());
        }
        Student student;
        BaseDeviceState baseDevice;
        for (StuDevicePair pair : pairs) {
            student = pair.getStudent();
            baseDevice = pair.getBaseDevice();
            if (student != null) {
                if (SettingHelper.getSystemSetting().isInputTest()) {
                    if (addToCache) {
                        students.add(student);
                    }
                    forTestPairs.add(pair);
                } else {
                    if (baseDevice.getState() == BaseDeviceState.STATE_FREE
                            || baseDevice.getState() == BaseDeviceState.STATE_LOW_BATTERY) {
                        if (addToCache) {
                            students.add(student);
                        }
                        forTestPairs.add(pair);
                        if (baseDevice.getState() == BaseDeviceState.STATE_LOW_BATTERY) {
                            contaisLowBattery = true;
                        }
                    } else {
                        if (!isConstraintStart) {
                            view.showToast("存在考生设备为非空闲状态");
                            isConstraintStart = true;
                        }

                    }
                }

            }
        }
        TestCache.getInstance().setTestingPairs(forTestPairs);
        if (addToCache) {
            TestCache.getInstance().setAllStudents(students);
        }
        students = TestCache.getInstance().getAllStudents();
        if (students == null || students.size() == 0) {
            view.showToast("不存在已配对好的考生和设备");
            return;
        }
        if (isConstraintStart) {
            view.showConstraintStartDialog(contaisLowBattery);
            isConstraintStart = false;
            return;
        }
        if (contaisLowBattery) {
            view.showLowBatteryStartDialog();
            return;
        }
        view.startTest();
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
            focusPosition = 0;
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
    public void resetLED() {
        facade.pause();
        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
            mLEDManager.resetLEDScreen(systemSetting.getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        }
        facade.resume();
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
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        facade.pause();
        // 绑定学生和手柄
        int deviceId = focusPosition + 1;
        // 如果这个人已经绑定了手柄,不允许绑定到新的手柄上
        for (int i = 0; i < pairs.size(); i++) {
            StuDevicePair pair = pairs.get(i);
            Student stu = pair.getStudent();
            for (RoundResult result : results){
                if (result.getIsDelete()){
                    pair.setCurrentRoundNo(result.getRoundNo());
                    pair.setIsAgain(true);
                }
            }
            if (stu != null && stu.getStudentCode().equals(student.getStudentCode())) {
                view.showToast("该考生已绑定设备");
                return;
            }
        }
        view.showStuInfo(student, results);
        //绑定手柄
        pairs.get(focusPosition).setStudent(student);

        int oldPosition = focusPosition;
        focusPosition = (oldPosition != pairs.size() - 1) ? focusPosition + 1 : focusPosition;
        if (focusPosition == oldPosition) {
            view.updateSpecificItem(oldPosition);
        } else {
            view.select(focusPosition, false);
        }

        RoundResult lastResult = null;


//        if (results == null || results.size() == 0) {
//            TestCache.getInstance().getResults().put(student,
//                    results != null ? results
//                            : new ArrayList<RoundResult>(TestConfigs.getMaxTestCount(context)));
//            TestCache.getInstance().getTestNoMap().put(student, 1);
//        } else {
//            lastResult = results.get(results.size() - 1);
//            TestCache.getInstance().getResults().put(student, results);
//            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
//            int testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
//            TestCache.getInstance().getTestNoMap().put(student, testNo);
//        }
        if (results == null || results.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            TestCache.getInstance().getResults().put(student,
                    results != null ? results
                            : new ArrayList<RoundResult>(TestConfigs.getMaxTestCount(context)));
            TestCache.getInstance().getTestNoMap().put(student, testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1);
        } else {
            lastResult = results.get(results.size() - 1);
            TestCache.getInstance().getResults().put(student, results);
            TestCache.getInstance().getTestNoMap().put(student, results.get(0).getTestNo());
        }


        TestCache.getInstance().getStudentItemMap().put(student, studentItem);
        displayCheckedInLED(student, deviceId, lastResult);

        facade.letDisplayWait3Sec();
        facade.resume();

        TestCache.getInstance().setTestingPairs(pairs);
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
        return systemSetting.getHostId();
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
                view.updateSpecificItem(i);
            }
        }
        mCurrentConnect = new int[getDeviceSumFromSetting() + 1];
        endTest();
    }

}
