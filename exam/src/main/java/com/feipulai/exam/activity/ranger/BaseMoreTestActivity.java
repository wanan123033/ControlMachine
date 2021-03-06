package com.feipulai.exam.activity.ranger;

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
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.printer.PrinterManager;
import com.feipulai.device.spputils.beans.RangerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.activity.base.PenalizeDialog;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
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
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class BaseMoreTestActivity extends BaseCheckActivity implements PenalizeDialog.PenalizeListener {
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
    @BindView(R.id.ll_state)
    public LinearLayout llState;
    @BindView(R.id.cb_device_state)
    public CheckBox cbDeviceState;
    @BindView(R.id.tv_base_height)
    TextView tvBaseHeight;
    @BindView(R.id.txt_stu_skip)
    TextView txtStuSkip;
    //    @BindView(R.id.txt_stu_fault)
//    TextView txtStuFault;
    private List<BaseStuPair> stuPairsList;
    private BaseGroupTestStuAdapter stuAdapter;
    private List<String> resultList = new ArrayList<>();
    private BasePersonTestResultAdapter testResultAdapter;
    /**
     * ?????????????????????
     */
    protected int roundNo = 1;
    /**
     * ??????????????????
     */
    private boolean isStop = true;
    private LEDManager mLEDManager;
    private Group group;

    private LedHandler ledHandler = new LedHandler(this);
    private int testType = 1;//0?????? 1??????
    /**
     * ????????????  0????????? 1??????
     */
    public int runUp;
    public int baseHeight;
    private boolean isFault;
    private RangerSetting setting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_base_more_test;
    }
    @Override
    public void setRoundNo(Student student, int roundNo) {
        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        if (systemSetting.isResit())
            this.roundNo = roundNo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        PrinterManager.getInstance().init();
        group = (Group) TestConfigs.baseGroupMap.get("group");
        initData();

        setting = SharedPrefsUtil.loadFormSource(getApplicationContext(),RangerSetting.class);
        mLEDManager = new LEDManager();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId());
        mLEDManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));

        rvTestStu.setLayoutManager(new LinearLayoutManager(this));
        stuPairsList = new ArrayList<>();
        stuAdapter = new BaseGroupTestStuAdapter(stuPairsList);
        rvTestStu.setAdapter(stuAdapter);

        StringBuffer sbName = new StringBuffer();
        sbName.append(group.getGroupType() == Group.MALE ? "??????" :
                (group.getGroupType() == Group.FEMALE ? "??????" : "????????????"));
        sbName.append(group.getSortName() + String.format("???%1$d???", group.getGroupNo()));
        txtGroupName.setText(sbName);

        stuAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (stuPairsList.get(position).getBaseDevice().getState() == BaseDeviceState.STATE_ERROR) {
                    startTest(stuPairsList.get(position));
                }
            }
        });
        if (SettingHelper.getSystemSetting().isIdentityMark()) {
            setOpenDevice(true);
        } else {
            setOpenDevice(false);
        }


        GridLayoutManager layoutManager = new GridLayoutManager(this, setTestCount());
        rvTestResult.setLayoutManager(layoutManager);
        String result[] = new String[setTestCount()];

        //???????????????
        resultList.addAll(Arrays.asList(result));
        testResultAdapter = new BasePersonTestResultAdapter(resultList);
        //???RecyclerView???????????????
        rvTestResult.setAdapter(testResultAdapter);

        getTestStudent(group);
        setStuShowLed(stuAdapter.getTestPosition() != -1 ? stuPairsList.get(stuAdapter.getTestPosition()) : null);
    }

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
            if ((roundResultList.size() == 0 || roundResultList.size() < setTestCount()) && groupItem != null) {
                isStop = false;
                roundNo = roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
                gotoTest(student);
                groupItem.setIdentityMark(1);
                DBManager.getInstance().updateStudentGroupItem(groupItem);
            } else if (groupItem == null) {//?????????
                toastSpeak(student.getSpeakStuName() + "????????????????????????????????????????????????",
                        student.getStudentName() + "????????????????????????????????????????????????");
            } else if (roundResultList.size() > 0) {
                toastSpeak(student.getSpeakStuName() + "?????????????????????",
                        student.getStudentName() + "?????????????????????");
            }
        }
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        if (baseEvent.getTagInt() == EventConfigs.TOKEN_ERROR) {
            ToastUtils.showShort("???????????????????????????????????????");
        }
    }

    public void setBaseHeightVisible(int visible) {
        tvBaseHeight.setVisibility(visible == 0 ? View.VISIBLE : View.GONE);
        txtStuSkip.setVisibility(View.GONE);
    }

    public void setBaseHeight(int height) {
        tvBaseHeight.setText("????????????" + ResultDisplayUtils.getStrResultForDisplay(height * 10));
    }

    public void setBeginTxt(int isBegin) {
        tvStartTest.setText(isBegin == 0 ? "????????????" : "????????????");
    }


    public void setFaultEnable(boolean enable) {
        isFault = enable;
    }

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
     * ????????????
     */
    public abstract void startTest(BaseStuPair stuPair);

    /**
     * ???????????????????????? 0 ?????? 1 ??????
     */
    public abstract int setTestPattern();


    public void setTestType(int testType) {
        this.testType = testType;
    }

    @OnClick({R.id.txt_start_test, R.id.txt_led_setting, R.id.txt_stu_skip,R.id.txt_commit,R.id.txt_penglize,R.id.txt_fg})
    public void onViewClicked(View view) {
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

//                        toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
//                                String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

                        if (testType == 1) {
                            startTest(stuPairsList.get(stuAdapter.getTestPosition()));
                            setShowLed(stuPairsList.get(stuAdapter.getTestPosition()));
                        } else {
                            Message msg = new Message();
                            msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                            ledHandler.sendMessageDelayed(msg, 1000);
                        }

                        tvStartTest.setText("????????????");
                    } else {
                        toastSpeak("????????????????????????????????????????????????????????????");
                    }
                } else {
                    isStop = true;
                    tvStartTest.setText("????????????");
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
            case R.id.txt_commit:
                commitTest();
                break;
            case R.id.txt_penglize:
                showPenalize();
                break;
            case R.id.txt_fg:
                fgStuTest();
                break;
//            case R.id.txt_stu_fault:
//                showPenalize();
//                break;
        }
    }

    protected abstract void commitTest();

    protected abstract void showPenalize();

    protected abstract void fgStuTest();

    /**
     * ????????????
     */
    public void showPenalize(final BaseDeviceState deviceState, final BaseStuPair pair) {
//        SweetAlertDialog alertDialog = new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
//        alertDialog.setTitleText(getString(R.string.confirm_result));
//        alertDialog.setCancelable(false);
//        alertDialog.setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                sweetAlertDialog.dismissWithAnimation();
//                updatePair(deviceState, pair, false);
//            }
//        }).setCancelText(getString(R.string.foul)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//            @Override
//            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                sweetAlertDialog.dismissWithAnimation();
//                updatePair(deviceState, pair, true);
//            }
//        }).show();

        PenalizeDialog dialog = new PenalizeDialog(this);
        dialog.setPenalizeListener(this);
        dialog.setMinMaxValue(-1,1);
        dialog.show();

    }

    public void updatePair(BaseDeviceState deviceState, BaseStuPair pair, boolean isFault) {
        if (isFault) {
            pair.setResultState(RoundResult.RESULT_STATE_FOUL);
            updateTestResult(pair);
        }
        doTestEnd(deviceState, pair);
    }

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
        if (stuAdapter.getTestPosition() == stuPairsList.size() - 1) {
            if (setTestPattern() == 0) { //????????????
                //????????????????????????
                allTestComplete();
                return;
            } else if (setTestPattern() == 1 && setTestCount() > roundNo) {
                //??????????????????????????????????????????????????????????????????????????????????????????
                roundNo++;
                stuAdapter.setTestPosition(0);
                loopTestNext();
                return;
            } else {
                allTestComplete();
            }
        } else {
            if (setTestPattern() == 0) {//???????????? ?????????
                continuousTestNext();
            } else {
                stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);
                loopTestNext();
            }
            print(stuPairsList.get(stuAdapter.getTestPosition()));
        }
    }

    /**
     * ??????????????????
     *
     * @param student
     */
    private void gotoTest(Student student) {
        baseHeight = 0;
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG && runUp == 0) {
            setBaseHeight(0);
        }
        for (int i = 0; i < stuPairsList.size(); i++) {
            if (TextUtils.equals(stuPairsList.get(i).getStudent().getStudentCode(), student.getStudentCode())) {

                rvTestStu.scrollToPosition(i);
                stuAdapter.setTestPosition(i);
                if (testType == 1) {
                    isStop = true;
                    tvStartTest.setText("????????????");
                } else {
//                    startTest(stuPairsList.get(stuAdapter.getTestPosition()));
                }
                Message msg = new Message();
                msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
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
            stuPair.setTimeResult(new String[setTestCount()]);
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

                if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount()) {
                    if (stuAdapter.getTestPosition() == -1) {
                        stuAdapter.setTestPosition(i);
                        rvTestStu.scrollToPosition(i);
                        roundNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;
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
                    roundNo = 1;
                }
                if (roundResultList.size() > 0) {
                    setStuPairsData(i, roundResultList);
                }
            }
            if (stuAdapter.getTestPosition() == -1) {
                //?????????????????? ????????????????????????????????? ???????????????????????????????????????
                for (int i = roundNo; i <= setTestCount(); i++) {
                    for (int j = 0; j < stuPairsList.size(); j++) {
                        if (TextUtils.isEmpty(stuPairsList.get(j).getTimeResult()[i - 1]) && stuAdapter.getTestPosition() == -1) {
                            roundNo = i;
                            stuAdapter.setTestPosition(j);
                            rvTestStu.scrollToPosition(j);
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
        resultList.clear();
        resultList.addAll(Arrays.asList(stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult()));
        testResultAdapter.notifyDataSetChanged();
    }

    /**
     * ??????????????????????????????
     *
     * @param index
     * @param roundResultList
     */
    public void setStuPairsData(int index, List<RoundResult> roundResultList) {
        stuPairsList.get(index).setResultState(-99);
        String[] result = new String[setTestCount()];
        for (int j = 0; j < roundResultList.size(); j++) {
            switch (roundResultList.get(j).getResultState()) {
                case RoundResult.RESULT_STATE_FOUL:
                    result[j] = "X";
                    break;
                case -2:
                    result[j] = "??????";
                    break;
                default:
                    result[j] = ResultDisplayUtils.getStrResultForDisplay(roundResultList.get(j).getResult());
                    break;
            }

        }
        stuPairsList.get(index).setTimeResult(result);
    }

    /**
     * ?????????????????????????????????????????????STATE_END???????????????????????????????????????????????????????????????
     *
     * @param deviceState
     */
    public void updateDevice(@NonNull BaseDeviceState deviceState) {
        if (stuAdapter == null || stuAdapter.getTestPosition() == -1)
            return;

        if (deviceState.getState() != BaseDeviceState.STATE_ERROR) {
            cbDeviceState.setChecked(true);
        } else {
            cbDeviceState.setChecked(false);
        }
        BaseStuPair pair = stuPairsList.get(stuAdapter.getTestPosition());
        pair.getBaseDevice().setState(deviceState.getState());
        if (isStop && !SettingHelper.getSystemSetting().isIdentityMark()) {
            return;
        }
        //????????????????????????
        if (deviceState.getState() == BaseDeviceState.STATE_END) {
            if (isFault && pair.getResultState() != RoundResult.RESULT_STATE_FOUL) {
                showPenalize(deviceState, pair);
            } else {
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
        //??????????????????
        resultList.clear();
        resultList.addAll(Arrays.asList(stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult()));
        testResultAdapter.notifyDataSetChanged();

        Logger.i("??????" + stuPairsList.get(stuAdapter.getTestPosition()).getStudent().toString());
        Logger.i("??????????????????STATE_END==>" + deviceState.toString());
        //????????????
        saveResult(stuPairsList.get(stuAdapter.getTestPosition()));
        printResult(stuPairsList.get(stuAdapter.getTestPosition()));
        broadResult(stuPairsList.get(stuAdapter.getTestPosition()));
//            setShowLed(pair);

        //?????????????????????
        if (!SettingHelper.getSystemSetting().isIdentityMark()) {
            //????????????
            if (setTestPattern() == 0) {
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


    /**
     * ???????????????????????????
     *
     * @param baseStu
     */
    public synchronized void updateTestResult(@NonNull BaseStuPair baseStu) {
        if (isStop) {
            return;
        }
        stuPairsList.get(stuAdapter.getTestPosition()).setResult(baseStu.getResult());
        stuPairsList.get(stuAdapter.getTestPosition()).setResultState(baseStu.getResultState());
        stuPairsList.get(stuAdapter.getTestPosition()).setFullMark(baseStu.isFullMark());
        //ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult())
        stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult()[roundNo - 1] = ((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult()));
//        stuAdapter.notifyDataSetChanged();

        txtStuResult.setText((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult()));
        updateResultLed((baseStu.getResultState() == RoundResult.RESULT_STATE_FOUL) ? "X" : ResultDisplayUtils.getStrResultForDisplay(baseStu.getResult()));
    }

    /**
     * ??????????????????
     *
     * @param baseStuPair ????????????
     */
    private void saveResult(@NonNull BaseStuPair baseStuPair) {
        Logger.i("saveResult==>" + baseStuPair.toString());
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(baseStuPair.getStudent().getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(baseStuPair.getResult());
        roundResult.setMachineResult(baseStuPair.getResult());
        roundResult.setResultState(baseStuPair.getResultState());
        roundResult.setEndTime(baseStuPair.getEndTime());
        roundResult.setTestTime(baseStuPair.getTestTime());
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(1);
        roundResult.setGroupId(group.getId());
        roundResult.setExamType(group.getExamType());
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
        //??????????????????
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        roundResult.setEndTime(sdf.format(new Date()));
        DBManager.getInstance().insertRoundResult(roundResult);
        Logger.i("saveResult==>insertRoundResult->" + roundResult.toString());

        List<RoundResult> roundResultList = new ArrayList<>();
        roundResultList.add(roundResult);
        UploadResults uploadResults = new UploadResults(group.getScheduleNo()
                , TestConfigs.getCurrentItemCode(), baseStuPair.getStudent().getStudentCode()
                , "1", group , RoundResultBean.beanCope(roundResultList,group));

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
                if (roundNo == setTestCount()) {
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
        if (roundNo < setTestCount() && !baseStuPair.isFullMark()) {
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
        BaseStuPair pair = stuPairsList.get(stuAdapter.getTestPosition());
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        if (roundNo < setTestCount()) {
            if (pair.isFullMark() && pair.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                //???????????????????????????
                if (stuAdapter.getTestPosition() == stuPairsList.size() - 1) {
                    //????????????????????????
                    allTestComplete();
                    return;
                }
                continuousTestNext();
                return;

            }

            roundNo++;
            pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            if (testType == 1) {
                isStop = true;
                tvStartTest.setText("????????????");
            } else {
                toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

            }
            Message msg = new Message();
            msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
            ledHandler.sendMessageDelayed(msg, 3000);
        } else {
            //???????????????????????????
//            if (stuAdapter.getTestPosition() == stuPairsList.size() - 1) {
//                //????????????????????????
//                allTestComplete();
//                return;
//            }
            continuousTestNext();
        }
    }

    /**
     * ?????????????????????
     */
    private void continuousTestNext() {
        baseHeight = 0;
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG && runUp == 0) {
            setBaseHeight(0);
        }
        for (int i = (stuAdapter.getTestPosition() + 1); i < stuPairsList.size(); i++) {

            //  ?????????????????? ???????????????????????????????????????
            List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                    (stuPairsList.get(i).getStudent().getStudentCode(), group.getId() + "");
            if (roundResultList == null || roundResultList.size() == 0 || roundResultList.size() < setTestCount()) {

                roundNo = roundResultList == null || roundResultList.size() == 0 ? 1 : roundResultList.size() + 1;


                stuAdapter.setTestPosition(i);
//                rvTestStu.scrollToPosition(i);
                if (testType == 1) {
                    isStop = true;
                    tvStartTest.setText("????????????");
                }
//                else {
                //???????????????????????????
                toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

//                }
                Message msg = new Message();
                msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                ledHandler.sendMessageDelayed(msg, 3000);
                Logger.i("addStudent:" + stuPairsList.get(i).getStudent().toString());
                Logger.i("addStudent:?????????????????????" + 1 + "?????????" + roundNo + "?????????");
                group.setIsTestComplete(2);
                DBManager.getInstance().updateGroup(group);
//                //??????????????????
//                resultList.clear();
//                resultList.addAll(Arrays.asList(stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult()));
//                testResultAdapter.notifyDataSetChanged();
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
            if (setTestCount() > roundNo) {
                roundNo++;
                //????????????????????????????????????????????????????????????????????????
                stuAdapter.setTestPosition(0);
                loopTestNext();
                return;
            } else {
                //????????????????????????
                allTestComplete();
                return;
            }
        }
        //????????????????????????????????????????????????????????????????????????
        stuAdapter.setTestPosition(stuAdapter.getTestPosition() + 1);
        loopTestNext();
    }

    /**
     * ???????????????????????????
     */
    private void loopTestNext() {
        baseHeight = 0;
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_MG && runUp == 0) {
            setBaseHeight(0);
        }
        //?????????????????? ????????????????????????????????? ???????????????????????????????????????
        for (int i = roundNo; i <= setTestCount(); i++) {
            for (int j = stuAdapter.getTestPosition(); j < stuPairsList.size(); j++) {
                if (TextUtils.isEmpty(stuPairsList.get(j).getTimeResult()[i - 1])) {
                    if (stuPairsList.get(j).isFullMark() && stuPairsList.get(j).getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                        continue;
                    }
                    roundNo = i;
//                    rvTestStu.scrollToPosition(j);
                    stuAdapter.setTestPosition(j);
                    if (testType == 1) {
                        isStop = true;
                        tvStartTest.setText("????????????");
                    }
//                    else {
                    toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                            String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));

//                    }
                    Message msg = new Message();
                    msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
                    ledHandler.sendMessageDelayed(msg, 3000);
                    Logger.i("????????????????????????" + stuPairsList.get(stuAdapter.getTestPosition()).getStudent());
//                    resultList.clear();
//                    resultList.addAll(Arrays.asList(stuPairsList.get(stuAdapter.getTestPosition()).getTimeResult()));
//                    testResultAdapter.notifyDataSetChanged();
                    group.setIsTestComplete(2);
                    DBManager.getInstance().updateGroup(group);
                    return;
                }
            }
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
        if (roundNo < setTestCount() && !pair.isFullMark()) {
            roundNo++;
            pair.getBaseDevice().setState(BaseDeviceState.STATE_NOT_BEGAIN);
            if (testType == 1) {
                isStop = true;
                tvStartTest.setText("????????????");
            } else {
                toastSpeak(String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), stuPairsList.get(stuAdapter.getTestPosition()).getStudent().getStudentName(), roundNo));


            }
            Message msg = new Message();
            msg.obj = stuPairsList.get(stuAdapter.getTestPosition());
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
        //????????????????????????
        toastSpeak("???????????????????????????????????????????????????");
        if (group.getIsTestComplete() != 1 &&
                SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN &&
                (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 ||SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP) &&
                SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printA4Result(this, group);
        }
        roundNo = 1;
        stuAdapter.setTestPosition(-1);
        stuAdapter.notifyDataSetChanged();
        isStop = true;
        tvStartTest.setText("????????????");
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
    }

    public void setScore(RangerResult result,BaseStuPair stuPair){
        calculation(result,setting,stuPair);
        updateTestResult(stuPair);
    }

    private void calculation(RangerResult result, RangerSetting rangerSetting,BaseStuPair stuPair) {
        int itemType = rangerSetting.getItemType();
        if (itemType == 2 || itemType == 3 || itemType == 4){ //???????????????
            double level1 = rangerSetting.getLevel1();
            double level2 = rangerSetting.getLevel2();
            Point jidian1 = RangerUtil.getPoint(level1,rangerSetting.getQd1_hor());
            Point jidian2 = RangerUtil.getPoint(level2,rangerSetting.getQd2_hor());
            double level = RangerUtil.level(result.getLevel_d(),result.getLevel_g(),result.getLevel_m());
            Point p = RangerUtil.getPoint(level,result.getResult());
            double length = RangerUtil.length(jidian1, jidian2, p);
            stuPair.setResult((int) length);
        }else if (itemType == 0 || itemType == 1){   //???????????????
            stuPair.setResult(result.getResult());
        }else if (itemType == 5 || itemType == 6 || itemType == 7 || itemType == 8){  //???????????????
            double dd = RangerUtil.level(result.getLevel_d(),result.getLevel_g(),result.getLevel_m());
            double inclination = RangerUtil.inclination(rangerSetting.getLevel(), dd);
            double length = RangerUtil.cosine(inclination, rangerSetting.getQd_hor(), result.getResult());
            stuPair.setResult((int) (length - rangerSetting.getRadius()));

        }
    }

    private static class LedHandler extends Handler {

        private WeakReference<BaseMoreTestActivity> mActivityWeakReference;

        public LedHandler(BaseMoreTestActivity activity) {
            mActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            BaseMoreTestActivity activity = mActivityWeakReference.get();
            if (activity.stuAdapter.getTestPosition() == -1) {
                return;
            }
            activity.setShowLed((BaseStuPair) msg.obj);
            activity.txtStuResult.setText("");
            if (activity.testType == 0) {
                activity.startTest(activity.stuPairsList.get(activity.stuAdapter.getTestPosition()));
            }
            //??????????????????
            activity.resultList.clear();
            activity.resultList.addAll(Arrays.asList(activity.stuPairsList.get(activity.stuAdapter.getTestPosition()).getTimeResult()));
            activity.testResultAdapter.notifyDataSetChanged();
            activity.rvTestStu.scrollToPosition(activity.stuAdapter.getTestPosition());
            activity.stuAdapter.notifyDataSetChanged();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
        Intent serverIntent = new Intent(this, UploadService.class);
        stopService(serverIntent);
    }
}
