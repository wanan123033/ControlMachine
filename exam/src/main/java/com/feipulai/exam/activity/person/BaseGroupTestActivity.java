package com.feipulai.exam.activity.person;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.adapter.BaseGroupTestStuAdapter;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
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
import com.feipulai.exam.service.UploadService;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.EditResultDialog;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ??????
 * Created by zzs on 2018/11/21
 * ??????????????????????????????????????????   ????????????:??????
 */
public abstract class BaseGroupTestActivity extends BaseCheckActivity {

    @BindView(R.id.txt_group_name)
    TextView txtGroupName;
    @BindView(R.id.rv_test_stu)
    RecyclerView rvTestStu;
    @BindView(R.id.txt_start_test)
    TextView tvStartTest;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.txt_test_result)
    TextView txtStuResult;
    //    @BindView(R.id.ll_state)
//    public LinearLayout llState;
    @BindView(R.id.cb_device_state)
    public CheckBox cbDeviceState;
    @BindView(R.id.tv_base_height)
    public TextView tvBaseHeight;
    @BindView(R.id.txt_stu_skip)
    TextView txtStuSkip;
    //    @BindView(R.id.tv_penalizeFoul)
//    TextView tv_penalizeFoul;
    //    @BindView(R.id.txt_stu_fault)
//    TextView txtStuFault;
    private List<BaseStuPair> stuPairsList;
    private BaseGroupTestStuAdapter stuAdapter;
    //    private List<String> resultList = new ArrayList<>();
//    private BasePersonTestResultAdapter testResultAdapter;
    @BindView(R.id.tv_foul)
    TextView tvFoul;
    @BindView(R.id.tv_inBack)
    TextView tvInBack;
    @BindView(R.id.tv_abandon)
    TextView tvAbandon;
    @BindView(R.id.tv_normal)
    TextView tvNormal;
    @BindView(R.id.tv_resurvey)
    TextView tvResurvey;
    /**
     * ?????????????????????
     */
    private int roundNo = 1;
    /**
     * ??????????????????
     */
    private boolean isStop = true;
    private LEDManager mLEDManager;
    private Group group;

    private LedHandler ledHandler = new LedHandler(this);
    private int testType = 0;//0?????? 1??????
    private volatile boolean lockFoul;//??????????????????????????????????????????
    /**
     * ????????????  0????????? 1??????
     */
    public int runUp;
    public int baseHeight;
    //    private boolean isFault;
    private EditResultDialog editResultDialog;
    private PenalizeDialog penalizeDialog;
    private String[] lastResult;
    private Student lastStudent;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_group_test;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.life("BaseGroupTestActivity onCreate");
        ButterKnife.bind(this);
        PrinterManager.getInstance().init();
        group = (Group) TestConfigs.baseGroupMap.get("group");
        initData();

        mLEDManager = new LEDManager();

        rvTestStu.setLayoutManager(new LinearLayoutManager(this));
        stuPairsList = new ArrayList<>();
        stuAdapter = new BaseGroupTestStuAdapter(stuPairsList);
        rvTestStu.setAdapter(stuAdapter);
//        rvTestStu.setFocusableInTouchMode(false);
//        rvTestStu.setLayoutFrozen(true);
        StringBuffer sbName = new StringBuffer();
        if (group != null) {
            sbName.append(group.getGroupType() == Group.MALE ? "??????" :
                    (group.getGroupType() == Group.FEMALE ? "??????" : "????????????"));
            sbName.append(group.getSortName() + String.format("???%1$d???", group.getGroupNo()));
            txtGroupName.setText(sbName);
        }
//        stuAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
//            @Override
//            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
//                if (stuPairsList.get(position).getBaseDevice().getState() == BaseDeviceState.STATE_ERROR) {
//                    startTest(stuPairsList.get(position));
//                }
//            }
//        });
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            setOpenDevice(true);
        } else {
            setOpenDevice(false);
        }


        GridLayoutManager layoutManager = new GridLayoutManager(this, TestConfigs.getMaxTestCount());
        rvTestResult.setLayoutManager(layoutManager);
        String result[] = new String[TestConfigs.getMaxTestCount()];

//        //???????????????
//        resultList.addAll(Arrays.asList(result));
//        testResultAdapter = new BasePersonTestResultAdapter(resultList);
//        //???RecyclerView???????????????
//        rvTestResult.setAdapter(testResultAdapter);

        getTestStudent(group);
//        setStuShowLed(stuAdapter.getTestPosition() != -1 ? stuPairsList.get(stuAdapter.getTestPosition()) : null);
        if (stuAdapter.getTestPosition() != -1) {
            setShowLed(stuPairsList.get(stuAdapter.getTestPosition()));
        }

