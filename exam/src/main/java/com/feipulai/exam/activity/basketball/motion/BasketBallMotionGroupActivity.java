package com.feipulai.exam.activity.basketball.motion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.TimerTask;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.BasketBallGroupActivity;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.BasketBallSettingActivity;
import com.feipulai.exam.activity.basketball.adapter.BasketBallResultAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.sport_timer.SportContract;
import com.feipulai.exam.activity.sport_timer.SportPresent;
import com.feipulai.exam.activity.sport_timer.TestState;
import com.feipulai.exam.activity.sport_timer.bean.DeviceState;
import com.feipulai.exam.activity.sport_timer.bean.SportTimeResult;
import com.feipulai.exam.activity.sport_timer.pair.SportPairActivity;
import com.feipulai.exam.adapter.VolleyBallGroupStuAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.PrintResultUtil;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BasketBallMotionGroupActivity extends BaseTitleActivity implements BaseQuickAdapter.OnItemClickListener, SportContract.SportView, TimerTask.TimeUpdateListener {
    private static final int UPDATE_ON_STOP = 0xF4;
    private static final int UPDATE_ON_WAIT = 0xF7;
    protected volatile int state = WAIT_FREE;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    private final int UPDATE_ON_TEXT = 0XF5;

    private BasketBallSetting setting;
    private List<BaseStuPair> stuPairs;
    private List<DeviceState> deviceStates;
    private List<StuDevicePair> pairs;
    private BasketBallResultAdapter resultAdapter;
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private boolean startTest = true;
    private int roundNo;
    private Group group;
    private SportPresent sportPresent;
    private TimerTask timerTask;

    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestingPairs;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_continue_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.tv_group_name)
    TextView tvGroupName;
    @BindView(R.id.cb_near)
    CheckBox cbDeviceState;
    private List<BasketBallTestResult> resultList = new ArrayList<>();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ON_TEXT:
                    int time = (int) msg.obj;
                    String formatTime ;
                    if (time<60*60*1000){
                        formatTime = DateUtil.formatTime1(time, "mm:ss.SSS");
                    }else {
                        formatTime = DateUtil.formatTime1(time, "HH:mm:ss");
                    }
                    tvResult.setText(formatTime);
                    break;
                case UPDATE_ON_STOP:
                    setOperationUI();
                    break;
                case UPDATE_ON_WAIT:
                    txtWaiting.setEnabled(false);
                    txtIllegalReturn.setEnabled(true);
                    sportPresent.setRunState(1);
                    state = WAIT_CONFIRM;
                    break;
            }
            return true;
        }
    });

            @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        return builder.setTitle("篮球运球"+hostId+"号机").addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }
    private void gotoItemSetting() {
        if (!isConfigurableNow()) {
            LogUtils.operation("跳转至篮球项目设置界面");
            IntentUtil.gotoActivityForResult(this, BasketBallSettingActivity.class, 1);
        } else {
            toastSpeak("测试中,不允许修改设置");
        }
    }
    private boolean isConfigurableNow() {
        boolean flag = !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
        return flag;
    }
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_basketball;
    }

    @OnClick({R.id.tv_punish_add,R.id.tv_punish_subtract,R.id.tv_foul,R.id.tv_inBack,R.id.tv_abandon,R.id.tv_normal,
    R.id.txt_waiting,R.id.txt_illegal_return,R.id.txt_continue_run,R.id.txt_stop_timing,R.id.tv_print,R.id.tv_confirm,R.id.txt_finish_test})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_punish_add:
                setPunish(1);
                break;
            case R.id.tv_punish_subtract:
                setPunish(-1);
                break;
            case R.id.tv_foul:
                setResultState(RoundResult.RESULT_STATE_FOUL);
                break;
            case R.id.tv_inBack:
                setResultState(RoundResult.RESULT_STATE_BACK);
                break;
            case R.id.tv_abandon:
                setResultState(RoundResult.RESULT_STATE_WAIVE);
                break;
            case R.id.tv_normal:
                setResultState(RoundResult.RESULT_STATE_NORMAL);
                break;
            case R.id.txt_waiting:
                if ((state == WAIT_CHECK_IN || state == WAIT_CONFIRM || state == WAIT_STOP)) {
                    if (isExistTestPlace()) {
                        sportPresent.waitStart();
                        state = WAIT_BEGIN;
                    }
                }
                break;
            case R.id.txt_illegal_return://违例返回
                showIllegalReturnDialog();
                break;
            case R.id.txt_continue_run:
                break;
            case R.id.txt_stop_timing:
                if (state == WAIT_BEGIN) {
                    sportPresent.setDeviceStateStop();
                    timerTask.stopKeepTime();
                    state = WAIT_STOP;
                    setOperationUI();
                    sportPresent.getDeviceState();
                }
                break;
            case R.id.tv_print:
                showPrintDialog();
                break;
            case R.id.tv_confirm:
                LogUtils.operation("篮球点击了确定");
                if (SettingHelper.getSystemSetting().isInputTest()) {
                    onResultConfirmed();
                    return;
                }
                timerTask.stopKeepTime();
                if (state == WAIT_CONFIRM || state == WAIT_BEGIN) {
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                    sportPresent.setDeviceStateStop();
                    startTest = false;
                }
                break;
            case R.id.txt_finish_test:
                LogUtils.operation("篮球点击了跳过");
                if (state == TESTING) {
                    toastSpeak("测试中,不允许跳过本次测试");
                } else {
                    if (group.getIsTestComplete() == Group.FINISHED) {
                        toastSpeak("分组考生全部测试完成，请选择下一组");
                    } else {
                        timerTask.stopKeepTime();

                        sportPresent.setDeviceStateStop();
                        prepareForFinish();
                    }
                }
                break;
        }
    }
    private void prepareForFinish() {

        state = WAIT_FREE;
        setOperationUI();
        if (setting.getTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
            continuousTestNext();
        } else {
            //循环
            loopTestNext();
        }
    }
    private void setResultState(int resultState){
        if (state == TESTING || state == WAIT_BEGIN) {
            toastSpeak("测试中,不允许更改考试成绩状态");
        } else {
            if (resultAdapter.getSelectPosition() == -1)
                return;
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            if (testResult.getResult() == 0 && testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL
                    && resultState == RoundResult.RESULT_STATE_NORMAL) {
                toastSpeak("成绩不存在，不允许修改为正常状态");
                return;
            }
            if (testResult.getResult() < 0 && resultState == RoundResult.RESULT_STATE_NORMAL) {
                resultList.get(resultAdapter.getSelectPosition()).setResultState(-999);
            } else {
                resultList.get(resultAdapter.getSelectPosition()).setResultState(resultState);
            }

            resultAdapter.notifyDataSetChanged();
            LogUtils.operation("修改成绩状态:resultState=" + resultState);
        }
    }
    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
        if (setting == null)
            setting = new BasketBallSetting();
        TestCache.getInstance().init();

        stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
        pairs = CheckUtils.newPairs(stuPairs.size(),stuPairs);
        //分组标题
        group = (Group) TestConfigs.baseGroupMap.get("group");
        String type = "男女混合";
        if (group.getGroupType() == Group.MALE) {
            type = "男子";
        } else if (group.getGroupType() == Group.FEMALE) {
            type = "女子";
        }
        tvGroupName.setText(String.format(Locale.CHINA, "%s第%d组", type, group.getGroupNo()));
        //获取分组学生数据
        TestCache.getInstance().init();

        stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
        pairs = CheckUtils.newPairs(stuPairs.size(),stuPairs);
        CheckUtils.groupCheck(pairs);

        resultAdapter = new BasketBallResultAdapter(resultList, setting);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isConfigurableNow()) {
                    resultAdapter.setSelectPosition(position);
                    resultAdapter.notifyDataSetChanged();
                }
            }
        });
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setOnItemClickListener(this);

        sportPresent = new SportPresent(this,2);
        timerTask = new TimerTask(this,100);
        timerTask.keepTime();
        deviceStates = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            DeviceState deviceState = new DeviceState();
            deviceState.setDeviceId(i + 1);
            deviceState.setDeviceState(0);
            deviceStates.add(deviceState);
        }

    }
    private boolean isExistTestPlace() {
        if (resultAdapter.getSelectPosition() == -1)
            return false;
        if (resultList.get(resultAdapter.getSelectPosition()).getResultState() != -999) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getResultState() == -999) {
                    resultAdapter.setSelectPosition(i);
                    if (startTest) {
                        roundNo = i + 1;
                        startTest = false;
                    }

//                    Log.i("roundNo", "isExistTestPlace" + roundNo);
                    resultAdapter.notifyDataSetChanged();
                    return true;
                } else {
                    if (isFullSkip(resultList.get(i).getResult(), resultList.get(i).getResultState())) {
                        return false;
                    }
                }
            }
            return false;
        } else {
            roundNo = resultAdapter.getSelectPosition() + 1;
            Log.i("roundNo", "isExistTestPlace resultAdapter" + roundNo);
            return true;
        }

    }
    private boolean isFullSkip(int result, int resultState) {
        Student student = pairs.get(position()).getStudent();
        if (setting.isFullSkip() && resultState == RoundResult.RESULT_STATE_NORMAL) {
//            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            if (student.getSex() == Student.MALE) {
                return result <= setting.getMaleFullScore() * 1000;
            } else {
                return result <= setting.getFemaleFullScore() * 1000;
            }
        }
        return false;
    }
    private int position() {
        return stuPairAdapter.getTestPosition();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (!isConfigurableNow()) {
            resultAdapter.setSelectPosition(-1);
            stuPairAdapter.setTestPosition(position);
            rvTestingPairs.scrollToPosition(position);
            stuPairAdapter.notifyDataSetChanged();
            presetResult();
            if (isExistTestPlace()) {
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
                prepareForBegin();
            } else {
                state = WAIT_FREE;
                setOperationUI();
            }

        } else {
            ToastUtils.showShort("测试中,不能更换考生");
        }
    }
    private void presetResult() {
        resultList.clear();
        resultAdapter.setSelectPosition(-1);
        Student student = TestCache.getInstance().getAllStudents().get(position());
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        roundNo = (roundResults == null ? 1 : roundResults.size() + 1);
        Log.i("roundNo", "presetResult" + roundNo);
        for (int i = 0; i < setTestCount(); i++) {
            RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId() + "", i + 1);
            if (roundResult == null) {
                resultList.add(new BasketBallTestResult(i + 1, null, 0, -999, 0, -999));
                if (resultAdapter.getSelectPosition() == -1) {
                    resultAdapter.setSelectPosition(i);
                }
            } else {
                List<MachineResult> machineResultList = DBManager.getInstance().getItemGroupFRoundMachineResult(student.getStudentCode(),
                        group.getId(), i + 1);
                resultList.add(new BasketBallTestResult(i + 1, machineResultList, roundResult.getMachineResult(), roundResult.getResult(), roundResult.getPenaltyNum(), roundResult.getResultState()));

            }

        }
        resultAdapter.notifyDataSetChanged();

    }

    private void prepareForBegin() {
        LogUtils.operation("篮球考生:" + pairs.get(position()).getStudent().getStudentName() + "进行第" + roundNo + "轮测试");
        Student student = pairs.get(position()).getStudent();
        tvResult.setText(student.getStudentName());
        state = WAIT_CHECK_IN;
        setOperationUI();
//        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(1,
//                UdpLEDUtil.getLedByte(pairs.get(position()).getStudent().getSpeakStuName(), Paint.Align.CENTER)));
//        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
//                UdpLEDUtil.getLedByte("", Paint.Align.RIGHT)));
        sportPresent.waitStart();
    }
    private void setOperationUI() {
        switch (state) {
            case WAIT_FREE:
                txtWaiting.setEnabled(false);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(false);

                break;
            case WAIT_CHECK_IN:
                txtWaiting.setEnabled(true);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(false);
                break;
            case WAIT_BEGIN:
                txtWaiting.setEnabled(false);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(true);
                break;
            case TESTING:
//                List<MachineResult> machineResultList = resultList.get(resultAdapter.getSelectPosition()).getMachineResultList();
//                if (machineResultList != null && machineResultList.size() > 0) {
//                    txtIllegalReturn.setEnabled(false);
//                } else {
//                    txtIllegalReturn.setEnabled(true);
//                }
                txtWaiting.setEnabled(false);
                txtIllegalReturn.setEnabled(true);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(true);
                break;
            case WAIT_STOP:
                txtWaiting.setEnabled(true);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(false);
                break;
            case WAIT_CONFIRM:
                txtWaiting.setEnabled(true);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(true);
                txtStopTiming.setEnabled(false);
                break;

        }

    }

    @Override
    public void updateDeviceState(int deviceId, int state) {
        boolean flag = false;
        for (DeviceState deviceState : deviceStates) {
            if (deviceState.getDeviceState() == 0) {
                flag = false;
                break;
            } else {
                flag = true;
            }
        }
        if (flag != cbDeviceState.isChecked()) {
            final boolean b = flag;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cbDeviceState.setChecked(b);
                }
            });

        }
    }

    @Override
    public void getDeviceStart() {
        mHandler.sendEmptyMessage(UPDATE_ON_WAIT);
    }

    @Override
    public void receiveResult(SportResult sportResult) {

    }


    public void getDeviceStop() {
        if (state == WAIT_CONFIRM) {
            state = WAIT_CHECK_IN;
            sportPresent.setRunState(0);
            mHandler.sendEmptyMessage(UPDATE_ON_STOP);
        }
    }

    private int setTestCount() {
        return TestConfigs.getMaxTestCount();
    }

    @Override
    public void onTimeTaskUpdate(int time) {
        Message message = mHandler.obtainMessage();
        message.what = UPDATE_ON_TEXT;
        message.obj = time;
        mHandler.sendMessage(message);
    }
    private void setPunish(int punishType) {
        if (state == TESTING || state == WAIT_BEGIN) {
            toastSpeak("测试中,不允许更改考试成绩");
        } else {
            if (resultAdapter.getSelectPosition() == -1)
                return;
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            if ((testResult.getResult() <= 0 && (testResult.getResultState() == -999
                    || testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL))) {
                toastSpeak("成绩不存在");
                return;
            }
            int penalizeNum = testResult.getPenalizeNum();
            LogUtils.operation("原始成绩判罚数:" + penalizeNum);
            if (punishType >= 0) {//+
                testResult.setPenalizeNum(penalizeNum + 1);
            } else {//-
                if (penalizeNum > 0) {
                    testResult.setPenalizeNum(penalizeNum - 1);
                }
            }
            LogUtils.operation("判罚后成绩判罚数:" + penalizeNum);
            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * (int)(setting.getPenaltySecond() * 1000));
            testResult.setResult(result);

            resultAdapter.notifyDataSetChanged();
        }
    }
    private void showIllegalReturnDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.illegal_return_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                sportPresent.setDeviceStateStop();
                timerTask.stopKeepTime();
                BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
                List<MachineResult> machineResultList = testResult.getMachineResultList();
                if (machineResultList != null && machineResultList.size() > 0) {
                    Student student = pairs.get(position()).getStudent();
                    int testNo = 1;
                    DBManager.getInstance().deleteStuResult(student.getStudentCode(), testNo, roundNo, group.getId());
                    DBManager.getInstance().deleteStuMachineResults(student.getStudentCode(), testNo, roundNo, group.getId());
                    testResult.setSelectMachineResult(0);
                    testResult.setResult(-999);
                    testResult.setResultState(-999);
                    testResult.setMachineResultList(null);
                    resultAdapter.notifyDataSetChanged();
                    LogUtils.operation("篮球考生" + student.getStudentName() + "第" + roundNo + "轮进行违规返回");
                }
                state = WAIT_CONFIRM;

