package com.feipulai.exam.activity.pushUp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
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
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.person.PenalizeDialog;
import com.feipulai.exam.activity.person.adapter.BaseGroupTestStuAdapter;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.activity.pushUp.check.PushUpCheckActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.adapter.VolleyBallGroupStuAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class PushUpGroupActivity extends BaseTitleActivity
        implements PushUpResiltListener.Listener,
        BaseQuickAdapter.OnItemClickListener, SitPullLinker.SitPullPairListener {

    private static final int UPDATE_SCORE = 0x3;

    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
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
    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestingPairs;
    @BindView(R.id.tv_group_name)
    TextView tvGroupName;
    @BindView(R.id.tv_device_pair)
    TextView tvDevicePair;
    @BindView(R.id.tv_resurvey)
    TextView tvResurvey;
    private PushUpResiltListener facade;
    // ??????  WAIT_BEGIN--->TESTING---->WAIT_CONFIRM--->WAIT_BEGIN
    private static final int WAIT_BEGIN = 0x1;
    private static final int TESTING = 0x2;
    private static final int WAIT_CONFIRM = 0x3;
    protected volatile int state = WAIT_BEGIN;
    private Handler handler = new MyHandler(this);
    private PushUpSetting setting;
    private LEDManager ledManager = new LEDManager();
    private String testDate;
    private SystemSetting systemSetting;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private BaseGroupTestStuAdapter stuPairAdapter;
    private Group group;
    private int intervalCount = 0;
    private WaitDialog changBadDialog;
    private SitPullLinker linker;
    protected final int TARGET_FREQUENCY = SettingHelper.getSystemSetting().getUseChannel();
    private EditResultDialog editResultDialog;
    private List<BaseStuPair> stuPairs;
    private PenalizeDialog penalizeDialog;
    private String[] lastResult;
    private Student lastStudent;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_pushup;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();
        setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);

        group = (Group) TestConfigs.baseGroupMap.get("group");
        String type = "????????????";
        if (group.getGroupType() == Group.MALE) {
            type = "??????";
        } else if (group.getGroupType() == Group.FEMALE) {
            type = "??????";
        }
        tvGroupName.setText(String.format(Locale.CHINA, "%s???%d???", type, group.getGroupNo()));

        TestCache.getInstance().init();
        stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
        pairs = CheckUtils.newPairs(stuPairs.size(), stuPairs);
        LogUtils.operation("??????????????????????????????:" + pairs.toString());
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new BaseGroupTestStuAdapter(stuPairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setTestPosition(0);

        facade = new PushUpResiltListener(SettingHelper.getSystemSetting().getHostId(), setting, this);
        stuPairAdapter.setOnItemClickListener(this);

//        prepareForBegin();
        if (setting.getTestType() == PushUpSetting.WIRED_TYPE) {
            tvDevicePair.setVisibility(View.GONE);
        }
        locationTestStu();
        editResultDialog = new EditResultDialog(this);
        editResultDialog.setListener(new EditResultDialog.OnInputResultListener() {

            @Override
            public void inputResult(String result, int state) {
                pairs.get(position()).getDeviceResult().setResult(ResultDisplayUtils.getDbResultForUnit(Double.valueOf(result)));
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult());
                editResultDialog.dismissDialog();
            }
        });
        penalizeDialog = new PenalizeDialog(this, TestConfigs.getMaxTestCount());
        if (SettingHelper.getSystemSetting().isAgainTest()) {
            tvResurvey.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            ToastUtils.showShort("???????????????????????????????????????");
        }
        if (baseEvent.getTagInt() == EventConfigs.ITEM_SETTING_UPDATE) {
            setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
            if (setting.getTestType() == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() > 1) {
                finish();
                IntentUtil.gotoActivity(PushUpGroupActivity.this, PushUpCheckActivity.class);
            }
            facade.setTimeLimit(setting);
        }
        switch (baseEvent.getTagInt()) {
            case EventConfigs.INSTALL_RESULT:
                RoundResult iRoundResult = (RoundResult) baseEvent.getData();
                String tmp = RoundResult.resultStateStr(iRoundResult.getResultState(), iRoundResult.getResult());
                StuDevicePair pair = pairs.get(stuPairAdapter.getTestPosition());
                BaseStuPair baseStuPair = stuPairs.get(stuPairAdapter.getSaveLayoutSeletePosition());
                String[] timeResult = baseStuPair.getTimeResult();
                timeResult[iRoundResult.getRoundNo() - 1] = tmp;
                baseStuPair.setTimeResult(timeResult);

                stuPairAdapter.notifyDataSetChanged();
                pair.setDeviceResult(new VolleyBallResult());
                pair.setPenalty(0);
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(iRoundResult.getStudentCode(),
                        group.getId() + "");
                TestCache.getInstance().getResults().put(baseStuPair.getStudent(), roundResultList);
                if (TextUtils.equals(pair.getStudent().getStudentCode(), iRoundResult.getStudentCode())) {
                    String displayInLed = "??????:" + tmp;
                    ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);
                    if (systemSetting.isAutoBroadcast()) {
                        TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), pair.getStudent().getSpeakStuName(),
                                tmp));
                    }
                    boolean isAllTest = isAllTest(roundResultList, pair);
                    if (isAllTest) {
                        uploadResults();
                    }

                    dispatch(isAllTest);
                }


                break;
            case EventConfigs.UPDATE_RESULT:
                RoundResult roundResult = (RoundResult) baseEvent.getData();
                pair = pairs.get(stuPairAdapter.getTestPosition());
                tmp = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
                baseStuPair = stuPairs.get(stuPairAdapter.getSaveLayoutSeletePosition());
                timeResult = baseStuPair.getTimeResult();
                timeResult[roundResult.getRoundNo() - 1] = tmp;
                baseStuPair.setTimeResult(timeResult);

                stuPairAdapter.notifyDataSetChanged();

                pair.setDeviceResult(new VolleyBallResult());
                pair.setPenalty(0);
                roundResultList = DBManager.getInstance().queryGroupRound(roundResult.getStudentCode(),
                        group.getId() + "");
                TestCache.getInstance().getResults().put(baseStuPair.getStudent(), roundResultList);

                if (TextUtils.equals(pair.getStudent().getStudentCode(), roundResult.getStudentCode())) {
                    if (systemSetting.isAutoBroadcast()) {
                        TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), pair.getStudent().getSpeakStuName(),
                                tmp));
                    }
                    if (roundResult.getRoundNo() == pair.getCurrentRoundNo()) {
                        tmp = RoundResult.resultStateStr(roundResult.getResultState(), roundResult.getResult());
                        String displayInLed = "??????:" + tmp;
                        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), displayInLed, 1, 1, false, true);

                    }
                    boolean isAllTest = isAllTest(roundResultList, pair);
                    if (isAllTest) {
                        uploadResults();
                    }

                    dispatch(isAllTest);
                }


                break;

        }
    }

    private void initStuResult() {
        for (int i = 0; i < stuPairs.size(); i++) {

            //  ?????????????????? ???????????????????????????????????????
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                    (stuPairs.get(i).getStudent().getStudentCode(), group.getId() + "");

            setStuPairsData(i, roundResultList);
        }
        stuPairAdapter.notifyDataSetChanged();
    }

    /**
     * ??????????????????????????????
     *
     * @param index
     * @param roundResultList
     */
    public void setStuPairsData(int index, List<RoundResult> roundResultList) {
        stuPairs.get(index).setResultState(-99);
//        int testNo = stuPairsList.get(index).getTestNo() == -1 ? setTestCount() : stuPairsList.get(index).getTestNo();
        String[] result = new String[TestConfigs.getMaxTestCount()];

        for (int j = 0; j < roundResultList.size(); j++) {
            result[roundResultList.get(j).getRoundNo() - 1] = RoundResult.resultStateStr(roundResultList.get(j).getResultState(), roundResultList.get(j).getResult());


        }
        stuPairs.get(index).setTimeResult(result);
    }


    private void locationTestStu() {
        if (setting.getGroupMode() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {//??????
            for (int i = 0; i < pairs.size(); i++) {
                StuDevicePair pair = pairs.get(i);
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (pair.getStudent().getStudentCode(), group.getId() + "");
                SystemSetting setting = SettingHelper.getSystemSetting();
                StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(), stuPairs.get(stuPairAdapter.getTestPosition()).getStudent().getStudentCode());
                //???????????????????????????????????????????????????????????????,???????????????????????????
                if (studentItem != null && (setting.isResit() || studentItem.getMakeUpType() == 1) && !stuPairs.get(stuPairAdapter.getTestPosition()).isResit()) {
                    roundResultList.clear();
                }
                if ((roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount())) {
                    isAllTest(roundResultList, pair);
                    switchToPosition(i);
                    return;
                }
            }
        } else {
            for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {
                for (int j = 0; j < pairs.size(); j++) {
                    StuDevicePair pair = pairs.get(j);
                    List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                            (pair.getStudent().getStudentCode(), group.getId() + "");
                    SystemSetting setting = SettingHelper.getSystemSetting();
                    StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(), stuPairs.get(stuPairAdapter.getTestPosition()).getStudent().getStudentCode());
                    //???????????????????????????????????????????????????????????????,???????????????????????????
                    if (studentItem != null && (setting.isResit() || studentItem.getMakeUpType() == 1) && !stuPairs.get(stuPairAdapter.getTestPosition()).isResit()) {
                        roundResultList.clear();
                    }
                    if ((roundResultList.size() < setTestCount())) {
                        isAllTest(roundResultList, pair);
                        switchToPosition(j);
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        facade.setTimeLimit(setting);
    }


    @OnClick({R.id.tv_start_test, R.id.tv_stop_test, R.id.tv_print, R.id.tv_led_setting, R.id.tv_device_pair, R.id.tv_confirm,
            R.id.tv_abandon_test, R.id.tv_resurvey, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal})
    public void onViewClicked(View view) {
        String[] resultArray = stuPairs
                .get(stuPairAdapter.getSaveLayoutSeletePosition()).getTimeResult();

        switch (view.getId()) {

            case R.id.tv_led_setting:
                LogUtils.operation("??????????????????????????????");
                if (isConfigurableNow()) {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                } else {
                    toastSpeak("?????????,??????????????????????????????");
                }
                break;

            case R.id.tv_start_test:
                LogUtils.operation("??????????????????????????????");
                for (StuDevicePair pair : pairs) {
                    for (BaseStuPair stuPair : stuPairs) {
                        if (TextUtils.equals(pair.getStudent().getStudentCode(), stuPair.getStudent().getStudentCode())) {
                            pair.setCurrentRoundNo(stuPair.getRoundNo());
                        }
                    }
                }
                prepareForTesting();
                break;

            case R.id.tv_stop_test:
                LogUtils.operation("??????????????????????????????");
                facade.stopTest();
                SoundPlayUtils.play(12);
//                prepareForConfirmResult();
                if (intervalCount > 0) {
                    //??????????????????
                    ledManager.showString(systemSetting.getHostId(),
                            "????????????:" + ResultDisplayUtils.getStrResultForDisplay(pairs.get(position()).getDeviceResult().getResult() - intervalCount),
                            1, 3, false, true);
                }
                onResultConfirmed();
                break;
            case R.id.tv_print:
                LogUtils.operation("????????????????????????");
                TestCache testCache = TestCache.getInstance();
                InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                        TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                break;

            case R.id.tv_confirm:
                LogUtils.operation("??????????????????????????????");
                onResultConfirmed();
                break;

            case R.id.tv_abandon_test:
                LogUtils.operation("??????????????????????????????");
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("????????????")
                        .setContentText("????????????????????????")
                        .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        facade.abandonTest();
                        prepareForBegin();
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                    }
                }).show();

                break;
            case R.id.tv_device_pair:
                LogUtils.operation("??????????????????????????????");
                changeBadDevice();
                break;
//            case R.id.tv_result:
//
//                if (SettingHelper.getSystemSetting().isInputTest()) {
//                    editResultDialog.showDialog(pairs.get(0).getStudent());
//                }
//                break;
            case R.id.tv_foul:
                penalizeDialog.setGroupId(group.getId());

                penalizeDialog.setData(1, pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).getStudent(), resultArray, lastStudent, lastResult);

                penalizeDialog.showDialog(0);
                break;
            case R.id.tv_inBack:
                penalizeDialog.setGroupId(group.getId());
                penalizeDialog.setData(1, pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).getStudent(), resultArray, lastStudent, lastResult);
                penalizeDialog.showDialog(1);
                break;
            case R.id.tv_abandon:
                penalizeDialog.setGroupId(group.getId());

                penalizeDialog.setData(1, pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).getStudent(), resultArray, lastStudent, lastResult);

                penalizeDialog.showDialog(2);
                break;
            case R.id.tv_normal:
                penalizeDialog.setGroupId(group.getId());
                penalizeDialog.setData(1, pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).getStudent(), resultArray, lastStudent, lastResult);
                penalizeDialog.showDialog(3);
                break;
            case R.id.tv_resurvey:
                if (pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).getStudent() == null) {
                    return;
                }
                Student student = pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).getStudent();
                AgainTestDialog dialog = new AgainTestDialog();
                RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId().toString(), stuPairAdapter.getSaveSeletePosition() + 1);

                if (roundResult == null) {
                    toastSpeak("???????????????????????????????????????");
                    return;
                }
                GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
                List<RoundResult> results = new ArrayList<>();
                results.add(roundResult);
                dialog.setArguments(student, results, groupItem);
                dialog.setOnIndividualCheckInListener(new ResitDialog.onClickQuitListener() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int updateRoundNo) {

                    }

                    @Override
                    public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int updateRoundNo) {
                        LogUtils.operation(student.getStudentCode() + "?????????" + updateRoundNo + "?????????");
                        BaseStuPair pair = stuPairs.get(stuPairAdapter.getSaveLayoutSeletePosition());
                        pair.getTimeResult()[updateRoundNo - 1] = "";

                        //??????????????????
                        pair.setRoundNo(updateRoundNo);
                        pair.setTimeResult(pair.getTimeResult());
                        stuPairAdapter.notifyDataSetChanged();
                        //??????????????????
                        pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).setCurrentRoundNo(updateRoundNo);
                        pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).setDeviceResult(new PullUpStateResult());
                        pairs.get(stuPairAdapter.getSaveLayoutSeletePosition()).setPenalty(0);
                        switchToPosition(stuPairAdapter.getSaveLayoutSeletePosition());
                        List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(),
                                group.getId() + "");
                        TestCache.getInstance().getResults().put(student, roundResultList);

                        prepareForBegin();

                        displayCheckedInLED();
                    }
                });
                dialog.show(getSupportFragmentManager(), "AgainTestDialog");


                break;
        }
    }

    private void onResultConfirmed() {
        List<StuDevicePair> pairList = new ArrayList<>(1);
        pairs.get(position()).getDeviceResult().setResult(pairs.get(position()).getDeviceResult().getResult() - intervalCount);


        pairList.add(pairs.get(position()));
        InteractUtils.saveResults(pairList, testDate);

        int isTestComplete = group.getIsTestComplete();
        if (isTestComplete == Group.NOT_TEST) {
            group.setIsTestComplete(Group.NOT_FINISHED);
            DBManager.getInstance().updateGroup(group);
        }

        TestCache testCache = TestCache.getInstance();
        StuDevicePair pair = pairs.get(position());
        Student student = pair.getStudent();
        List<RoundResult> roundResults = testCache.getResults().get(student);
        Logger.i("?????????" + ResultDisplayUtils.getStrResultForDisplay(roundResults.get(roundResults.size() - 1).getResult()));
        if (systemSetting.isAutoBroadcast()) {

            TtsManager.getInstance().speak(
                    String.format(getString(R.string.speak_result), student.getSpeakStuName(),
                            ResultDisplayUtils.getStrResultForDisplay(roundResults.get(roundResults.size() - 1).getResult())));
        }
        uploadResults();

        boolean isAllTest = isAllTest(roundResults, pairs.get(position()));
//        // List<Student> tmpList = new ArrayList<>(1);
//        // tmpList.add(student);
//        // Map<Student, List<RoundResult>> tmpMap = new HashMap<>(2);
//        // tmpMap.put(student, roundResults);
//        if (isAllTest) {
//            uploadResults();
//            // if (systemSetting.isAutoPrint()) {
//            //     InteractUtils.printResults(group, tmpList, tmpMap,
//            //             TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
//            // }
//        }

        dispatch(isAllTest);
        GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
        if (groupItem != null && groupItem.getExamType() == StudentItem.EXAM_MAKE) {
            int nextPosition = nextPosition();
            if (nextPosition != -1) {
                switchToPosition(nextPosition);
                stuPairAdapter.indexStuTestResult(nextPosition, pairs.get(nextPosition).getCurrentRoundNo());
            }
        }
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        //?????????????????????????????? ????????????????????????????????????
//        if (systemSetting.isGroupCheck() && setting.getGroupMode() == TestConfigs.GROUP_PATTERN_LOOP) {
//            finish();
//        }
//        if (systemSetting.isGroupCheck() && setting.getGroupMode() == TestConfigs.GROUP_PATTERN_SUCCESIVE && TestConfigs.getMaxTestCount() == 1) {
//            finish();
//        }
    }

    private void switchToPosition(int position) {
        int oldPosition = position();
        stuPairAdapter.setTestPosition(position);
        stuPairAdapter.notifyItemChanged(oldPosition);
        stuPairAdapter.notifyItemChanged(position);
        stuPairAdapter.indexStuTestResult(position, pairs.get(position).getCurrentRoundNo() - 1);

        prepareForBegin();
    }

    private void dispatch(boolean isAllTest) {
        int groupMode = setting.getGroupMode();
        int nextPosition;
        if (groupMode == TestConfigs.GROUP_PATTERN_SUCCESIVE && !isAllTest) {
            nextPosition = position();
        } else {
            nextPosition = nextPosition();
            if (nextPosition == -1) {
                ToastUtils.showShort("????????????????????????");
                group.setIsTestComplete(Group.FINISHED);
                DBManager.getInstance().updateGroup(group);
                if (systemSetting.isAutoPrint()) {
                    TestCache testCache = TestCache.getInstance();

                    if (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 || SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP) {
                        InteractUtils.printA4Result(this, group);
                    } else {
                        InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                                TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                    }
                }
                state=WAIT_BEGIN;
                stuPairAdapter.notifyDataSetChanged();
                prepareView(true,
                        false,
                        false, false, false, false,
                        false);
                return;

            }
        }
        switchToPosition(nextPosition);
    }

    private int nextPosition() {
        for (int i = position() + 1; i < pairs.size() + position(); i++) {
            int j = i % pairs.size();
            stuPairAdapter.setTestPosition(j);
            StuDevicePair pair = pairs.get(j);
            Student student = pair.getStudent();
            List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
            if (!isAllTest(roundResults, pair)) {
                return j;
            }
        }
        return -1;// ???????????????????????????
    }

    private boolean isAllTest(List<RoundResult> roundResults, StuDevicePair pair) {
        if (roundResults == null || roundResults.size() == 0) {
            pair.setCurrentRoundNo(1);
            return false;
        }
        boolean fullSkip = fullSkip(roundResults, pair.getStudent());
        return !hasRemains(roundResults)|| fullSkip ;
    }

    private boolean fullSkip(List<RoundResult> roundResults, Student student) {
        if (roundResults == null || roundResults.size() == 0) {
            return false;
        }
        boolean fullSkip = setting.isFullSkip();
        if (fullSkip) {
            if (student.getSex() == Student.MALE) {
                for (RoundResult result : roundResults) {
                    if (result.getResult() >= setting.getMaleFullScore()) {
                        return true;
                    }
                }
            } else {
                for (RoundResult result : roundResults) {
                    if (result.getResult() >= setting.getFemaleFullScore()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void uploadResults() {
        if (systemSetting.isRtUpload()) {
            Student student = pairs.get(position()).getStudent();
            if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                return;
            }
            List<UploadResults> uploadResults = new ArrayList<>();
            String groupNo;
            String scheduleNo;
            String testNo;
            List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
            groupNo = group.getGroupNo() + "";
            scheduleNo = group.getScheduleNo();
            testNo = "1";
            UploadResults uploadResult = new UploadResults(scheduleNo,
                    TestConfigs.getCurrentItemCode(), student.getStudentCode()
                    , testNo, group, RoundResultBean.beanCope(roundResultList, group));
            uploadResults.add(uploadResult);
            Logger.i("??????????????????:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    private boolean hasRemains(List<RoundResult> roundResults) {

        String[] resultArray = new String[TestConfigs.getMaxTestCount()];
        if (roundResults != null) {
            for (RoundResult result : roundResults) {
                resultArray[result.getRoundNo() - 1] = RoundResult.resultStateStr(result.getResultState(), result.getResult());
            }
        }
        stuPairs.get(stuPairAdapter.getTestPosition()).setTimeResult(resultArray);


        boolean isAllTest = true;
        for (int i = 0; i < resultArray.length; i++) {
            if (TextUtils.isEmpty(resultArray[i])) {
                isAllTest = false;
                pairs.get(stuPairAdapter.getTestPosition()).setCurrentRoundNo(i + 1);
                break;
            }
        }
        if (isAllTest) {
            return false;
        }


        return roundResults.size() < TestConfigs.getMaxTestCount(this);
    }

    private void prepareView(boolean tvPrintEnable, boolean tvStartTestEnable, boolean tvAbandonTestEnable, boolean tvConfirmEnable,
                             boolean tvTimeCountEnable, boolean tvStopTestEnable, boolean tvPunishEnable) {
        tvPrint.setVisibility(tvPrintEnable ? View.VISIBLE : View.GONE);

        tvStartTest.setVisibility(tvStartTestEnable ? View.VISIBLE : View.GONE);
        tvAbandonTest.setVisibility(tvAbandonTestEnable ? View.VISIBLE : View.GONE);
        tvConfirm.setVisibility(tvConfirmEnable ? View.VISIBLE : View.GONE);

        tvTimeCount.setVisibility(tvTimeCountEnable ? View.VISIBLE : View.GONE);
        tvStopTest.setVisibility(tvStopTestEnable ? View.VISIBLE : View.GONE);
        tvPunish.setVisibility(tvPunishEnable ? View.VISIBLE : View.GONE);

    }

    protected void displayCheckedInLED() {
        Student student = TestCache.getInstance().getAllStudents().get(position());
        List<RoundResult> results = TestCache.getInstance().getResults().get(student);

        RoundResult lastResult = null;
        if (results != null && results.size() > 0) {
            lastResult = results.get(results.size() - 1);
        }
        int hostId = systemSetting.getHostId();
        ledManager.showString(hostId, pairs.get(position()).getStudent().getLEDStuName(), 5, 0, true, lastResult == null);
        if (lastResult != null) {
            String displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
            ledManager.showString(hostId, "??????:" + displayResult, 2, 3, false, true);
        }
    }

    private void prepareForBegin() {
        Student student = pairs.get(position()).getStudent();

        List<RoundResult> results = TestCache.getInstance().getResults().get(student);
        LogUtils.operation("???????????????????????????:" + student.toString() + "---?????????????????? = " + results.toString());

        prepareView(true,
                results == null || results.size() < TestConfigs.getMaxTestCount(this),
                false, false, false, false,
                false);

        displayCheckedInLED();

        rvTestingPairs.smoothScrollToPosition(position());
        state = WAIT_BEGIN;
    }

    private void prepareForTesting() {
        if (pairs.get(position()).getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT
                && !SettingHelper.getSystemSetting().isInputTest()) {
            ToastUtils.showShort("???????????????,??????????????????");
            return;
        }
        LogUtils.operation("???????????????????????????:stuCode=" + pairs.get(position()).getStudent().getStudentCode() + ",????????????");
        prepareView(false, false,
                true, false, true, false,
                false);

        testDate = System.currentTimeMillis() + "";
        facade.startTest(0);
        state = TESTING;
    }

    private void prepareForConfirmResult() {
        state = WAIT_CONFIRM;
        prepareView(false, false,
                false, true, false, false,
                false);
    }

    @Override
    protected void handleMessage(Message msg) {

        switch (msg.what) {
            case SitPushUpManager.STATE_DISCONNECT:
                LogUtils.all("???????????????????????????...");
                cbDeviceState.setChecked(false);
                pairs.get(position()).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
                break;

            case SitPushUpManager.STATE_FREE:
                LogUtils.all("????????????????????????...");
                cbDeviceState.setChecked(true);
                pairs.get(position()).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                break;

            case UPDATE_SCORE:
                LogUtils.all("??????????????????????????????...");
                SitPushUpStateResult result = (SitPushUpStateResult) msg.obj;
                intervalCount = msg.arg1;
                pairs.get(position()).setDeviceResult(result);
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(position()).getDeviceResult().getResult());
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
        // tickInUI("??????");
        // Log.i("james", "onGetReadyTimerFinish");
        onScoreArrived(new SitPushUpStateResult(), 0);
        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), pairs.get(position()).getStudent().getLEDStuName(), 5, 0, true, true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean noTimeLimit = setting.getTestTime() == VolleyBallSetting.NO_TIME_LIMIT;
                prepareView(false, false,
                        true, false, !noTimeLimit, noTimeLimit,
                        false);
            }
        });
    }

    @Override
    public void finish() {
        LogUtils.life("PushUpGroupActivity finish");
        if (!isConfigurableNow()) {
            ToastUtils.showShort("?????????,???????????????????????????");
            return;
        }
        super.finish();
        facade.stopTotally();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
    }

    @Override
    public void onTestingTimerTick(long tick) {
        tickInUI(DateUtil.formatTime(tick * 1000, "mm:ss"));
    }

    @Override
    public void onTestingTimerFinish() {
        tickInUI("??????");


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
        /*facade.stopTest();
        SoundPlayUtils.play(12);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                prepareForConfirmResult();
            }
        });*/
    }

    private boolean isConfigurableNow() {
        return state == WAIT_BEGIN;
    }


    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        if (isConfigurableNow()) {
            LogUtils.operation("??????????????????????????????:" + pairs.get(i).getStudent().toString());

            List<RoundResult> results = TestCache.getInstance().getResults().get(stuPairs.get(i).getStudent());
            stuPairAdapter.setTestPosition(i);
            hasRemains(results);
            stuPairAdapter.indexStuTestResult(i, pairs.get(i).getCurrentRoundNo() - 1);

            stuPairAdapter.notifyDataSetChanged();
            prepareForBegin();
        } else {
            ToastUtils.showShort("?????????,??????????????????");
        }
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "??????"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        if (setting.getTestType() == 1) {
            return builder.setTitle(title).addRightText("????????????", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeBadDevice();
                }
            });
        }
        return builder.setTitle(title);
    }

    private int position() {
        return stuPairAdapter.getTestPosition();
    }

    @Override
    public void onNoPairResponseArrived() {
        toastSpeak("?????????????????????,????????????,?????????");
    }

    @Override
    public void onNewDeviceConnect() {
        cancelChangeBad();
        changBadDialog.dismiss();
    }

    @Override
    public void setFrequency(int deviceId, int originFrequency, int targetFrequency) {
        facade.deviceManager.setFrequency(SettingHelper.getSystemSetting().getUseChannel(),
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
        // ?????????dialog????????????????????????
        changBadDialog.setTitle("????????????????????????");
        changBadDialog.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelChangeBad();
                changBadDialog.dismiss();
            }
        });
    }

    public int setTestCount() {
//        SystemSetting setting = SettingHelper.getSystemSetting();
//        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),stuPairs.get(position()).getStudent().getStudentCode());
//        if (setting.isResit() || studentItem.getMakeUpType() == 1){
//            return stuPairs.get(position()).getTestNo();
//        }
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return TestConfigs.getMaxTestCount();
        }
    }
}