//        tv_penalizeFoul.setVisibility(isShowPenalizeFoul());
        editResultDialog = new EditResultDialog(this);
        editResultDialog.setListener(new EditResultDialog.OnInputResultListener() {

            @Override
            public void inputResult(String result, int state) {
                getTestPair().setTestTime(System.currentTimeMillis() + "");
                getTestPair().setResultState(state);
                getTestPair().setResult(ResultDisplayUtils.getDbResultForUnit(Double.valueOf(result)));
                doTestEnd(getTestPair().getBaseDevice(), getTestPair());
                editResultDialog.dismissDialog();
            }
        });
        penalizeDialog = new PenalizeDialog(this, setTestCount());
        lastResult = new String[setTestCount()];

        if (SettingHelper.getSystemSetting().isAgainTest()) {
            tvResurvey.setEnabled(true);
            tvResurvey.setVisibility(View.VISIBLE);
        } else {
            tvResurvey.setVisibility(View.GONE);
        }
    }

    @Override
    public void setRoundNo(Student student, int roundNo) {
        for (BaseStuPair stuPair : stuPairsList) {
            if (stuPair.getStudent() != null && stuPair.getStudent().getStudentCode().equals(student.getStudentCode())) {
                stuPair.setRoundNo(roundNo);
                break;
            }
        }
    }

    protected abstract int isShowPenalizeFoul();

    public BaseStuPair getTestPair() {
        if (stuAdapter == null || stuAdapter.getTestPosition() == -1) {
            return null;
        }
        return stuPairsList.get(stuAdapter.getTestPosition());
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title);
    }

    @Override
    public void onCheckIn(Student student) {
        Logger.i("onCheckIn====>" + student.toString());
        if (student == null) {
            toastSpeak("??????????????????");
            return;
        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        if (studentItem == null) {
            toastSpeak("????????????");
            return;
        }
        //????????????????????????
        if (SettingHelper.getSystemSetting().isIdentityMark() && stuPairsList.size() > 0) {
            //???????????????????????????
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
            //???????????????????????????????????????
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, student.getStudentCode());
            int testNo = stuPairsList.get(stuAdapter.getTestPosition()).getTestNo() == -1 ? setTestCount() : stuPairsList.get(stuAdapter.getTestPosition()).getTestNo();
            if ((roundResultList.size() == 0 || roundResultList.size() < testNo) && groupItem != null) {
                isStop = false;
                roundNo = roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                gotoTest(student);
                groupItem.setIdentityMark(1);
                DBManager.getInstance().updateStudentGroupItem(groupItem);
            } else if (groupItem == null) {//?????????
                toastSpeak(student.getSpeakStuName() + "????????????????????????????????????????????????",
                        student.getStudentName() + "????????????????????????????????????????????????");
            } else if (roundResultList.size() > 0) {
                SystemSetting setting = SettingHelper.getSystemSetting();
                if (setting.isResit()) {
                    for (BaseStuPair pair : stuPairsList) {
                        if (pair.getStudent().getStudentCode().equals(student.getStudentCode())) {
                            isStop = false;
                            roundNo = pair.getRoundNo();

                            gotoTest(student);
                            groupItem.setIdentityMark(1);
                            DBManager.getInstance().updateStudentGroupItem(groupItem);
                        }
                    }
                } else {
                    toastSpeak(student.getSpeakStuName() + "?????????????????????",
                            student.getStudentName() + "?????????????????????");
                }
            }
        }
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            ToastUtils.showShort("???????????????????????????????????????");
        }
        switch (baseEvent.getTagInt()) {
            case EventConfigs.INSTALL_RESULT:
            case EventConfigs.UPDATE_RESULT:
                lockFoul = false;
                int position = stuAdapter.getSaveLayoutSeletePosition();
                if (position == -1) {
                    position = stuAdapter.getItemCount() - 1;
                }
                BaseStuPair baseStuPair = stuPairsList.get(position);

                if (null != baseStuPair) {
                    RoundResult roundResult = (RoundResult) baseEvent.getData();
                    if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        baseStuPair.setFullMark(false);
                        if (baseStuPair.getStudent().getSex() == 0 && TestConfigs.getFullSkip() != null && roundResult.getResult() >= TestConfigs.getFullSkip()[0]) {//??????????????????
                            baseStuPair.setFullMark(true);
                        }
                        if (baseStuPair.getStudent().getSex() == 1 && TestConfigs.getFullSkip() != null && roundResult.getResult() >= TestConfigs.getFullSkip()[1]) {//??????????????????
                            baseStuPair.setFullMark(true);
                        }
                    } else {
                        baseStuPair.setFullMark(false);
                    }
                    String tmp = "";
                    if (roundResult.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                        tmp = "X";
                    } else if (roundResult.getResultState() == RoundResult.RESULT_STATE_WAIVE) {
                        tmp = "??????";
                    } else if (roundResult.getResultState() == RoundResult.RESULT_STATE_BACK) {
                        tmp = "??????";
                    } else {
                        tmp = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
                    }

                    //??????????????????
                    BaseStuPair testPair = stuAdapter.getTestPosition() == -1 ? stuPairsList.get(stuAdapter.getSaveLayoutSeletePosition()) : stuPairsList.get(stuAdapter.getTestPosition());
                    if (TextUtils.equals(testPair.getStudent().getStudentCode(), roundResult.getStudentCode())) {
                        String[] timeResult = baseStuPair.getTimeResult();

                        timeResult[penalizeDialog.getSelectPosition()] = tmp;
                        baseStuPair.setTimeResult(timeResult);
//                        resultList.clear();
//                        resultList.addAll(Arrays.asList(timeResult));
//                        testResultAdapter.notifyDataSetChanged();
                        stuAdapter.notifyDataSetChanged();
                        uploadServer(baseStuPair, roundResult);
                        if (roundResult.getRoundNo() == roundNo) {
                            updateResultLed(((roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) ?
                                    ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()) : tmp));
                        }

                        updateLastResultLed(DBManager.getInstance().queryGroupBestScore(roundResult.getStudentCode(), group.getId()));
                        //????????????????????????
                        if (setTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                            if (roundResult.getRoundNo() != roundNo && stuAdapter.getTestPosition() != -1) {
                                return;
                            }
                            //???????????????????????????
//                            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound(roundResult.getStudentCode(), group.getId() + "");
//                            roundNo = roundResultList.size() + 1;
                            if (!SettingHelper.getSystemSetting().isIdentityMark()) {
                                stuAdapter.setTestPosition(stuAdapter.getSaveLayoutSeletePosition());
                                //????????????
                                if (setTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                                    continuousTest();
                                } else {
                                    //??????
                                    loopTest();
                                }
                            } else {
                                //?????????????????????
                                identityMarkTest();
                            }
                        }
                    } else {

                        for (int i = 0; i < stuPairsList.size(); i++) {
                            BaseStuPair stuPair = stuPairsList.get(i);
                            //??????????????????
                            if (TextUtils.equals(stuPair.getStudent().getStudentCode(), roundResult.getStudentCode())) {
                                uploadServer(stuPair, roundResult);
                                if (roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                                    stuPair.setFullMark(false);
                                    if (stuPair.getStudent().getSex() == 0 && TestConfigs.getFullSkip() != null && roundResult.getResult() >= TestConfigs.getFullSkip()[0]) {//??????????????????
                                        stuPair.setFullMark(true);
                                    }
                                    if (stuPair.getStudent().getSex() == 1 && TestConfigs.getFullSkip() != null && roundResult.getResult() >= TestConfigs.getFullSkip()[1]) {//??????????????????
                                        stuPair.setFullMark(true);
                                    }
                                } else {
                                    stuPair.setFullMark(false);
                                }
                                String[] timeResult = baseStuPair.getTimeResult();
                                if (roundResult.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                                    tmp = "X";
                                } else if (roundResult.getResultState() == RoundResult.RESULT_STATE_WAIVE) {
                                    tmp = "??????";
                                } else if (roundResult.getResultState() == RoundResult.RESULT_STATE_BACK) {
                                    tmp = "??????";
                                } else {
                                    tmp = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult());
                                }
                                timeResult[roundResult.getRoundNo() - 1] = tmp;
                                stuPair.setTimeResult(timeResult);
                                stuAdapter.notifyDataSetChanged();
                                if (setTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                                    if (stuPair.isFullMark()) {
                                        return;
                                    }

                                    for (int j = 0; j < timeResult.length; j++) {
                                        if (TextUtils.isEmpty(timeResult[j])) {
                                            roundNo = j;
                                            stuAdapter.setTestPosition(i);
                                            rvTestStu.scrollToPosition(i);
                                            continuousTest();
                                            return;
                                        }
                                    }
                                }
                                return;
                            }
                        }
                    }
                }

//                SystemSetting setting = SettingHelper.getSystemSetting();
//                RoundResult roundResult = (RoundResult) baseEvent.getData();
//                if (setting.isGroupCheck() && setTestPattern() == TestConfigs.GROUP_PATTERN_LOOP && roundResult.getRoundNo() == roundNo){
//                    finish();
//                }
                break;