//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
//                sleep();
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }
    private void showPrintDialog() {
        String[] printType = new String[]{"个人", "整组"};
        new AlertDialog.Builder(this).setTitle("选择成绩打印类型")
                .setItems(printType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TestCache testCache = TestCache.getInstance();
                        switch (which) {
                            case 0:
                                List<RoundResult> stuResult = testCache.getResults().get(pairs.get(position()).getStudent());
                                if (stuResult == null || stuResult.size() == 0) {
                                    toastSpeak("该考生未进行考试");
                                    return;
                                }
                                PrintResultUtil.printResult(pairs.get(position()).getStudent().getStudentCode());
                                break;
                            case 1:

                                InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                                        TestConfigs.getMaxTestCount(), testCache.getTrackNoMap());
                                break;
                        }
                    }
                }).create().show();
    }
    private void onResultConfirmed() {
        if (pairs.get(position()) == null)
            return;
        Student student = pairs.get(position()).getStudent();
        List<RoundResult> updateResult = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++) {
            BasketBallTestResult testResult = resultList.get(i);
            if (testResult.getResult() >= 0) {
                RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId() + "", resultList.get(i).getRoundNo());
                if (roundResult != null) {
                    //是否有进行更改，有更改成绩为未上传
                    if (roundResult.getResult() != testResult.getResult() || roundResult.getPenaltyNum() != testResult.getPenalizeNum()
                            || roundResult.getResultState() != testResult.getResultState()) {
                        LogUtils.operation("更新修改成绩前" + roundResult.toString());
                        roundResult.setUpdateState(0);
                        roundResult.setResult(testResult.getResult());
                        roundResult.setPenaltyNum(testResult.getPenalizeNum());
                        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
                        roundResult.setResultState(testResult.getResultState());
                        LogUtils.operation("更新修改成绩后" + roundResult.toString());
                        updateResult.add(roundResult);
                    }
                }
            } else {
                if (testResult.getResultState() != -999 && testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    RoundResult roundResult = new RoundResult();
                    roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    roundResult.setStudentCode(student.getStudentCode());
                    roundResult.setItemCode(TestConfigs.getCurrentItemCode());
                    roundResult.setResult(0);
                    roundResult.setMachineResult(0);
                    roundResult.setResultState(testResult.getResultState());
                    roundResult.setTestTime(System.currentTimeMillis() + "");
                    roundResult.setEndTime(DateUtil.getCurrentTime() + "");
                    roundResult.setRoundNo(resultList.get(i).getRoundNo());
                    roundResult.setTestNo(1);
                    roundResult.setExamType(group.getExamType());
                    roundResult.setScheduleNo(group.getScheduleNo());
                    roundResult.setUpdateState(0);
                    roundResult.setGroupId(group.getId());
                    roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
                    LogUtils.operation("篮球保存成绩到数据库:" + roundResult.toString());
                    DBManager.getInstance().insertRoundResult(roundResult);
                    resultList.get(i).setResult(0);
                    resultAdapter.notifyDataSetChanged();
                    SystemSetting setting = SettingHelper.getSystemSetting();
                    StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),stuPairs.get(stuPairAdapter.getTestPosition()).getStudent().getStudentCode());
                    //判断是否开启补考需要加上是否已完成本次补考,并将学生改为已补考
                    if ((setting.isResit() || studentItem.getMakeUpType() == 1) && !stuPairs.get(stuPairAdapter.getTestPosition()).isResit()){
                        stuPairs.get(stuPairAdapter.getTestPosition()).setResit(true);
                    }
                }

            }

        }
        if (updateResult.size() > 0) {
            DBManager.getInstance().updateRoundResult(updateResult);
        }

        List<RoundResult> dbRoundResult = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
        //获取最小成绩设置为最好成绩
        RoundResult dbAscResult = DBManager.getInstance().queryGroupOrderAscScore(student.getStudentCode(), group.getId());


        if (dbAscResult != null) {
            //获取所有成绩设置为非最好成绩
            for (RoundResult roundResult : dbRoundResult) {
                if (roundResult.getResult() == dbAscResult.getResult()) {
                    dbAscResult.setIsLastResult(1);
                } else {
                    roundResult.setIsLastResult(0);
                }

                DBManager.getInstance().updateRoundResult(roundResult);
            }
//            dbAscResult.setIsLastResult(1);
//            DBManager.getInstance().updateRoundResult(dbAscResult);
        } else if (dbRoundResult != null && dbRoundResult.size() > 0) {
            dbRoundResult.get(0).setIsLastResult(1);
            DBManager.getInstance().updateRoundResult(dbRoundResult.get(0));
        }

        if (dbRoundResult != null) {
            TestCache.getInstance().getResults().put(student, dbRoundResult);
        }


        uploadResults();

        nextTest();

    }
    /**
     * 上传成绩
     */
    private void uploadResults() {
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            Student student = pairs.get(position()).getStudent();
            List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
            if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                return;
            }
            if (roundResultList.size() == 0 || roundResultList == null) {
                return;
            }
            List<UploadResults> uploadResults = new ArrayList<>();
            String groupNo = group.getGroupNo() + "";
            String scheduleNo = group.getScheduleNo();
            String testNo = "1";
            UploadResults uploadResult = new UploadResults(scheduleNo,
                    TestConfigs.getCurrentItemCode(), student.getStudentCode()
                    , testNo, group, RoundResultBean.beanCope(roundResultList, group));
            uploadResults.add(uploadResult);
            Logger.i("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }
    private void nextTest() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //连续测试
                if (setting.getTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                    continuousTest();
                } else {
                    //循环
                    loopTestNext();
                }
            }
        }, 3000);

    }
    /**
     * 连续测试
     */
    private void continuousTest() {
        if (roundNo <= setTestCount()) {
            //是否存在可以测试位置
            if (isExistTestPlace()) {
                prepareForBegin();
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            } else {
                continuousTestNext();
            }
        } else {
            //是否测试到最后一位
            if (position() == pairs.size() - 1) {
                fristCheckTest();
            } else {
                continuousTestNext();
            }


        }
    }


    /**
     * 连续测试下一位
     */
    private void continuousTestNext() {

        for (int i = (position() + 1); i < pairs.size(); i++) {

            if (isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                continue;
            }
            stuPairAdapter.setTestPosition(i);
            rvTestingPairs.scrollToPosition(i);
            presetResult();
            prepareForBegin();
            //最后一次测试的成绩
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);
            stuPairAdapter.notifyDataSetChanged();
            return;
        }
        //全部次数测试完，
        fristCheckTest();

    }

