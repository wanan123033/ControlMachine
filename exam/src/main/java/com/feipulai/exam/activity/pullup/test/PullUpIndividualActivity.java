package com.feipulai.exam.activity.pullup.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.PenalizeDialog;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.pullup.setting.PullUpSettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.EditResultDialog;
import com.feipulai.exam.view.WaitDialog;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class PullUpIndividualActivity extends BaseTitleActivity
        implements PullUpTestFacade.Listener,
        IndividualCheckFragment.OnIndividualCheckInListener, BaseAFRFragment.onAFRCompareListener {

    private static final int UPDATE_SCORE = 0x3;

    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.ll_stu_detail)
    LinearLayout llStuDetail;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.tv_start_test)
    TextView tvStartTest;
    @BindView(R.id.tv_stop_test)
    TextView tvStopTest;
    @BindView(R.id.tv_time_count)
    TextView tvTimeCount;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_punish)
    TextView tvPunish;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.tv_abandon_test)
    TextView tvAbandonTest;
    @BindView(R.id.tv_finish_test)
    TextView tvFinishTest;
    @BindView(R.id.tv_exit_test)
    TextView tvExitTest;
    @BindView(R.id.tv_resurvey)
    TextView tvResurvey;

    private PullUpTestFacade facade;
    private IndividualCheckFragment individualCheckFragment;
    // 状态  WAIT_CHECK_IN--->WAIT_BEGIN--->TESTING---->WAIT_CONFIRM--->WAIT_CHECK_IN
    private static final int WAIT_CHECK_IN = 0x0;
    private static final int WAIT_BEGIN = 0x1;
    private static final int TESTING = 0x2;
    private static final int WAIT_CONFIRM = 0x3;
    protected volatile int state = WAIT_CHECK_IN;
    private Handler handler = new MyHandler(this);
    private PullUpSetting setting;
    private LEDManager ledManager = new LEDManager();
    private String testDate;
    private SystemSetting systemSetting;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private WaitDialog changBadDialog;
    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private EditResultDialog editResultDialog;
    private List<String> resultList = new ArrayList<>();
    private BasePersonTestResultAdapter adapter;
    private PenalizeDialog penalizeDialog;
    private String[] lastResult;
    private Student lastStudent;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_individual_pullup;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();
        setting = SharedPrefsUtil.loadFormSource(this, PullUpSetting.class);

        TestCache.getInstance().clear();

        StuDevicePair pair = new StuDevicePair();
        BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_DISCONNECT);
        pair.setBaseDevice(deviceState);
        pair.setDeviceResult(new PullUpStateResult());
        pairs.add(pair);

        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setResultView(lvResults);
        individualCheckFragment.setOnIndividualCheckInListener(this);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);

        facade = new PullUpTestFacade(SettingHelper.getSystemSetting().getHostId(), this);
        ledManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        prepareForCheckIn();

        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
        editResultDialog = new EditResultDialog(this);
        editResultDialog.setListener(new EditResultDialog.OnInputResultListener() {

            @Override
            public void inputResult(String result, int state) {
                pairs.get(0).getDeviceResult().setResult(ResultDisplayUtils.getDbResultForUnit(Double.valueOf(result)));
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult());
                tvResult.setText(displayResult);
                editResultDialog.dismissDialog();
            }
        });

        if (SettingHelper.getSystemSetting().isAgainTest()) {
            tvResurvey.setVisibility(View.VISIBLE);
        }
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BasePersonTestResultAdapter(resultList);
        rvTestResult.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                adapter.setSelectPosition(i);
                adapter.notifyDataSetChanged();
            }
        });
        penalizeDialog = new PenalizeDialog(this, TestConfigs.getMaxTestCount());
    }

    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// 提交更改
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (individualCheckFragment.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addRightText("外接屏幕", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfigurableNow()) {
                    startActivity(new Intent(PullUpIndividualActivity.this, LEDSettingActivity.class));
                } else {
                    toastSpeak("测试中,不能进行外接屏幕设置");
                }
            }
        }).addRightText("项目设置", new View.OnClickListener() {
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

    private void startProjectSetting() {
        if (isConfigurableNow()) {
            startActivity(new Intent(PullUpIndividualActivity.this, PullUpSettingActivity.class));
        } else {
            toastSpeak("测试中,不允许修改设置");
        }
    }

    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {

        if (state != WAIT_CHECK_IN) {
            toastSpeak("当前考生还未完成测试,拒绝检录");
            return;
        }

        pairs.get(0).setStudent(student);
        TestCache.getInstance().init();
        TestCache.getInstance().getAllStudents().add(student);
        TestCache.getInstance().getResults().put(student, results);

        RoundResult lastResult = null;
        if (results == null || results.size() == 0) {
            TestCache.getInstance().getResults().put(student,
                    results != null ? results
                            : new ArrayList<RoundResult>(TestConfigs.getMaxTestCount(this)));
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            int testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
            TestCache.getInstance().getTestNoMap().put(student, testNo);
        } else {
            lastResult = results.get(results.size() - 1);
            TestCache.getInstance().getResults().put(student, results);
//            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());

//            int testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
            int testNo = results.get(0).getTestNo();
            TestCache.getInstance().getTestNoMap().put(student, testNo);
        }

        //保存成绩，并测试轮次大于测试轮次次数
        List<RoundResult> roundResultAll = DBManager.getInstance().queryFinallyRountScoreByExamTypeAll(student.getStudentCode(), studentItem.getExamType());
        if (roundResultAll.size() >= TestConfigs.getMaxTestCount()) {
            List<Integer> rounds = new ArrayList<>();
            for (int i = 0; i < results.size(); i++) {
                if (results.size() > 0) {  //需要改变轮次
                    int roundNo = results.get(i).getRoundNo();
                    rounds.add(roundNo);
                }
            }

            for (int j = 1; j <= TestConfigs.getMaxTestCount(); j++) {
                if (!rounds.contains(j)) {
                    pairs.get(0).setCurrentRoundNo(j);
                    break;
                }
            }
        }
        if (pairs.get(0).getCurrentRoundNo() == 0) {
            pairs.get(0).setCurrentRoundNo(results.size() + 1);
        }

        TestCache.getInstance().setTestingPairs(pairs);
        TestCache.getInstance().getStudentItemMap().put(student, studentItem);

        pairs.get(0).setDeviceResult(new PullUpStateResult());
        pairs.get(0).setPenalty(0);

        tvResult.setText(student.getStudentName());
        prepareForBegin();
        displayCheckedInLED(lastResult);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            ToastUtils.showShort("自动上传失败，请先进行登录");
        }
        switch (baseEvent.getTagInt()) {
            case EventConfigs.INSTALL_RESULT:
                RoundResult iRoundResult = (RoundResult) baseEvent.getData();
                String tmp = RoundResult.resultStateStr(iRoundResult.getResultState(), iRoundResult.getResult());

                if (TextUtils.equals(pairs.get(0).getStudent().getStudentCode(), iRoundResult.getStudentCode())) {
                    resultList.remove(iRoundResult.getRoundNo() - 1);
                    resultList.add(iRoundResult.getRoundNo() - 1, tmp);
                    String displayInLed = "成绩:" + tmp;
                    ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                    if (systemSetting.isAutoBroadcast()) {
                        TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), pairs.get(0).getStudent().getSpeakStuName(),
                                tmp));
                    }
                }
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(pairs.get(0).getStudent().getStudentCode());

                adapter.notifyDataSetChanged();
                pairs.get(0).setDeviceResult(new PullUpStateResult());
                pairs.get(0).setPenalty(0);
                List<RoundResult> roundResultList = DBManager.getInstance().queryResultsByStuItem(studentItem);
                TestCache.getInstance().getResults().put(pairs.get(0).getStudent(), roundResultList);
                if (shouldContinue()) {
                    prepareForBegin();
                } else {
                    prepareForFinish();
                }

                break;
            case EventConfigs.UPDATE_RESULT:
                RoundResult roundResult = (RoundResult) baseEvent.getData();
                if (TextUtils.equals(pairs.get(0).getStudent().getStudentCode(), roundResult.getStudentCode())) {

                    tmp = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
                    resultList.remove(roundResult.getRoundNo() - 1);
                    resultList.add(roundResult.getRoundNo() - 1, tmp);
                    if (systemSetting.isAutoBroadcast()) {
                        TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), pairs.get(0).getStudent().getSpeakStuName(),
                                tmp));
                    }
                    adapter.notifyDataSetChanged();
                    studentItem = DBManager.getInstance().queryStuItemByStuCode(pairs.get(0).getStudent().getStudentCode());

                    adapter.notifyDataSetChanged();
                    pairs.get(0).setDeviceResult(new PullUpStateResult());
                    pairs.get(0).setPenalty(0);
                    roundResultList = DBManager.getInstance().queryResultsByStuItem(studentItem);
                    TestCache.getInstance().getResults().put(pairs.get(0).getStudent(), roundResultList);
                    if (roundResult.getRoundNo() == pairs.get(0).getCurrentRoundNo()) {
                        tmp = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
                        String displayInLed = "成绩:" + tmp;
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);

                    }
                    if (shouldContinue()) {
                        prepareForBegin();
                    } else {
                        prepareForFinish();
                    }
                }

                break;

        }
    }

    protected void displayCheckedInLED(RoundResult lastResult) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        ledManager.showString(hostId, pairs.get(0).getStudent().getLEDStuName(), 5, 0, true, lastResult == null);
        if (lastResult != null) {
            String displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
            ledManager.showString(hostId, "已有成绩:" + displayResult, 2, 3, false, true);
        }
    }

    @OnClick({R.id.tv_start_test, R.id.tv_stop_test, R.id.tv_print, R.id.tv_led_setting, R.id.tv_confirm, R.id.tv_result,
            R.id.tv_punish, R.id.tv_abandon_test, R.id.tv_finish_test, R.id.tv_exit_test, R.id.tv_pair, R.id.img_AFR,
            R.id.tv_resurvey, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal})
    public void onViewClicked(View view) {
        String[] resultArray = new String[TestConfigs.getMaxTestCount()];
        resultList.toArray(resultArray);
        switch (view.getId()) {

            case R.id.tv_led_setting:
                if (isConfigurableNow()) {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                } else {
                    toastSpeak("测试中,不能进行外接屏幕设置");
                }
                break;

            case R.id.tv_start_test:
                prepareForTesting();
                break;

            case R.id.tv_stop_test:
                facade.stopTest();
                SoundPlayUtils.play(12);
                if (setting.isPenalize()) {
                    prepareForConfirmResult();
                } else {
                    tvResult.setText("");
                    InteractUtils.saveResults(pairs, testDate);
                    onResultConfirmed();
                }

                break;

            case R.id.tv_print:
                TestCache testCache = TestCache.getInstance();
                InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                        TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                break;

            case R.id.tv_confirm:
                tvResult.setText("");
                InteractUtils.saveResults(pairs, testDate);
                onResultConfirmed();
                break;

            case R.id.tv_punish:
                showPenalizeDialog(pairs.get(0).getDeviceResult().getResult());
                break;

            case R.id.tv_abandon_test:
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("温馨提示")
                        .setContentText("是否放弃本轮成绩？")
                        .setConfirmText( getString(com.feipulai.common.R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        facade.abandonTest();
                        state = WAIT_BEGIN;
                        prepareForBegin();
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }).setCancelText( getString(com.feipulai.common.R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }).show();

                break;

            case R.id.tv_finish_test:
                prepareForCheckIn();
                break;

            case R.id.tv_exit_test:
                lastStudent = pairs.get(0).getStudent();
                resultList.toArray(lastResult);
                prepareForCheckIn();
                break;

            case R.id.tv_pair:
                changeBadDevice();
                break;
            case R.id.img_AFR:
                showAFR();
                break;
            case R.id.tv_result:

                if (SettingHelper.getSystemSetting().isInputTest() && pairs.size() > 0 && pairs.get(0).getStudent() != null) {
                    editResultDialog.showDialog(pairs.get(0).getStudent());
                }
                break;
            case R.id.txt_test_result:

                if (SettingHelper.getSystemSetting().isInputTest() && pairs.get(0).getStudent() != null) {
                    editResultDialog.showDialog(pairs.get(0).getStudent());
                }
                break;
            case R.id.tv_foul:

                if (pairs.get(0).getStudent() == null) {
                    penalizeDialog.setData(0, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                } else {
                    penalizeDialog.setData(1, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                }
                penalizeDialog.showDialog(0);
                break;
            case R.id.tv_inBack:

                if (pairs.get(0).getStudent() == null) {

                    penalizeDialog.setData(0, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                } else {
                    penalizeDialog.setData(1, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                }
                penalizeDialog.showDialog(1);
                break;
            case R.id.tv_abandon:

                if (pairs.get(0).getStudent() == null) {
                    penalizeDialog.setData(0, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                } else {
                    penalizeDialog.setData(1, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                }
                penalizeDialog.showDialog(2);
                break;
            case R.id.tv_normal:

                if (null == pairs.get(0).getStudent()) {
                    penalizeDialog.setData(0, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                } else {
                    penalizeDialog.setData(1, pairs.get(0).getStudent(), resultArray, lastStudent, lastResult);
                }
                penalizeDialog.showDialog(3);
                break;
            case R.id.tv_resurvey:
                if (pairs.get(0).getStudent() == null) {
                    return;
                }
                int testNo = TestCache.getInstance().getTestNoMap().get(pairs.get(0).getStudent());
                StudentItem studentItem = TestCache.getInstance().getStudentItemMap().get(pairs.get(0).getStudent());
                AgainTestDialog dialog = new AgainTestDialog();
                RoundResult roundResult = DBManager.getInstance().queryRoundByRoundNo(pairs.get(0).getStudent().getStudentCode(), testNo, (adapter.getSelectPosition() + 1));
                if (roundResult == null) {
                    toastSpeak("当前轮次无成绩，请进行测试");
                    return;
                }
                List<RoundResult> results = new ArrayList<>();
                results.add(roundResult);
                dialog.setArguments(pairs.get(0).getStudent(), results, studentItem);
                dialog.setOnIndividualCheckInListener(new ResitDialog.onClickQuitListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int updateRoundNo) {
                        LogUtils.operation(pairs.get(0).getStudent().getStudentCode() + "重测第" + updateRoundNo + "轮成绩");
                        String[] resultArray = new String[resultList.size()];
                        resultList.toArray(resultArray);
                        resultArray[updateRoundNo - 1] = "";
                        resultList.clear();
                        resultList.addAll(Arrays.asList(resultArray));
                        //设置测试轮次
                        pairs.get(0).setCurrentRoundNo(updateRoundNo);
                        adapter.setIndexPostion(updateRoundNo - 1);
                        adapter.notifyDataSetChanged();
                        pairs.get(0).setDeviceResult(new PullUpStateResult());
                        pairs.get(0).setPenalty(0);
                        List<RoundResult> roundResultList = DBManager.getInstance().queryResultsByStuItem(studentItem);
                        TestCache.getInstance().getResults().put(student, roundResultList);
                        tvResult.setText(student.getStudentName());
                        prepareForBegin();
                        if (roundResultList.size() > 0) {
                            displayCheckedInLED(roundResultList.get(roundResultList.size() - 1));
                        }

                    }

                    @Override
                    public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {

                    }
                });
                dialog.show(getSupportFragmentManager(), "AgainTestDialog");


                break;
        }
    }

    private void onResultConfirmed() {
        StuDevicePair pair = pairs.get(0);
        int result = pair.getDeviceResult().getResult() + pair.getPenalty();
        Logger.i("引体向上成绩：" + ResultDisplayUtils.getStrResultForDisplay(result));
        if (systemSetting.isAutoBroadcast()) {
            TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), pair.getStudent().getSpeakStuName(),
                    ResultDisplayUtils.getStrResultForDisplay(result)));
        }

        // 是否需要进行下一次测试
        if (shouldContinue()) {
            adapter.setIndexPostion(pairs.get(0).getCurrentRoundNo() - 1);
            adapter.setSelectPosition(pairs.get(0).getCurrentRoundNo() - 1);
            prepareForBegin();
        } else {
            prepareForFinish();
        }
        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(), pair.getStudent().getStudentCode());
        if (studentItem.getExamType() == 2) {
            prepareForCheckIn();
        }
    }

    private boolean shouldContinue() {
        int maxTestNo = TestConfigs.getMaxTestCount(this);
        TestCache testCache = TestCache.getInstance();
        Student student = testCache.getAllStudents().get(0);
        //更新列表
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        String[] resultArray = new String[maxTestNo];
        if (roundResults != null) {
            for (RoundResult result : roundResults) {
                resultArray[result.getRoundNo() - 1] = RoundResult.resultStateStr(result.getResultState(), result.getResult());
            }
        }
        resultList.clear();
        resultList.addAll(Arrays.asList(resultArray));

        boolean isAllTest = true;
        for (int i = 0; i < resultList.size(); i++) {
            if (TextUtils.isEmpty(resultList.get(i))) {
                isAllTest = false;
                pairs.get(0).setCurrentRoundNo(i + 1);
                break;
            }
        }
        if (isAllTest) {
            return false;
        }


        return testCache.getResults().get(student).size() < maxTestNo;
    }

    private void prepareView(boolean rvResultEnable, boolean tvFinishTestEnable, boolean tvPrintEnable,
                             boolean tvStartTestEnable, boolean tvAbandonTestEnable, boolean tvConfirmEnable,
                             boolean tvStopTestEnable, boolean tvPunishEnable, boolean tvExitTestEnable, boolean tvCountEnable) {
        rvTestResult.setVisibility(rvResultEnable ? View.VISIBLE : View.INVISIBLE);
        tvFinishTest.setVisibility(tvFinishTestEnable ? View.VISIBLE : View.GONE);
        tvPrint.setVisibility(tvPrintEnable ? View.VISIBLE : View.GONE);

        tvStartTest.setVisibility(tvStartTestEnable ? View.VISIBLE : View.GONE);
        tvAbandonTest.setVisibility(tvAbandonTestEnable ? View.VISIBLE : View.GONE);
        tvConfirm.setVisibility(tvConfirmEnable ? View.VISIBLE : View.GONE);

        tvStopTest.setVisibility(tvStopTestEnable ? View.VISIBLE : View.GONE);
        tvPunish.setVisibility(tvPunishEnable ? View.VISIBLE : View.GONE);
        tvExitTest.setVisibility(tvExitTestEnable ? View.VISIBLE : View.GONE);

        tvTimeCount.setVisibility(tvCountEnable ? View.VISIBLE : View.GONE);

        if (rvResultEnable) {
            setAdapter();
        }
    }

    private void prepareForCheckIn() {
        ledManager.resetLEDScreen(systemSetting.getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        TestCache.getInstance().clear();
        InteractUtils.showStuInfo(llStuDetail, null, null);
        tvResult.setText("请检录");

        prepareView(false, false, false, false,
                false, false, false,
                false, false, false);

        state = WAIT_CHECK_IN;
        pairs.get(0).setCurrentRoundNo(0);
    }

    private void prepareForBegin() {
        TestCache testCache = TestCache.getInstance();
        Student student = pairs.get(0).getStudent();
        InteractUtils.showStuInfo(llStuDetail, student, testCache.getResults().get(student));

        tvResult.setText(student.getStudentName());

        prepareView(true, false, true, true,
                false, false, false,
                false, true, false);

        state = WAIT_BEGIN;
        adapter.setIndexPostion(pairs.get(0).getCurrentRoundNo() - 1);
        adapter.setSelectPosition(pairs.get(0).getCurrentRoundNo() - 1);
    }

    private void prepareForTesting() {
        if (pairs.get(0).getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT
                && !SettingHelper.getSystemSetting().isInputTest()) {
            toastSpeak("设备未连接,不能开始测试");
            return;
        }

        prepareView(true, false, false, false,
                true, false, false,
                false, false, true);

        tvResult.setText("准备");
        testDate = System.currentTimeMillis() + "";
        facade.startTest();
        state = TESTING;
    }

    private void prepareForConfirmResult() {
        state = WAIT_CONFIRM;
        prepareView(true, false, false, false,
                false, true, false,
                setting.isPenalize(), false, false);
    }

    private void prepareForFinish() {
        prepareView(true, true, true, false,
                false, false, false,
                false, false, false);

        TestCache testCache = TestCache.getInstance();
        Student student = pairs.get(0).getStudent();
        InteractUtils.showStuInfo(llStuDetail, student, testCache.getResults().get(student));

        if (systemSetting.isAutoPrint()) {
            InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
        }
        if (systemSetting.isRtUpload() && !TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            String testNo = TestCache.getInstance().getTestNoMap().get(student) + "";
            StudentItem studentItem = TestCache.getInstance().getStudentItemMap().get(student);
            List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
            String scheduleNo = studentItem.getScheduleNo();

            List<UploadResults> uploadResults = new ArrayList<>();
            uploadResults.add(new UploadResults(scheduleNo,
                    TestConfigs.getCurrentItemCode(), student.getStudentCode()
                    , testNo, null, RoundResultBean.beanCope(roundResultList)));
            Logger.i("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    @Override
    protected void handleMessage(Message msg) {

        switch (msg.what) {
            case PullUpManager.STATE_DISCONNECT:
                cbDeviceState.setChecked(false);
                pairs.get(0).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
                break;

            case PullUpManager.STATE_FREE:
                cbDeviceState.setChecked(true);
                pairs.get(0).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                break;

            case UPDATE_SCORE:
                PullUpStateResult result = (PullUpStateResult) msg.obj;
                pairs.get(0).setDeviceResult(result);
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult());
                tvResult.setText(displayResult);
                break;
        }
    }

    @Override
    public void onGetReadyTimerTick(long tick) {
        tickInUI(tick + "");
    }

    void tickInUI(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTimeCount.setText(msg);
            }
        });
    }

    @Override
    public void onGetReadyTimerFinish() {
        tickInUI("开始");
        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), pairs.get(0).getStudent().getLEDStuName(), 5, 0, true, true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvResult.setText("");
                prepareView(true, false, false, false,
                        true, false, true,
                        false, false, false);
            }
        });
    }

    @Override
    public void finish() {
        if (!isConfigurableNow()) {
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
        facade.stopTotally();
    }

    @Override
    public void onDeviceConnectState(int state) {
        handler.sendEmptyMessage(state);
    }

    @Override
    public void onScoreArrived(PullUpStateResult result) {
        if (isConfigurableNow()) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = UPDATE_SCORE;
        msg.obj = result;
        handler.sendMessage(msg);
    }

    private boolean isConfigurableNow() {
        return state == WAIT_CHECK_IN || state == WAIT_BEGIN;
    }

    private void setAdapter() {
        int maxTestNo = TestConfigs.getMaxTestCount(this);
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
//        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        Student student = TestCache.getInstance().getAllStudents().get(0);
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);

        String[] resultArray = new String[maxTestNo];

//        List<String> results = new ArrayList<>(maxTestNo);
        if (roundResults != null) {
            for (RoundResult result : roundResults) {
                resultArray[result.getRoundNo() - 1] = RoundResult.resultStateStr(result.getResultState(), result.getResult());
//                results.add(ResultDisplayUtils.getStrResultForDisplay(result.getResult()));
            }
        }
        resultList.clear();
        resultList.addAll(Arrays.asList(resultArray));
        adapter.setIndexPostion(pairs.get(0).getCurrentRoundNo() - 1);
        adapter.setSelectPosition(pairs.get(0).getCurrentRoundNo() - 1);
        adapter.notifyDataSetChanged();
//        BasePersonTestResultAdapter adapter = new BasePersonTestResultAdapter(results);
//        rvTestResult.setAdapter(adapter);
    }

    public void showPenalizeDialog(int max) {
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(0);
        numberPicker.setValue(pairs.get(0).getPenalty());
        numberPicker.setMaxValue(max);
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.addView(numberPicker, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); //禁止输入

        new AlertDialog.Builder(this).setTitle("请输入判罚值")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int value = -1 * numberPicker.getValue();
                        if (value != pairs.get(0).getPenalty()) {
                            ledManager.showString(systemSetting.getHostId(), "判罚:" + ResultDisplayUtils.getStrResultForDisplay(value), 1, 2, false, false);
                            ledManager.showString(systemSetting.getHostId(),
                                    "最终:" + ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult() + value),
                                    1, 3, false, true);
                        }
                        pairs.get(0).setPenalty(value);
                        Logger.i("判罚：" + value);
                        toastSpeak("判罚成功");
                    }
                })
                .setNegativeButton("返回", null).show();
    }

    public void cancelChangeBad() {
        facade.cancelLinking();
        if (changBadDialog != null) {
            changBadDialog.dismiss();
        }
    }

    public void changeBadDevice() {
        if (!isConfigurableNow()) {
            ToastUtils.showShort("测试中,不允许更换设备");
            return;
        }
        facade.link();
        showChangeBadDialog();
    }

    public void showChangeBadDialog() {
        changBadDialog = new WaitDialog(this);
        changBadDialog.setCanceledOnTouchOutside(false);
        changBadDialog.setCancelable(false);
        changBadDialog.show();
        // 必须在dialog显示出来后再调用
        changBadDialog.setTitle("请重启待连接设备");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelChangeBad();
            }
        });
    }

    @Override
    public void onNoPairResponseArrived() {
        toastSpeak("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void onNewDeviceConnect() {
        cancelChangeBad();
        toastSpeak("设备连接成功");
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                individualCheckFragment.checkQulification(student.getStudentCode(), IndividualCheckFragment.STUDENT_CODE);
                if (student != null) {

                    afrFrameLayout.setVisibility(View.GONE);
                }
//                if (student == null) {
//                    InteractUtils.toastSpeak(PullUpIndividualActivity.this, "该考生不存在");
//                    return;
//                } else {
//                    afrFrameLayout.setVisibility(View.GONE);
//                }
//                final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//                if (studentItem == null) {
//                    InteractUtils.toastSpeak(PullUpIndividualActivity.this, "无此项目");
//                    return;
//                }
//                final List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
//                if (results != null && results.size() >= TestConfigs.getMaxTestCount(PullUpIndividualActivity.this)) {
//                    InteractUtils.toastSpeak(PullUpIndividualActivity.this, "该考生已测试");
//                    return;
//                }
//                onIndividualCheckIn(student, studentItem, results);
            }
        });


    }

    public void showAFR() {
        if (SettingHelper.getSystemSetting().getCheckTool() != 4) {
            ToastUtils.showShort("未选择人脸识别检录功能");
            return;
        }
        if (afrFrameLayout == null) {
            return;
        }

        boolean isGoto = afrFragment.gotoUVCFaceCamera(!afrFragment.isOpenCamera);
        if (isGoto) {
            if (afrFragment.isOpenCamera) {
                afrFrameLayout.setVisibility(View.VISIBLE);
            } else {
                afrFrameLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setRoundNo(Student student, int roundNo) {
        for (StuDevicePair pair : pairs) {
            Student student1 = pair.getStudent();
            if (student1 != null && student1.getStudentCode().equals(student.getStudentCode())) {
                pair.setCurrentRoundNo(roundNo);
            }
        }
    }
}