//            case EventConfigs.FOUL_DIALOG_MISS:
//                if (lockFoul) {
//                    lockFoul = false;
//                    loopTest();
//                }

//                break;

        }
    }


//    public void setBaseHeight(int height) {
//        tvBaseHeight.setText("????????????" + ResultDisplayUtils.getStrResultForDisplay(height * 10));
//    }

    public void setBeginTxt(int isBegin) {
        tvStartTest.setText(isBegin == 0 ? "????????????" : "??????\n??????");
    }


//    public void setFaultEnable(boolean enable) {
//        isFault = enable;
//    }

    public abstract void initData();

    /**
     * ????????????????????????
     */
    public abstract int setTestCount();

    /**
     * ????????????????????????
     */
    public abstract void gotoItemSetting();

    /**
     * ????????????????????????
     */
    public abstract void startTest(BaseStuPair stuPair);

    /**
     * ???????????????????????? 0 ?????? 1 ??????
     */
    public abstract int setTestPattern();


    public void setTestType(int testType) {
        this.testType = testType;
    }

    @OnClick({R.id.txt_start_test, R.id.txt_led_setting, R.id.txt_stu_skip, R.id.txt_test_result,
            R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.tv_resurvey})
//R.id.tv_penalizeFoul,
    public void onViewClicked(View view) {
        int pos = stuAdapter.getSaveLayoutSeletePosition();
        if (pos == -1) {
            pos = stuAdapter.getItemCount() - 1;
        }
        switch (view.getId()) {
//            case R.id.txt_setting:
//                gotoItemSetting();
//
//                break;
            case R.id.txt_start_test:
                //???????????????????????????
                if (stuAdapter.getTestPosition() == -1) {
                    //????????????????????????
                    toastSpeak("???????????????????????????????????????????????????");
                    return;
                }

                //?????????????????????
                if (isStop && stuPairsList.size() > 0) {
                    if (stuPairsList.get(stuAdapter.getTestPosition()).getBaseDevice().getState() == BaseDeviceState.STATE_ERROR) {
                        toastSpeak("??????????????????");
                        return;
                    }
                    if (!SettingHelper.getSystemSetting().isIdentityMark()) {
//
                        isStop = false;

                        if (testType == 1) {
                            startTest(stuPairsList.get(stuAdapter.getTestPosition()));
                            setShowLed(stuPairsList.get(stuAdapter.getTestPosition()));
                        } else {
                            Message msg = new Message();
                            msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                            ledHandler.removeCallbacksAndMessages(null);
                            ledHandler.sendMessageDelayed(msg, 1000);
                        }

                        tvStartTest.setText("????????????");
                    } else {
                        toastSpeak("????????????????????????????????????????????????????????????");
                    }
                } else {
                    isStop = true;
                    tvStartTest.setText("??????\n??????");
                }

                break;
            case R.id.txt_led_setting:
                if (!SettingHelper.getSystemSetting().isIdentityMark() && !isStop) {
                    toastSpeak("?????????????????????");
                    return;
                }
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.txt_stu_skip:
                studentSkip();
                break;
//            case R.id.txt_stu_fault:
//                showPenalize();
//                break;
//            case R.id.tv_penalizeFoul:
//                BaseStuPair baseStuPair;
//                if (stuAdapter.getTestPosition()==-1){
//                    baseStuPair = stuPairsList.get(stuPairsList.size()-1);
//                }else{
//                    baseStuPair  = stuPairsList.get(stuAdapter.getTestPosition());
//                }
//
//                if (baseStuPair.getStudent() != null) {
//                    DataRetrieveBean bean = new DataRetrieveBean();
//                    bean.setStudentCode(baseStuPair.getStudent().getStudentCode());
//                    bean.setSex(baseStuPair.getStudent().getSex());
//                    bean.setTestState(1);
//                    bean.setGroupId(group.getId());
//                    bean.setScheduleNo(group.getScheduleNo());
//                    bean.setExamType(group.getExamType());
//                    bean.setStudentName(baseStuPair.getStudent().getStudentName());
//                    Intent intent = new Intent(this, DataDisplayActivity.class);
//                    intent.putExtra(DataDisplayActivity.ISSHOWPENALIZEFOUL, isShowPenalizeFoul());
//                    intent.putExtra(DataRetrieveActivity.DATA_ITEM_CODE, getItemCode());
//                    intent.putExtra(DataDisplayActivity.TESTNO, 1);
//                    intent.putExtra(DataRetrieveActivity.DATA_EXTRA, bean);
//
//                    startActivity(intent);
//                } else {
//                    toastSpeak("?????????????????????");
//                }
//                break;
            case R.id.txt_test_result:

                if (SettingHelper.getSystemSetting().isInputTest() && getTestPair().getStudent() != null) {
                    editResultDialog.showDialog(getTestPair().getStudent());
                }
                break;
            case R.id.tv_foul:
                penalizeDialog.setGroupId(group.getId());
                penalizeDialog.setData(1, stuPairsList.get(pos).getStudent(),
                        stuPairsList.get(pos).getTimeResult(), null, null);
                penalizeDialog.showDialog(0);
                lockFoul = true;
                break;
            case R.id.tv_inBack:
                penalizeDialog.setGroupId(group.getId());
                penalizeDialog.setData(1, stuPairsList.get(pos).getStudent(),
                        stuPairsList.get(pos).getTimeResult(), null, null);
                penalizeDialog.showDialog(1);
                break;
            case R.id.tv_abandon:
                penalizeDialog.setGroupId(group.getId());
                penalizeDialog.setData(1, stuPairsList.get(pos).getStudent(),
                        stuPairsList.get(pos).getTimeResult(), null, null);
                penalizeDialog.showDialog(2);
                break;
            case R.id.tv_normal:
                penalizeDialog.setGroupId(group.getId());
                penalizeDialog.setData(2, stuPairsList.get(pos).getStudent(),
                        stuPairsList.get(pos).getTimeResult(), null, null);
                penalizeDialog.showDialog(3);
                break;
            case R.id.tv_resurvey:


                String resultString = stuPairsList.get(stuAdapter.getSaveLayoutSeletePosition())
                        .getSelectResultList().get(stuAdapter.getSaveSeletePosition()).getResult();
                if (TextUtils.isEmpty(resultString)) {
                    toastSpeak("???????????????????????????????????????");
                    return;
                }
                AgainTestDialog dialog = new AgainTestDialog();

                Student student = stuPairsList.get(stuAdapter.getSaveLayoutSeletePosition()).getStudent();
                RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId().toString(), stuAdapter.getSaveSeletePosition() + 1);
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
                        isStop = false;
                        group.setIsTestComplete(0);
                        tvStartTest.setText("????????????");
                        BaseStuPair pair = stuPairsList.get(stuAdapter.getSaveLayoutSeletePosition());
                        pair.getTimeResult()[updateRoundNo - 1] = "";

                        //??????????????????
                        pair.setRoundNo(updateRoundNo);
                        roundNo = updateRoundNo;
                        stuAdapter.setTestPosition(stuAdapter.getSaveLayoutSeletePosition());
                        stuAdapter.indexStuTestResult(stuAdapter.getSaveLayoutSeletePosition(), roundNo - 1);
                        pair.setTimeResult(pair.getTimeResult());
                        stuAdapter.notifyDataSetChanged();
                        toastSpeak(String.format(getString(R.string.test_speak_hint), pair.getStudent().getSpeakStuName(), roundNo)
                                , String.format(getString(R.string.test_speak_hint), pair.getStudent().getStudentName(), roundNo));

                        Message msg = new Message();
                        msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                        ledHandler.removeCallbacksAndMessages(null);
                        ledHandler.sendMessageDelayed(msg, 1000);
                    }
                });
                dialog.show(getSupportFragmentManager(), "AgainTestDialog");

                break;
        }

    }

    private String getItemCode() {
        return TestConfigs.sCurrentItem.getItemCode() == null ? TestConfigs.DEFAULT_ITEM_CODE : TestConfigs.sCurrentItem.getItemCode();
    }

    boolean clicked = false;

    /**
     * ????????????
     */
    private void showPenalize(final BaseDeviceState deviceState, final BaseStuPair pair) {
        clicked = false;
//        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
//        alertDialog.setTitleText(getString(R.string.confirm_result));
//        alertDialog.setCancelable(false);
//        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                sweetAlertDialog.dismissWithAnimation();
//                if (!clicked) {
//                    updatePair(deviceState, pair, false);
//                    clicked = true;
//                }
//            }
//        }).setCancelText(getString(R.string.foul)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                sweetAlertDialog.dismissWithAnimation();
//
//                if (!clicked) {
//                    updatePair(deviceState, pair, true);
//                    clicked = true;
//                }
//            }
//        });
//        alertDialog.show();
    }

    public void updatePair(BaseDeviceState deviceState, BaseStuPair pair, boolean isFault) {
        if (isFault) {
            pair.setResultState(RoundResult.RESULT_STATE_FOUL);
            updateTestResult(pair);
        }
        doTestEnd(deviceState, pair);
    }
