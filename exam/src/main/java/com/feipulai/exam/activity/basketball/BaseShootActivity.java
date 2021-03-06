package com.feipulai.exam.activity.basketball;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.util.RunTimerImpl;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseShootActivity extends BaseTitleActivity
        implements IndividualCheckFragment.OnIndividualCheckInListener{
    private static final String TAG = "BaseShootActivity";
    protected IndividualCheckFragment individualCheckFragment;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    protected volatile int state = WAIT_FREE;
    private List<StuDevicePair> pairs = new ArrayList<>();
    private SerialDeviceManager deviceManager;
    private ShootSetting setting;
    public long baseTimer ;
    private long startTime;
    @Override
    protected void initData() {
        pairs.add(new StuDevicePair());
        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setOnIndividualCheckInListener(this);

        deviceManager = SerialDeviceManager.getInstance();
        deviceManager.setRS232ResiltListener(runTimer);

        //??????????????????
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        if (setting == null)
            setting = new ShootSetting();

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (individualCheckFragment.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        if (student != null)
            LogUtils.operation("?????????????????????:" + student.toString());
        if (studentItem != null)
            LogUtils.operation("?????????????????????StudentItem:" + studentItem.toString());
        if (results != null)
            LogUtils.operation("???????????????????????????:" + results.size() + "----" + results.toString());
        if (state == WAIT_FREE || state == WAIT_CHECK_IN) {

            pairs.get(0).setStudent(student);
            for (RoundResult result : results) {
                if (isFullSkip(result.getResult(), result.getResultState())) {
                    toastSpeak("??????");
                    pairs.get(0).setStudent(null);
                    LogUtils.operation("????????????????????????????????????:" + result.getStudentCode());
                    return;
                }
            }
            updateStudent(student,results);
        }


    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "??????"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addRightText("????????????", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProjectSetting();
            }
        });
    }

    /**
     * ??????????????????
     */
    private void startProjectSetting() {
        if (!isConfigurableNow()) {
            LogUtils.operation("?????????????????????????????????");
            IntentUtil.gotoActivityForResult(this, ShootSettingActivity.class, 1);
        } else {
            toastSpeak("?????????,?????????????????????");
        }
    }

    /**
     * ??????????????????
     */
    private boolean isConfigurableNow() {
        boolean flag = !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
        LogUtils.operation("??????isConfigurableNow(??????????????????) = " + flag);
        return flag;
    }

    protected abstract void updateStudent(Student student,List<RoundResult> results);

    private boolean isFullSkip(int result, int resultState) {
        Student student = pairs.get(0).getStudent();
        if (setting.isFullSkip() && resultState == RoundResult.RESULT_STATE_NORMAL) {
//            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            if (student.getSex() == Student.MALE) {

                return setting.getTestType() == 2? result <= setting.getMaleFullDribble()*1000:
                        result<=setting.getMaleFullShoot();
            } else {
                return setting.getTestType() == 2? result <= setting.getFemaleFullDribble()*1000:
                        result<=setting.getFemaleFullShoot();
            }
        }
        return false;
    }

    public void disposeResult(int order, Student student, int testRound, int testNo, boolean b) {

        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        RoundResult roundResult = new RoundResult();
        if (b){
            roundResult.setResultTestState(1);
        }else {
            roundResult.setResultTestState(0);
        }
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(order);
        roundResult.setMachineResult(order);
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(startTime+"");
        //??????????????????
        roundResult.setEndTime(System.currentTimeMillis() + "");
        roundResult.setRoundNo(testRound);
        roundResult.setTestNo(testNo);
        roundResult.setExamType(studentItem.getExamType());
        roundResult.setScheduleNo(studentItem.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());

        RoundResult bestResult = DBManager.getInstance().queryBestScore(studentItem.getStudentCode(), testNo);
        if (bestResult != null) {
            // ???????????????????????? ????????????????????????????????????????????????????????????
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL &&  bestResult.getResult() <= order) {
                // ????????????????????????????????????????????????
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
//                updateLastResultLed(roundResult);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
//                    updateLastResultLed(roundResult);
                } else {
                    roundResult.setIsLastResult(0);
//                    updateLastResultLed(bestResult);
                }
            }
        } else {
            // ???????????????
            roundResult.setIsLastResult(1);
//            updateLastResultLed(roundResult);
        }

        DBManager.getInstance().insertRoundResult(roundResult);
        LogUtils.operation("????????????:" + roundResult.toString());

    }


    public ShootSetting getSetting() {
        return setting;
    }
    public List<StuDevicePair> getPairs() {
        return pairs;
    }

    private RunTimerImpl runTimer = new RunTimerImpl(new RunTimerImpl.RunTimerListener() {
        @Override
        public void onGetTime(RunTimerResult result) {
            getResult(result);
        }

        @Override
        public void onConnected(RunTimerConnectState connectState) {
            Log.i(TAG,connectState.toString());
            disposeConnect(connectState);
        }

        @Override
        public void onTestState(int testState) {
            changeState(testState);
//            switch (testState) {
//                case 0:
//                case 1:
//                case 5://????????????
//                    changeState(new boolean[]{true,false,false,false,false});
//                    break;
//                case 2://????????????
//                    baseTimer = System.currentTimeMillis();
//                    startTime = System.currentTimeMillis();
//                    changeState(new boolean[]{false, true, true, false, false});
//                    break;
//                case 3://??????
//                    //??????????????????
//                    changeState(new boolean[]{false, true, false, false, false});
//                    break;
//                case 4://???????????????
//                    changeState(new boolean[]{false,true,false,false,false});
//                case 6://????????????
//                    changeState(new boolean[]{true,true,false,true,true});
//                    break;
//            }
        }
    });

    public void disposeConnect(RunTimerConnectState connectState) {

    }


    /**
     * @param state
     */
    public  void changeState(int state){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.setRS232ResiltListener(null);
    }

    public abstract void getResult(RunTimerResult result);
}
