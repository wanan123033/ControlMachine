package com.feipulai.exam.activity.pushUp;

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
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.serial.beans.VolleyBallResult;
import com.feipulai.device.sitpullup.SitPullLinker;
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
import com.feipulai.exam.activity.pushUp.check.PushUpCheckActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
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
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PushUpIndividualActivity extends BaseTitleActivity
        implements PushUpResiltListener.Listener,
        IndividualCheckFragment.OnIndividualCheckInListener, SitPullLinker.SitPullPairListener, BaseAFRFragment.onAFRCompareListener {

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
    @BindView(R.id.tv_device_pair)
    TextView tvDevicePair;
    @BindView(R.id.tv_resurvey)
    TextView tvResurvey;

    private PushUpResiltListener facade;
    private IndividualCheckFragment individualCheckFragment;
    // 状态  WAIT_CHECK_IN--->WAIT_BEGIN--->TESTING---->WAIT_CONFIRM--->WAIT_CHECK_IN
    private static final int WAIT_CHECK_IN = 0x0;
    private static final int WAIT_BEGIN = 0x1;
    private static final int TESTING = 0x2;
    private static final int WAIT_CONFIRM = 0x3;
    protected volatile int state = WAIT_CHECK_IN;
    private Handler handler = new MyHandler(this);
    private PushUpSetting setting;
    private LEDManager ledManager = new LEDManager();
    private String testDate;
    private SystemSetting systemSetting;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private int intervalCount = 0;
    private WaitDialog changBadDialog;
    private SitPullLinker linker;
    protected final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
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
        return R.layout.activity_individual_pushup;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();
        setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
        lastResult = new String[setting.getTestNo()];
        TestCache.getInstance().clear();

        StuDevicePair pair = new StuDevicePair();
        BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_DISCONNECT);
        pair.setBaseDevice(deviceState);
        pair.setDeviceResult(new VolleyBallResult());
        pairs.add(pair);

        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setResultView(lvResults);
        individualCheckFragment.setOnIndividualCheckInListener(this);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);

        facade = new PushUpResiltListener(SettingHelper.getSystemSetting().getHostId(), setting, this);
        ledManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        prepareForCheckIn();

        if (setting.getTestType() == PushUpSetting.WIRED_TYPE) {
            tvDevicePair.setVisibility(View.GONE);
        }
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
                tvResult.setText(displayResult + (intervalCount == 0 ? "" : "\n超时：" + intervalCount + "个"));
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
                    startActivity(new Intent(PushUpIndividualActivity.this, LEDSettingActivity.class));
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

    @Override
    protected void onRestart() {
        super.onRestart();
        facade.setTimeLimit(setting);
    }

    private void startProjectSetting() {
        if (isConfigurableNow()) {
            IntentUtil.gotoActivityForResult(PushUpIndividualActivity.this, PushUpSettingActivity.class, 1);
        } else {
            toastSpeak("测试中,不允许修改设置");
        }
    }


    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        if (student != null)
            LogUtils.all("俯卧撑检入到学生:" + student.toString());
        if (studentItem != null)
            LogUtils.all("俯卧撑检入到学生StudentItem:" + studentItem.toString());
        if (results != null)
            LogUtils.all("俯卧撑检入到学生成绩:" + results.size() + "----" + results.toString());
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

        pairs.get(0).setDeviceResult(new VolleyBallResult());
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
        if (baseEvent.getTagInt() == EventConfigs.ITEM_SETTING_UPDATE) {
            setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
            if (setting.getTestType() == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() > 1) {
                finish();
                IntentUtil.gotoActivity(PushUpIndividualActivity.this, PushUpCheckActivity.class);
            }
            facade.setTimeLimit(setting);
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
                pairs.get(0).setDeviceResult(new VolleyBallResult());
                pairs.get(0).setPenalty(0);
                List<RoundResult> roundResultList = DBManager.getInstance().queryResultsByStuItem(studentItem);
                TestCache.getInstance().getResults().put(pairs.get(0).getStudent(), roundResultList);
                if (shouldContinue(iRoundResult.getResult())) {
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
                    pairs.get(0).setDeviceResult(new VolleyBallResult());
                    pairs.get(0).setPenalty(0);
                    roundResultList = DBManager.getInstance().queryResultsByStuItem(studentItem);
                    TestCache.getInstance().getResults().put(pairs.get(0).getStudent(), roundResultList);
                    if (roundResult.getRoundNo() == pairs.get(0).getCurrentRoundNo()) {
                        tmp = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
                        String displayInLed = "成绩:" + tmp;
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);

                    }
                    if (shouldContinue(roundResult.getResult())) {
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
            R.id.tv_punish, R.id.tv_abandon_test, R.id.tv_finish_test, R.id.tv_exit_test, R.id.tv_device_pair, R.id.img_AFR
            , R.id.tv_resurvey, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal})
    public void onViewClicked(View view) {
        String[] resultArray = new String[TestConfigs.getMaxTestCount()];
        resultList.toArray(resultArray);
        switch (view.getId()) {

            case R.id.tv_led_setting:
                LogUtils.operation("俯卧撑点击了外接屏幕");
                if (isConfigurableNow()) {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                } else {
                    toastSpeak("测试中,不能进行外接屏幕设置");
                }
                break;

            case R.id.tv_start_test:
                LogUtils.operation("俯卧撑点击了开始测试");
                prepareForTesting();
                break;

            case R.id.tv_stop_test:
                LogUtils.operation("俯卧撑点击了结束测试");
                facade.stopTest();
                SoundPlayUtils.play(12);
                prepareForConfirmResult();
                if (intervalCount > 0) {
                    //显示最终成绩
                    ledManager.showString(systemSetting.getHostId(),
                            "最终成绩:" + ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult() - intervalCount),
                            1, 3, false, true);
                }
                break;

            case R.id.tv_print:
                LogUtils.operation("俯卧撑点击了打印");
                TestCache testCache = TestCache.getInstance();
                InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                        TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                break;

            case R.id.tv_confirm:
                LogUtils.operation("俯卧撑点击了确认");
                tvResult.setText("");
                pairs.get(0).getDeviceResult().setResult(pairs.get(0).getDeviceResult().getResult() - intervalCount);

                InteractUtils.saveResults(pairs, testDate);
                onResultConfirmed();
                break;

//            case R.id.tv_punish:
//                showPenalizeDialog(pairs.get(0).getDeviceResult().getResult());
//                break;

            case R.id.tv_abandon_test:
                abandon();
                break;

            case R.id.tv_finish_test:
                LogUtils.operation("俯卧撑点击了跳过测试");
                prepareForCheckIn();
                break;

            case R.id.tv_exit_test:
                LogUtils.operation("俯卧撑点击了退出测试");
                lastStudent = pairs.get(0).getStudent();
                resultList.toArray(lastResult);
                prepareForCheckIn();
                break;
            case R.id.tv_device_pair:
                LogUtils.operation("俯卧撑点击了配对");
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
                        pairs.get(0).setDeviceResult(new VolleyBallResult());
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

    private void abandon() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("温馨提示");
        builder.setMessage("确认放弃测试吗?");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogUtils.operation("俯卧撑点击了放弃测试");
                facade.abandonTest();
                state = WAIT_BEGIN;
                prepareForBegin();
            }
        });
        builder.setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    private void onResultConfirmed() {
        StuDevicePair pair = pairs.get(0);
        int result = pair.getDeviceResult().getResult() + pair.getPenalty();
        LogUtils.operation("俯卧撑成绩确认：" + result);
        if (systemSetting.isAutoBroadcast()) {
            TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), pair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(result)));
        }
        uploadResult(pairs.get(0).getStudent());
        // 是否需要进行下一次测试
        if (shouldContinue(result)) {
            adapter.setIndexPostion(pairs.get(0).getCurrentRoundNo() - 1);
            adapter.setSelectPosition(pairs.get(0).getCurrentRoundNo() - 1);
            prepareForBegin();
        } else {
            prepareForFinish();
        }
        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(), pair.getStudent().getStudentCode());
        if (studentItem.getExamType() == 2) {
            prepareForFinish();
        }
    }

    private boolean shouldContinue(int result) {
        int maxTestNo = TestConfigs.getMaxTestCount(this);
        TestCache testCache = TestCache.getInstance();
        Student student = testCache.getAllStudents().get(0);
        boolean hasRemain = testCache.getResults().get(student).size() < maxTestNo;// 测试次数未完成

        //更新列表
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        String[] resultArray = new String[maxTestNo];
        if (roundResults != null) {
            for (RoundResult roundResult : roundResults) {
                resultArray[roundResult.getRoundNo() - 1] = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
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


        boolean fullSkip = setting.isFullSkip();
        if (fullSkip) {
            if (student.getSex() == Student.MALE) {
                fullSkip = result >= setting.getMaleFullScore();
            } else {
                fullSkip = result >= setting.getFemaleFullScore();
            }
        }

        if (hasRemain && !fullSkip) {
            LogUtils.operation("俯卧撑当前考生进入下一轮测试:stuCode = " + student.getStudentCode());
        }
        return hasRemain && !fullSkip;
    }

    private void prepareView(boolean rvResultEnable, boolean tvFinishTestEnable, boolean tvPrintEnable,
                             boolean tvStartTestEnable, boolean tvAbandonTestEnable, boolean tvConfirmEnable,
                             boolean tvTimeCountEnable, boolean tvStopTestEnable, boolean tvPunishEnable, boolean tvExitTestEnable) {
        rvTestResult.setVisibility(rvResultEnable ? View.VISIBLE : View.INVISIBLE);
        tvFinishTest.setVisibility(tvFinishTestEnable ? View.VISIBLE : View.GONE);
        tvPrint.setVisibility(tvPrintEnable ? View.VISIBLE : View.GONE);

        tvStartTest.setVisibility(tvStartTestEnable ? View.VISIBLE : View.GONE);
        tvAbandonTest.setVisibility(tvAbandonTestEnable ? View.VISIBLE : View.GONE);
        tvConfirm.setVisibility(tvConfirmEnable ? View.VISIBLE : View.GONE);

        tvTimeCount.setVisibility(tvTimeCountEnable ? View.VISIBLE : View.GONE);
        tvStopTest.setVisibility(tvStopTestEnable ? View.VISIBLE : View.GONE);
        tvPunish.setVisibility(tvPunishEnable ? View.VISIBLE : View.GONE);
        tvExitTest.setVisibility(tvExitTestEnable ? View.VISIBLE : View.GONE);

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
                false, false, false, false,
                false, false);

        state = WAIT_CHECK_IN;
        pairs.get(0).setCurrentRoundNo(0);
    }

    private void prepareForBegin() {
        TestCache testCache = TestCache.getInstance();
        Student student = pairs.get(0).getStudent();
        LogUtils.operation("俯卧撑等待开始测试:" + student.toString());
        InteractUtils.showStuInfo(llStuDetail, student, testCache.getResults().get(student));

        tvResult.setText(student.getStudentName());

        prepareView(true, false, true, true,
                false, false, false, false,
                false, true);

        state = WAIT_BEGIN;
        adapter.setIndexPostion(pairs.get(0).getCurrentRoundNo() - 1);
        adapter.setSelectPosition(pairs.get(0).getCurrentRoundNo() - 1);
    }

    private void prepareForTesting() {
        LogUtils.operation("俯卧撑开始测试");
        if (pairs.get(0).getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT
                && !SettingHelper.getSystemSetting().isInputTest()) {
            toastSpeak("设备未连接,不能开始测试");
            return;
        }

        prepareView(true, false, false, false,
                true, false, true, false,
                false, false);

        tvResult.setText("准备");
        testDate = System.currentTimeMillis() + "";
        facade.startTest(0);
        state = TESTING;
    }

    private void prepareForConfirmResult() {
        LogUtils.operation("俯卧撑确认成绩");
        state = WAIT_CONFIRM;
        prepareView(true, false, false, false,
                false, true, false, false,
                false, false);
    }

    private void prepareForFinish() {
        prepareView(true, true, true, false,
                false, false, false, false,
                false, false);

        TestCache testCache = TestCache.getInstance();
        Student student = pairs.get(0).getStudent();
        InteractUtils.showStuInfo(llStuDetail, student, testCache.getResults().get(student));

        if (systemSetting.isAutoPrint()) {
            InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
        }

    }

    private void uploadResult(Student student) {
        if (systemSetting.isRtUpload() && !TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            String testNo = TestCache.getInstance().getTestNoMap().get(student) + "";
            StudentItem studentItem = TestCache.getInstance().getStudentItemMap().get(student);
            List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
            String scheduleNo = studentItem.getScheduleNo();

            List<UploadResults> uploadResults = new ArrayList<>();
            uploadResults.add(new UploadResults(scheduleNo,
                    TestConfigs.getCurrentItemCode(), student.getStudentCode()
                    , testNo, null, RoundResultBean.beanCope(roundResultList)));
//            LogUtils.operation("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    @Override
    protected void handleMessage(Message msg) {

        switch (msg.what) {
            case SitPushUpManager.STATE_DISCONNECT:
                LogUtils.all("俯卧撑设备已断开...");
                cbDeviceState.setChecked(false);
                pairs.get(0).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
                break;

            case SitPushUpManager.STATE_FREE:
                LogUtils.all("俯卧撑设备空闲中...");
                cbDeviceState.setChecked(true);
                pairs.get(0).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                break;

            case UPDATE_SCORE:
                LogUtils.all("俯卧撑设备更新成绩中...");
                SitPushUpStateResult result = (SitPushUpStateResult) msg.obj;
                intervalCount = msg.arg1;
                pairs.get(0).setDeviceResult(result);
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult());
                tvResult.setText(displayResult + (intervalCount == 0 ? "" : "\n超时：" + intervalCount + "个"));
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
                boolean noTimeLimit = setting.getTestTime() == VolleyBallSetting.NO_TIME_LIMIT;
                prepareView(true, false, false, false,
                        true, false, !noTimeLimit, noTimeLimit,
                        false, false);
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
    public void onTestingTimerTick(long tick) {
        tickInUI(DateUtil.formatTime(tick * 1000, "mm:ss"));
    }

    @Override
    public void onTestingTimerFinish() {
        tickInUI("结束");
        state = WAIT_CONFIRM;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                prepareForConfirmResult();
            }
        });
    }

    @Override
    public void onDeviceConnectState(int state) {
        handler.sendEmptyMessage(state);
    }

    @Override
    public void onScoreArrived(SitPushUpStateResult result, int intervalCount) {
        if (isConfigurableNow()) {
            return;
        }
        if (state == WAIT_CONFIRM) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = UPDATE_SCORE;
        msg.obj = result;
        msg.arg1 = intervalCount;
        handler.sendMessage(msg);
    }

    @Override
    public void onTimeOut() {

    }

    private boolean isConfigurableNow() {
        return state == WAIT_CHECK_IN || state == WAIT_BEGIN;
    }

    private void setAdapter() {
        int maxTestNo = TestConfigs.getMaxTestCount(this);
//        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
//        rvTestResult.setLayoutManager(layoutManager);
        Student student = TestCache.getInstance().getAllStudents().get(0);
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
//        List<String> results = new ArrayList<>(maxTestNo);
//        if (roundResults != null) {
//            for (RoundResult result : roundResults) {
//                results.add(ResultDisplayUtils.getStrResultForDisplay(result.getResult()));
//            }
//        }
//        BasePersonTestResultAdapter adapter = new BasePersonTestResultAdapter(results);
//        rvTestResult.setAdapter(adapter);



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

    }


    @Override
    public void onNoPairResponseArrived() {
        toastSpeak("未收到子机回复,设置失败,请重试");
    }

    @Override
    public void onNewDeviceConnect() {
        cancelChangeBad();
        changBadDialog.dismiss();
    }


    @Override
    public void setFrequency(int deviceId, int originFrequency, int targetFrequency) {
        facade.deviceManager.setFrequency(systemSetting.getUseChannel(),
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());

    }

    public void cancelChangeBad() {
        facade.mLinking = false;
        if (linker != null) {
            linker.cancelPair();
        }
        facade.stopTotally();
        facade = null;
        facade = new PushUpResiltListener(SettingHelper.getSystemSetting().getHostId(), setting, this);

    }

    public void changeBadDevice() {
        if (linker == null) {
            linker = new SitPullLinker(TestConfigs.sCurrentItem.getMachineCode(), TARGET_FREQUENCY, this);
            facade.setLinker(linker);
        } else {
            facade.setLinker(linker);
        }
        facade.stopTotally();
        facade.mLinking = true;
        linker.startPair(1);
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
                changBadDialog.dismiss();
            }
        });
    }

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// 提交更改
    }

    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
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
//                    InteractUtils.toastSpeak(PushUpIndividualActivity.this, "该考生不存在");
//                    return;
//                } else {
//                    afrFrameLayout.setVisibility(View.GONE);
//                }
//                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//                if (studentItem == null) {
//                    InteractUtils.toastSpeak(PushUpIndividualActivity.this, "无此项目");
//                    return;
//                }
//                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
//                if (results != null && results.size() >= TestConfigs.getMaxTestCount(PushUpIndividualActivity.this)) {
//                    InteractUtils.toastSpeak(PushUpIndividualActivity.this, "该考生已测试");
//                    return;
//                }
//                LogUtils.operation("检入考生：" + student.toString());
//                // 可以直接检录
//                onIndividualCheckIn(student, studentItem, results);
            }
        });
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