//    private void penalize() {
//        BaseStuPair pair = stuPairsList.get(stuAdapter.getTestPosition());
//        if (pair.getStudent() == null)
//            return;
//        //??????????????????
//        RoundResult roundResult = DBManager.getInstance().queryLastScoreByStuCode(pair.getStudent().getStudentCode());
//        //????????????
//        roundResult.setResultState(RoundResult.RESULT_STATE_FOUL);
//        //??????????????????
//        RoundResult bestResult = DBManager.getInstance().queryGroupBestScore(pair.getStudent().getStudentCode(), group.getId());
//        if (bestResult == roundResult) {
//            roundResult.setIsLastResult(0);
//            DBManager.getInstance().updateRoundResult(roundResult);//??????
//            RoundResult best = DBManager.getInstance().queryGroupOrderDescScore(pair.getStudent().getStudentCode(), group.getId());
//            if (best != null && best.getIsLastResult() == 0) {
//                best.setIsLastResult(1);
//                DBManager.getInstance().updateRoundResult(best);//??????????????????
//            }
//        } else {
//            DBManager.getInstance().updateRoundResult(roundResult);//??????
//        }
//
//        //??????????????????
//        pair.setResultState(RoundResult.RESULT_STATE_FOUL);
//        updateTestResult(pair);
//        updateLastResultLed(roundResult);
//        testResultAdapter.notifyDataSetChanged();
//
//        //????????????
//        DBManager.getInstance().insertRoundResult(roundResult);
//        Logger.i("saveResult==>insertRoundResult->" + roundResult.toString());
//
//        List<RoundResult> roundResultList = new ArrayList<>();
//        roundResultList.add(roundResult);
//        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
//                , TestConfigs.getCurrentItemCode(), pair.getStudent().getStudentCode()
//                , "1", group.getGroupNo() + "", RoundResultBean.beanCope(roundResultList));
//
//        uploadResult(uploadResults);
//    }

    /**
     * ????????????
     */
    private void studentSkip() {
        if (isStop) {
            toastSpeak("??????????????????");
            return;
        }
        //??????????????????
        if (stuPairsList == null || stuPairsList.size() == 0 || stuAdapter.getTestPosition() == -1) {
            return;
        }
        //????????????????????????
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            //???????????????????????????????????????
            GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group
                    , stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentCode());
            if (groupItem.getIdentityMark() == 0) {
                return;
            }
        }
        Logger.i("studentSkip=>???????????????" + stuPairsList.get(stuAdapter.getTestPosition()).getStudent());
        //????????????????????????????????????????????????????????????????????????
        int testNo = stuPairsList.get(stuAdapter.getTestPosition()).getTestNo() == -1 ? setTestCount() : stuPairsList.get(stuAdapter.getTestPosition()).getTestNo();
        if (stuAdapter.getTestPosition() == stuPairsList.size() - 1) {
            if (setTestPattern() == 0) { //????????????
                continuousTestNext();
                return;
            } else if (setTestPattern() == 1 && testNo > roundNo) {
                //??????????????????????????????????????????????????????????????????????????????????????????
                roundNo++;
                stuAdapter.setTestPosition(0);
                loopTestNext();
                return;
            } else {
                loopTestNext();
            }
        } else {
            if (setTestPattern() == 0) {//???????????? ?????????
                continuousTestNext();
            } else {
                stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);
                loopTestNext();
            }
            if (stuAdapter.getTestPosition() != -1) {
                print(stuPairsList.get(stuAdapter.getTestPosition()));
            }

        }
    }

    /**
     * ??????????????????
     *
     * @param student
     */
    private void gotoTest(Student student) {
        baseHeight = 0;
//        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG && runUp == 0) {
//            setBaseHeight(0);
//        }
        for (int i = 0; i < stuPairsList.size(); i++) {
            if (TextUtils.equals(stuPairsList.get(i).getStudent().getStudentCode(), student.getStudentCode())) {

                rvTestStu.scrollToPosition(i);
                stuAdapter.setTestPosition(i);
                if (testType == 1) {
                    isStop = true;
                    tvStartTest.setText("??????\n??????");
                } else {
//                    startTest(stuPairsList.get(stuAdapter.getTestPosition()));
                }
                Message msg = new Message();
                msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                ledHandler.removeCallbacksAndMessages(null);
                ledHandler.sendMessageDelayed(msg, 3000);
                break;
            }
        }
    }


    /**
     * ??????????????????
     */
    private void getTestStudent(Group group) {

        roundNo = 1;
        stuPairsList.clear();
        stuAdapter.setTestPosition(-1);
        stuPairsList.addAll((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu"));
        for (BaseStuPair stuPair : stuPairsList) {
            stuPair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE));
            int testNo = stuPair.getTestNo() == -1 ? setTestCount() : stuPair.getTestNo();
            stuPair.setTimeResult(new String[testNo]);
        }
        //??????????????????
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            stuAdapter.notifyDataSetChanged();
            return;
        }

        if (setTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
            for (int i = 0; i < stuPairsList.size(); i++) {

                //  ?????????????????? ???????????????????????????????????????
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (stuPairsList.get(i).getStudent().getStudentCode(), group.getId() + "");
                SystemSetting setting = SettingHelper.getSystemSetting();
                StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(), stuPairsList.get(i).getStudent().getStudentCode());
                //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (studentItem != null) {
                    if ((setting.isResit() || studentItem.getMakeUpType() == StudentItem.EXAM_DELAYED) && !stuPairsList.get(i).isResit()) {
                        roundResultList.clear();
                    }
                }
                int testNo = stuPairsList.get(i).getTestNo() == -1 ? setTestCount() : stuPairsList.get(i).getTestNo();
                if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount()) {
                    if (stuAdapter.getTestPosition() == -1) {
                        stuAdapter.setTestPosition(i);
                        rvTestStu.scrollToPosition(i);
                        stuAdapter.notifyDataSetChanged();
                        roundNo = stuPairsList.get(i).getRoundNo() != 0 ? stuPairsList.get(i).getRoundNo()
                                : (roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.size() + 1);
                        stuAdapter.indexStuTestResult(i, roundNo - 1);
                        stuAdapter.indexStuTestResultSelect(i, roundNo - 1);
                        toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                                String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                        LogUtils.operation(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

                    }
                    if (roundResultList.size() > 0) {
                        setStuPairsData(i, roundResultList);
                    }
                } else {

                    setStuPairsData(i, roundResultList);
                }


            }
        } else {//???????????? ??????????????????
            for (int i = 0; i < stuPairsList.size(); i++) {
                //  ?????????????????? ???????????????????????????????????????
                List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                        (stuPairsList.get(i).getStudent().getStudentCode(), group.getId() + "");
                if ((roundResultList == null || roundResultList.size() == 0) && stuAdapter.getTestPosition() == -1) {
                    stuAdapter.setTestPosition(i);
                    rvTestStu.scrollToPosition(i);
                    stuAdapter.notifyDataSetChanged();
                    roundNo = 1;
                    stuAdapter.indexStuTestResult(i, roundNo - 1);
                    stuAdapter.indexStuTestResultSelect(i, roundNo - 1);
                    toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                            String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                    LogUtils.operation(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

                }
                if (roundResultList.size() > 0) {
                    setStuPairsData(i, roundResultList);
                }
            }
            if (stuAdapter.getTestPosition() == -1) {
                //?????????????????? ????????????????????????????????? ???????????????????????????????????????
                //???????????????????????????????????????
//                int testNo = stuPairsList.get(stuAdapter.getTestPosition()).getTestNo() == -1 ? setTestCount() : stuPairsList.get(stuAdapter.getTestPosition()).getTestNo();
                for (int i = roundNo; i <= setTestCount(); i++) {
                    for (int j = 0; j < stuPairsList.size(); j++) {
                        if (TextUtils.isEmpty(stuPairsList.get(j).getTimeResult()[i - 1]) && stuAdapter.getTestPosition() == -1) {
                            roundNo = i;
                            stuAdapter.setTestPosition(j);
                            rvTestStu.scrollToPosition(j);
                            stuAdapter.notifyDataSetChanged();
                            stuAdapter.indexStuTestResult(j, roundNo - 1);
//                            stuAdapter.indexStuTestResultSelect(i, roundNo - 1);

                            toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                                    String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                            LogUtils.operation(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

                            break;
                        }
                    }
                }

            }
        }
        stuAdapter.notifyDataSetChanged();
        if (stuAdapter.getTestPosition() == -1) {
            allTestComplete();
            return;
        }

//        resultList.clear();
//        resultList.addAll(Arrays.asList(stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult()));
//        testResultAdapter.notifyDataSetChanged();
        stuAdapter.notifyDataSetChanged();
    }

    /**
     * ??????????????????????????????
     *
     * @param index
     * @param roundResultList
     */
    public void setStuPairsData(int index, List<RoundResult> roundResultList) {
        stuPairsList.get(index).setResultState(-99);
//        int testNo = stuPairsList.get(index).getTestNo() == -1 ? setTestCount() : stuPairsList.get(index).getTestNo();
        String[] result = new String[TestConfigs.getMaxTestCount()];

        for (int j = 0; j < roundResultList.size(); j++) {
            result[roundResultList.get(j).getRoundNo() - 1] = RoundResult.resultStateStr(roundResultList.get(j).getResultState(), roundResultList.get(j).getResult());


        }
        stuPairsList.get(index).setTimeResult(result);
    }

    /**
     * ?????????????????????????????????????????????STATE_END???????????????????????????????????????????????????????????????
     *
     * @param deviceState
     */
    public void updateDevice(@NonNull BaseDeviceState deviceState) {
//        LogUtils.operation("??????????????????:" + deviceState);
//        if (stuAdapter == null || stuAdapter.getTestPosition() == -1)
//            return;

        if (deviceState.getState() != BaseDeviceState.STATE_ERROR) {
            cbDeviceState.setChecked(true);
        } else {
            cbDeviceState.setChecked(false);
        }
        if (stuAdapter == null || stuAdapter.getTestPosition() == -1)
            return;
        if (!SettingHelper.getSystemSetting().isInputTest()) {
            BaseStuPair pair = stuPairsList.get(stuAdapter.getTestPosition());
            pair.getBaseDevice().setState(deviceState.getState());
            if (isStop && !SettingHelper.getSystemSetting().isIdentityMark()) {
                return;
            }
            //????????????????????????
            if (deviceState.getState() == BaseDeviceState.STATE_END) {
//                if (isFault && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {
//                    showPenalize(deviceState, pair);
//                } else {
//                    doTestEnd(deviceState, pair);
//                }
                doTestEnd(deviceState, pair);
            }

        }


        //??????????????????
//        stuAdapter.notifyDataSetChanged();


    }

    /**
     * ??????????????????
     *
     * @param deviceState
     * @param pair
     */
    private void doTestEnd(@NonNull BaseDeviceState deviceState, BaseStuPair pair) {
        RoundResult roundResult = null;
        if (pair.getRoundNo() != 0) {
            roundResult = DBManager.getInstance().queryGroupRoundNoResult(pair.getStudent().getStudentCode(), group.getId() + "", pair.getRoundNo());
        } else {
            roundResult = DBManager.getInstance().queryGroupRoundNoResult(pair.getStudent().getStudentCode(), group.getId() + "", roundNo);
        }
        if (roundResult != null) {
            toastSpeak("???????????????????????????");
            return;
        }
        //??????????????????
//        resultList.clear();
//        resultList.addAll(Arrays.asList(stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult()));
//        testResultAdapter.notifyDataSetChanged();
        stuAdapter.notifyDataSetChanged();
        lastStudent = pair.getStudent();
        lastResult = stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult();
        //????????????
        saveResult(pair);
        printResult(pair);
        broadResult(pair);
//            setShowLed(pair);

        //?????????????????????
        if (!SettingHelper.getSystemSetting().isIdentityMark()) {

            //????????????
            if (setTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                continuousTest();
            } else {
                //??????
                if (testType == 0 && lockFoul) {
                    return;
                }
                loopTest();
            }
        } else {
            //?????????????????????
            identityMarkTest();
        }
    }


    /**
     * ??????????????????
     *
     * @param baseStuPair ????????????
     */
    private void saveResult(@NonNull BaseStuPair baseStuPair) {
        LogUtils.all("????????????:" + baseStuPair.toString());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        if (TextUtils.isEmpty(baseStuPair.getEndTime())) {
            roundResult.setEndTime(DateUtil.getCurrentTime() + "");
        } else {
            roundResult.setEndTime(baseStuPair.getEndTime());
        }

        roundResult.setTestTime(baseStuPair.getTestTime());
        if (baseStuPair.getRoundNo() != 0) {
            roundResult.setRoundNo(baseStuPair.getRoundNo());
            baseStuPair.setRoundNo(0);
            roundResult.setResultTestState(RoundResult.RESULT_RESURVEY_STATE);
            stuPairsList.get(stuAdapter.getTestPosition()).setRoundNo(0);
        } else {
            roundResult.setRoundNo(roundNo);
            roundResult.setResultTestState(0);
        }
        Log.e("TAG", "round=" + roundResult.getRoundNo());
        roundResult.setTestNo(1);
        roundResult.setGroupId(group.getId());
        GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group, baseStuPair.getStudent().getStudentCode());
        if (group.getExamType() == StudentItem.EXAM_MAKE) {
            roundResult.setExamType(group.getExamType());
        } else {
            roundResult.setExamType(groupItem.getExamType());
        }
        roundResult.setScheduleNo(group.getScheduleNo());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        RoundResult bestResult = DBManager.getInstance().queryGroupBestScore(baseStuPair.getStudent().getStudentCode(), group.getId());
        if (bestResult != null) {
            // ???????????????????????? ????????????????????????????????????????????????????????????
            if (bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL && baseStuPair.getResultState() == RoundResult.RESULT_STATE_NORMAL && bestResult.getResult() <= baseStuPair.getResult()) {
                // ????????????????????????????????????????????????
                roundResult.setIsLastResult(1);
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                updateLastResultLed(roundResult);
            } else {
                if (bestResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    roundResult.setIsLastResult(1);
                    bestResult.setIsLastResult(0);
                    DBManager.getInstance().updateRoundResult(bestResult);
                    updateLastResultLed(roundResult);
                } else {
                    roundResult.setIsLastResult(0);
                    updateLastResultLed(bestResult);
                }
            }
        } else {
            // ???????????????
            roundResult.setIsLastResult(1);
            updateLastResultLed(roundResult);
        }

        DBManager.getInstance().insertRoundResult(roundResult);
        LogUtils.operation("????????????:" + roundResult.toString());
        uploadServer(baseStuPair, roundResult);
//        if (groupItem != null && groupItem.getExamType() == StudentItem.EXAM_MAKE) {
//            //???????????????????????????
//            continuousTest();
//        }

        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        //?????????????????????????????? ????????????????????????????????????
//        if (systemSetting.isGroupCheck() && setTestPattern() == TestConfigs.GROUP_PATTERN_LOOP){
//            finish();
//        }
//        if (systemSetting.isGroupCheck() && setTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE && TestConfigs.getMaxTestCount() == roundNo){
//            finish();
//        }
    }

    /**
     * ???????????????????????????
     *
     * @param baseStu
     */
    public synchronized void updateTestResult(@NonNull BaseStuPair baseStu) {
        if (isStop) {
            return;
        }
        BaseStuPair pair = stuPairsList.get(stuAdapter.getTestPosition());
        pair.setResult(baseStu.getResult());
        pair.setResultState(baseStu.getResultState());
        pair.setFullMark(baseStu.isFullMark());
        //ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())
        if (pair.getRoundNo() != 0) {
            pair.getTimeResult()[pair.getRoundNo() - 1] = RoundResult.resultStateStr(baseStu.getResultState(), baseStu.getResult());

        } else {
            pair.getTimeResult()[roundNo - 1] = RoundResult.resultStateStr(baseStu.getResultState(), baseStu.getResult());

        }
        pair.setTimeResult(pair.getTimeResult());
//        pair.getTimeResult()[roundNo - 1] = ((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult()));
        stuAdapter.notifyDataSetChanged();

        txtStuResult.setText((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult()));
        updateResultLed((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult()));
    }

    private void uploadServer(@NonNull BaseStuPair baseStuPair, RoundResult roundResult) {
        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), baseStuPair.getStudent().getStudentCode()
                , "1", group, RoundResultBean.beanCope(roundResultList, group));

        uploadResult(uploadResults);
    }


    /**
     * ????????????
     *
     * @param uploadResults ????????????
     */
    private void uploadResult(UploadResults uploadResults) {
        if (!SettingHelper.getSystemSetting().isRtUpload()) {
            return;
        }
        if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            ToastUtils.showShort("?????????????????????????????????????????????");
            return;
        }
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            Intent serverIntent = new Intent(this, UploadService.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(UploadResults.BEAN_KEY, uploadResults);
            serverIntent.putExtras(bundle);
            startService(serverIntent);
        }


    }


    /**
     * ????????????
     */
    private void broadResult(@NonNull BaseStuPair baseStuPair) {
        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
            if (baseStuPair.getResultState() == RoundResult.RESULT_STATE_FOUL) {
                TtsManager.getInstance().speak(baseStuPair.getStudent().getSpeakStuName() + "??????");
            } else {
                TtsManager.getInstance().speak(String.format(getString(R.string.speak_result), baseStuPair.getStudent().getSpeakStuName(), ResultDisplayUtils.getStrResultForDisplay(baseStuPair.getResult())));
            }
        }
    }

    /**
     * LED?????????
     *
     * @param stuPair
     */
    private void setShowLed(BaseStuPair stuPair) {
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stuPair.getStudent().getLEDStuName() + "   ???" + roundNo + "???", 0, 0, true, false);
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 1, false, true);
        RoundResult bestResult = DBManager.getInstance().queryGroupBestScore(stuPair.getStudent().getStudentCode(), group.getId());
        if (bestResult != null && bestResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
            byte[] data = new byte[16];
            String str = "?????????";
            try {
                byte[] strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = ResultDisplayUtils.getStrResultForDisplay(bestResult.getResult()).getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);
        } else {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 2, false, true);

        }
        if (!SettingHelper.getSystemSetting().isIdentityMark()) {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "????????????" + getNextName(), 0, 3, false, true);
        }
    }

    private void updateResultLed(String result) {
        byte[] data = new byte[16];
        String str = "?????????";
        try {
            byte[] strData = str.getBytes("GB2312");
            System.arraycopy(strData, 0, data, 0, strData.length);
            byte[] resultData = result.getBytes("GB2312");
            System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 1, false, true);
    }

    private void updateLastResultLed(RoundResult roundResult) {
        if (roundResult != null && roundResult.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
            byte[] data = new byte[16];
            String str = "?????????";
            try {
                byte[] strData = str.getBytes("GB2312");
                System.arraycopy(strData, 0, data, 0, strData.length);
                byte[] resultData = ResultDisplayUtils.getStrResultForDisplay(roundResult.getResult()).getBytes("GB2312");
                System.arraycopy(resultData, 0, data, data.length - resultData.length - 1, resultData.length);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), data, 0, 2, false, true);
        } else {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), "?????????", 0, 2, false, true);

        }

    }

    private String getNextName() {
        if (stuPairsList.size() == stuAdapter.getTestPosition() + 1) {
            if (setTestPattern() == 0) {//??????
                return "";
            } else {
                int testNo = stuPairsList.get(stuAdapter.getTestPosition()).getTestNo() == -1 ? setTestCount() : stuPairsList.get(stuAdapter.getTestPosition()).getTestNo();
                if (roundNo == testNo) {
                    return "";
                } else {
                    return stuPairsList.get(0).getStudent().getLEDStuName();
                }
            }
        } else {
            return stuPairsList.get(stuAdapter.getTestPosition() + 1).getStudent().getLEDStuName();
        }
    }

    /**
     * LED?????????
     *
     * @param stuPair
     */
    private void setStuShowLed(BaseStuPair stuPair) {
        if (stuPair == null)
            return;
        mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(), stuPair.getTrackNo() + "   " + stuPair.getStudent().getLEDStuName(), 0, 0, true, false);
        for (int i = 0; i < stuPair.getTimeResult().length; i++) {
            mLEDManager.showString(SettingHelper.getSystemSetting().getHostId(),
                    String.format("%-4s", String.format("???%1$d??????", i + 1)) + (TextUtils.isEmpty(stuPair.getTimeResult()[i]) ? "" : stuPair.getTimeResult()[i]),
                    0, i + 1, false, true);
        }

    }


    /**
     * ????????????
     *
     * @param baseStuPair
     */
    private void printResult(@NonNull BaseStuPair baseStuPair) {
        if (!SettingHelper.getSystemSetting().isAutoPrint())
            return;
        //???????????????????????????????????????????????????
        int testNo = baseStuPair.getTestNo() == -1 ? setTestCount() : baseStuPair.getTestNo();
        if (roundNo < testNo && !baseStuPair.isFullMark()) {
            return;
        }
        print(baseStuPair);
    }

    private void print(@NonNull BaseStuPair baseStuPair) {
        Student student = baseStuPair.getStudent();
//        PrinterManager.getInstance().print(" \n");
        PrinterManager.getInstance().print(TestConfigs.sCurrentItem.getItemName() + SettingHelper.getSystemSetting().getHostId() + "??????  " + group.getGroupNo() + "???");
        PrinterManager.getInstance().print("???  ???:" + baseStuPair.getTrackNo() + "");
        PrinterManager.getInstance().print("???  ???:" + student.getStudentCode() + "");
        PrinterManager.getInstance().print("???  ???:" + student.getStudentName() + "");
        for (int i = 0; i < baseStuPair.getTimeResult().length; i++) {
            if (!TextUtils.isEmpty(baseStuPair.getTimeResult()[i])) {
                PrinterManager.getInstance().print(String.format("???%1$d??????", i + 1) + baseStuPair.getTimeResult()[i] + "");
            } else {
                PrinterManager.getInstance().print(String.format("???%1$d??????", i + 1) + "");
            }
        }
        PrinterManager.getInstance().print("????????????:" + TestConfigs.df.format(Calendar.getInstance().getTime()) + "");
        PrinterManager.getInstance().print(" \n");
    }

    /**
     * ????????????
     */
    private void continuousTest() {
        BaseStuPair pair = stuAdapter.getTestPosition() == -1 ? stuPairsList.get(stuAdapter.getSaveLayoutSeletePosition()) : stuPairsList.get(stuAdapter.getTestPosition());

        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        int testNo = pair.getTestNo() == -1 ? setTestCount() : pair.getTestNo();
        if (roundNo < testNo) {
            if (pair.isFullMark() && pair.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
//                //???????????????????????????
//                if (stuAdapter.getTestPosition() == stuPairsList.size() - 1) {
//                    //????????????????????????
//                    allTestComplete();
//                    return;
//                }
                if (isShowPenalizeFoul() == View.GONE) {
                    continuousTestNext();
                }

                return;

            }
            //???????????????????????????????????????
            boolean isAllTest = true;
            for (int i = 0; i < pair.getTimeResult().length; i++) {
                if (TextUtils.isEmpty(pair.getTimeResult()[i])) {
                    isAllTest = false;
                    roundNo = (i + 1);
                    break;
                }
            }

            if (isAllTest) {
                continuousTestNext();
                return;
            }
//            roundNo++;
            pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            stuAdapter.indexStuTestResult(stuAdapter.getTestPosition(), roundNo - 1);
            if (testType == 1) {
                isStop = true;
                tvStartTest.setText("??????\n??????");
            } else {

                stuAdapter.notifyDataSetChanged();
                toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
            }
            Message msg = new Message();
            msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
            ledHandler.removeCallbacksAndMessages(null);
            ledHandler.sendMessageDelayed(msg, 3000);
        } else {
            //???????????????????????????
            if (stuAdapter.getTestPosition() == stuPairsList.size() - 1) {
                //????????????????????????
                allTestComplete();
                return;
            }
            if (isShowPenalizeFoul() == View.GONE) {
                continuousTestNext();

            }
        }
    }

    /**
     * ?????????????????????
     */
    private void continuousTestNext() {
        baseHeight = 0;
//        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG && runUp == 0) {
//            setBaseHeight(0);
//        }
        for (int i = (stuAdapter.getTestPosition() + 1); i < stuPairsList.size(); i++) {

            //  ?????????????????? ???????????????????????????????????????
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                    (stuPairsList.get(i).getStudent().getStudentCode(), group.getId() + "");
            int testNo = stuPairsList.get(i).getTestNo() == -1 ? setTestCount() : stuPairsList.get(i).getTestNo();
            if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < testNo) {

                roundNo = stuPairsList.get(i).getRoundNo() != 0 ? stuPairsList.get(i).getRoundNo()
                        : (roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.size() + 1);

                stuAdapter.setTestPosition(i);
                if (testType == 1) {
                    isStop = true;
                    tvStartTest.setText("??????\n??????");
                }
                stuAdapter.indexStuTestResult(stuAdapter.getTestPosition(), roundNo - 1);
                stuAdapter.notifyDataSetChanged();
                //???????????????????????????
                toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

                Message msg = new Message();
                msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                ledHandler.removeCallbacksAndMessages(null);
                ledHandler.sendMessageDelayed(msg, 3000);
                group.setIsTestComplete(2);
                DBManager.getInstance().updateGroup(group);
                return;
            }

        }
        if (!isAllTest()) {
            roundNo = 1;
            stuAdapter.setTestPosition(0);

            continuousTest();
            return;
        }
        //????????????????????????
        allTestComplete();
    }

    /**
     * ????????????
     */
    private void loopTest() {
        //???????????????????????????
        if (stuAdapter.getTestPosition() == stuPairsList.size() - 1) {
            //????????????????????????????????????????????????
            int testNo = stuPairsList.get(stuAdapter.getTestPosition()).getTestNo() == -1 ? setTestCount() : stuPairsList.get(stuAdapter.getTestPosition()).getTestNo();
            if (testNo > roundNo) {
                if (isShowPenalizeFoul() == View.GONE) {
                    roundNo++;
                    //????????????????????????????????????????????????????????????????????????
                    stuAdapter.setTestPosition(0);
                    loopTestNext();
                }
                return;
            } else {
                //????????????????????????
//                allTestComplete();
                if (isShowPenalizeFoul() == View.GONE) {
                    loopTestNext();
                }
                return;
            }
        }

        if (isShowPenalizeFoul() == View.GONE) {
            //????????????????????????????????????????????????????????????????????????
            stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);
            loopTestNext();
        }

    }

    /**
     * ???????????????????????????
     */
    private void loopTestNext() {
        baseHeight = 0;
//        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG && runUp == 0) {
//            setBaseHeight(0);
//        }
        //?????????????????? ????????????????????????????????? ???????????????????????????????????????
        int testNo = stuPairsList.get(stuAdapter.getTestPosition()).getTestNo() == -1 ? setTestCount() : stuPairsList.get(stuAdapter.getTestPosition()).getTestNo();
        for (int i = roundNo; i <= testNo; i++) {
            for (int j = stuAdapter.getTestPosition(); j < stuPairsList.size(); j++) {
                if (TextUtils.isEmpty(stuPairsList.get(j).getTimeResult()[i - 1])) {
                    if (stuPairsList.get(j).isFullMark() && stuPairsList.get(j).getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        continue;
                    }
                    roundNo = i;
                    stuAdapter.setTestPosition(j);
                    if (testType == 1) {
                        isStop = true;
                        tvStartTest.setText("??????\n??????");
                    }
                    stuAdapter.indexStuTestResult(stuAdapter.getTestPosition(), roundNo - 1);
                    stuAdapter.notifyDataSetChanged();
                    toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                            String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                    LogUtils.operation(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                    Message msg = new Message();
                    msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                    ledHandler.removeCallbacksAndMessages(null);
                    ledHandler.sendMessageDelayed(msg, 3000);
                    group.setIsTestComplete(2);
                    DBManager.getInstance().updateGroup(group);
                    return;
                }
            }
            stuAdapter.setTestPosition(0);
        }

        if (!isAllTest()) {
            roundNo = 1;
            stuAdapter.setTestPosition(0);
            loopTestNext();
            return;
        }

        //????????????????????????
        allTestComplete();

    }

    private boolean isAllTest() {
        for (BaseStuPair stuPair : stuPairsList) {
            if (!stuPair.isFullMark()) {
                for (String s : stuPair.getTimeResult()) {
                    if (TextUtils.isEmpty(s)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * ???????????????????????????
     */
    private void identityMarkTest() {
        BaseStuPair pair = stuPairsList.get(stuAdapter.getTestPosition());
        int testNo = pair.getTestNo() == -1 ? setTestCount() : pair.getTestNo();
        if (roundNo < testNo && !pair.isFullMark()) {
            roundNo++;
            pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            if (testType == 1) {
                isStop = true;
                tvStartTest.setText("??????\n??????");
            } else {
                stuAdapter.indexStuTestResult(stuAdapter.getTestPosition(), roundNo - 1);
                stuAdapter.notifyDataSetChanged();
                toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));
                LogUtils.operation(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

            }
            Message msg = new Message();
            msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
            ledHandler.removeCallbacksAndMessages(null);
            ledHandler.sendMessageDelayed(msg, 3000);
        } else {
            //????????????????????????????????????
            checkTestComplete();
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    private void checkTestComplete() {
        boolean isAllTest = true;
        for (BaseStuPair stuPair : stuPairsList) {
            if (TextUtils.isEmpty(stuPair.getTimeResult()[0])) {
                isAllTest = false;
            }
        }
        if (isAllTest) {
            //????????????????????????
            allTestComplete();
        }
    }

    /**
     * ?????????????????????
     */
    private void allTestComplete() {
        if (group.getIsTestComplete() != 1 &&
                SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN &&
                (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 || SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP) &&
                SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printA4Result(this, group);
        }
        //????????????????????????
        toastSpeak("???????????????????????????????????????????????????");
        roundNo = 1;
        stuAdapter.setTestPosition(-1);
        stuAdapter.notifyDataSetChanged();
        isStop = true;
        tvStartTest.setText("??????\n??????");
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
    }

    private static class LedHandler extends Handler {

        private WeakReference<BaseGroupTestActivity> mActivityWeakReference;

        public LedHandler(BaseGroupTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            BaseGroupTestActivity activity = mActivityWeakReference.get();
            if (activity.stuAdapter == null || activity.stuAdapter.getTestPosition() == -1) {
                return;
            }
            activity.setShowLed((BaseStuPair) msg.obj);
            activity.txtStuResult.setText("");
            if (activity.testType == 0) {
                activity.startTest(activity.stuPairsList.get(activity.stuAdapter.getTestPosition()));
            }
            //??????????????????
//            activity.resultList.clear();
//            activity.resultList.addAll(Arrays.asList(activity.stuPairsList.get(activity.stuAdapter.getTestPosition()).getTimeResult()));
//            activity.testResultAdapter.notifyDataSetChanged();
            activity.rvTestStu.scrollToPosition(activity.stuAdapter.getTestPosition());
            activity.stuAdapter.notifyDataSetChanged();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RadioManager.getInstance().clearListener();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
        Intent serverIntent = new Intent(this, UploadService.class);
        stopService(serverIntent);
        ledHandler.removeCallbacksAndMessages(null);
    }
}