//    private void loopTest() {
//        //是否测试到最后一位
//        if (stuPairAdapter.getTestPosition() == pairs.size() - 1) {
//            fristCheckTest();
//        } else {
//            loopTestNext();
//        }
//    }

    private void fristCheckTest() {
        //是否为最后一次测试，开启新的测试
        for (int i = 0; i < pairs.size(); i++) {
            if (!isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                stuPairAdapter.setTestPosition(i);
                rvTestingPairs.scrollToPosition(i);
                presetResult();
                isExistTestPlace();
                prepareForBegin();
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
                stuPairAdapter.notifyDataSetChanged();
                return;
            }
        }
        if (SettingHelper.getSystemSetting().isAutoPrint()) {
            TestCache testCache = TestCache.getInstance();
            InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(), testCache.getTrackNoMap());
        }
        allTestComplete();
    }

    /**
     * 循环测试下一位测试
     */
    private void loopTestNext() {
        for (int i = (position() + 1); i < pairs.size(); i++) {
            if (isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                continue;
            }
            stuPairAdapter.setTestPosition(i);
            rvTestingPairs.scrollToPosition(i);
            presetResult();
            prepareForBegin();
            //最后一次测试的成绩
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            stuPairAdapter.notifyDataSetChanged();

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);

            return;
        }
//        if (position() + 1 < pairs.size()) {
//            loopTestNext();
//        }
        fristCheckTest();
    }


    /**
     * 全部次数测试完
     */
    private void allTestComplete() {
        LogUtils.operation("篮球分组模式测试完成");
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        if (group.getIsTestComplete() != 1 &&
                SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN &&
                (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 || SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP) &&
                SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printA4Result(this, group);
        }
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
        state = WAIT_FREE;
        setOperationUI();

        //        TestCache testCache = TestCache.getInstance();
//        if (SettingHelper.getSystemSetting().isAutoPrint() && isExistTestPlace()) {
//            InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
//                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
//        }
    }

    /**
     * 考生是否全部测试完成或满分
     *
     * @param studentCode
     * @return
     */
    private boolean isStuAllTest(String studentCode) {
        //  查询学生成绩 当有成绩则添加数据跳过测试
        List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                (studentCode, group.getId() + "");
        //成绩数量是否小于测试次数
        if (roundResultList.size() < TestConfigs.getMaxTestCount(this)) {
            boolean isSkip = false;
            for (RoundResult roundResult : roundResultList) {
                //成绩是否存在满分跳过
                if (isFullSkip(roundResult.getResult(), roundResult.getResultState())) {
                    LogUtils.operation("篮球考生:" + studentCode + "第" + roundNo + "轮已满分,跳过测试");
                    isSkip = true;
                }
            }
            if (!isSkip) {
                return false;
            }
        }
        return true;

    }

}
