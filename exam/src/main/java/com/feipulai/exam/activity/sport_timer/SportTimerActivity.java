package com.feipulai.exam.activity.sport_timer;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.TimerTask;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.adapter.PartResultAdapter;
import com.feipulai.exam.activity.sport_timer.adapter.SportTestCountAdapter;
import com.feipulai.exam.activity.sport_timer.adapter.TimeResultAdapter;
import com.feipulai.exam.activity.sport_timer.bean.DeviceState;
import com.feipulai.exam.activity.sport_timer.bean.InitRoute;
import com.feipulai.exam.activity.sport_timer.bean.SportTestResult;
import com.feipulai.exam.activity.sport_timer.bean.SportTimeResult;
import com.feipulai.exam.activity.sport_timer.bean.SportTimerSetting;
import com.feipulai.exam.activity.sport_timer.pair.SportPairActivity;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_TEXT;

/**
 * 根据实测知道频段换成25最好
 */
public class SportTimerActivity extends BaseTitleActivity implements BaseAFRFragment.onAFRCompareListener,
        IndividualCheckFragment.OnIndividualCheckInListener, SportContract.SportView, TimerTask.TimeUpdateListener {

    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.iv_portrait)
    ImageView ivPortrait;
    @BindView(R.id.tv_studentCode)
    TextView tvStudentCode;
    @BindView(R.id.tv_studentName)
    TextView tvStudentName;
    @BindView(R.id.tv_gender)
    TextView tvGender;
    @BindView(R.id.tv_grade)
    TextView tvGrade;
    @BindView(R.id.ll_stu_detail)
    LinearLayout llStuDetail;
    @BindView(R.id.ll_individual_check)
    LinearLayout llIndividualCheck;
    @BindView(R.id.tv_pair)
    TextView tvPair;
    @BindView(R.id.img_AFR)
    ImageView imgAFR;
    @BindView(R.id.cb_device_state)
    CheckBox cbDeviceState;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.tv_foul)
    TextView tvFoul;
    @BindView(R.id.tv_inBack)
    TextView tvInBack;
    @BindView(R.id.tv_abandon)
    TextView tvAbandon;
    @BindView(R.id.tv_normal)
    TextView tvNormal;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    //    @BindView(R.id.txt_continue_run)
//    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.view_list_head)
    LinearLayout viewListHead;
    @BindView(R.id.view_part_result)
    RelativeLayout viewPartResult;
    @BindView(R.id.list_item)
    RecyclerView listItem;
    @BindView(R.id.rv_region_mark)
    RecyclerView rvRegionMark;
    @BindView(R.id.rl_group)
    LinearLayout rlGroup;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_end_result)
    TextView endResult;
    @BindView(R.id.tv_part_result)
    TextView partResult;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;
    @BindView(R.id.tv_del)
    TextView tvDelete;
    @BindView(R.id.frame_camera)
    FrameLayout frameCamera;


    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private IndividualCheckFragment individualCheckFragment;
    private SportPresent sportPresent;
    private SportTimerSetting setting;
    private DeviceDialog deviceDialog;
    private List<DeviceState> deviceStates;
    private TimeResultAdapter timeResultAdapter;
    private SportTestCountAdapter testCountAdapter;
    private PartResultAdapter partResultAdapter;
    private int partSelect;
    private TestState testState;
    private StuDevicePair pair = new StuDevicePair();
    private StudentItem mStudentItem;
    private int roundNo = 1;
    private int receiveTime = 0;
    private int initTime;
    private List<SportTestResult> testResults = new ArrayList<>();//保存成绩
    private int testNum;
    private final int UPDATE_STOP_ENABLE = 0XF1;
    private final int UPDATE_RESULT = 0XF2;
    private final int UPDATE_ON_STOP = 0XF3;
    private final int UPDATE_ON_WAIT = 0XF4;
    private final int UPDATE_ON_TEXT = 0XF5;
    private TimerTask timerTask;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sport_timer;
    }

    @Override
    protected void initData() {
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }

        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setResultView(lvResults);
        individualCheckFragment.setOnIndividualCheckInListener(this);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);

        setting = SharedPrefsUtil.loadFormSource(this, SportTimerSetting.class);
        if (setting == null)
            setting = new SportTimerSetting();
        deviceStates = new ArrayList<>();
        TestConfigs.sCurrentItem.setDigital(setting.getDigital() + 1);
        TestConfigs.sCurrentItem.setCarryMode(setting.getCarryMode() + 1);
        for (int i = 0; i < setting.getDeviceCount(); i++) {
            DeviceState deviceState = new DeviceState();
            deviceState.setDeviceId(i + 1);
            deviceState.setDeviceState(0);
            deviceStates.add(deviceState);
        }

        endResult.setSelected(true);
        partResult.setSelected(false);
        viewPartResult.setVisibility(View.GONE);

        testNum = TestConfigs.sCurrentItem.getTestNum();
        if (testNum == 0) {
            testNum = setting.getTestTimes() > TestConfigs.getMaxTestCount(this) ? setting.getTestTimes() : TestConfigs.getMaxTestCount(this);
        }
        List<String> testTimes = new ArrayList<>();
        for (int i = 0; i < testNum; i++) {
            testTimes.add(String.format(Locale.CHINA, "轮次%d", i + 1));

            SportTestResult sportResult = new SportTestResult();
            sportResult.setRound(i + 1);
            sportResult.setSportTimeResults(new ArrayList<SportTimeResult>());
            testResults.add(sportResult);
        }
        listItem.setLayoutManager(new LinearLayoutManager(this));
        testCountAdapter = new SportTestCountAdapter(testTimes);
        listItem.setAdapter(testCountAdapter);
        testCountAdapter.setSelectPosition(0);

        timeResultAdapter = new TimeResultAdapter(testResults);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(timeResultAdapter);


        partResultAdapter = new PartResultAdapter(testResults.get(roundNo - 1).getSportTimeResults());
        rvRegionMark.setLayoutManager(new LinearLayoutManager(this));
        rvRegionMark.setAdapter(partResultAdapter);
        txtIllegalReturn.setEnabled(false);
        txtStopTiming.setEnabled(false);



        partResultAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                partResultAdapter.setSelectPosition(position);
                partResultAdapter.notifyDataSetChanged();
                partSelect = position;
            }
        });

        testCountAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                partResultAdapter.replaceData(testResults.get(position).getSportTimeResults());
                testCountAdapter.setSelectPosition(position);
                testCountAdapter.notifyDataSetChanged();
            }
        });

        setTxtEnable(false);
        testState = TestState.UN_STARTED;
        myRunnable = new MyRunnable();
        new Thread(myRunnable).start();
    }

    private MyRunnable myRunnable;
    private class MyRunnable implements Runnable {

        @Override
        public void run() {
            timerTask = new TimerTask(SportTimerActivity.this,100);
            timerTask.keepTime();
            sportPresent = new SportPresent(SportTimerActivity.this, setting.getDeviceCount());
            sportPresent.rollConnect();
            sportPresent.setContinueRoll(true);
        }
    }

    private void setTxtEnable(boolean enable) {
        penalize(enable);
        tvDelete.setEnabled(enable);
        tvPrint.setEnabled(enable);
        tvConfirm.setEnabled(enable);
        txtFinishTest.setEnabled(enable);
    }

    /**
     * 判罚可见与否
     * @param enable
     */
    private void penalize(boolean enable) {
        tvFoul.setEnabled(enable);
        tvInBack.setEnabled(enable);
        tvAbandon.setEnabled(enable);
        tvNormal.setEnabled(enable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int frequency = SettingHelper.getSystemSetting().getUseChannel();
        RadioChannelCommand command = new RadioChannelCommand(frequency);
        LogUtils.serial("切频指令" + StringUtility.bytesToHexString(command.getCommand()) + "---");
        RadioManager.getInstance().sendCommand(new ConvertCommand(command));
        if (sportPresent!=null){
            sportPresent.setContinueRoll(true);
        }
        setPartResult();
    }

    /**
     * 设置自定义路线
     */
    private void setPartResult() {
        String route = setting.getInitRoute();
        if (!TextUtils.isEmpty(route)) {
            Gson gson = new Gson();
            List<InitRoute> initRoutes = gson.fromJson(route, new TypeToken<List<InitRoute>>() {
            }.getType());
            if (initRoutes != null && initRoutes.size() > 0) {
                for (int i = 0; i < testNum; i++) {
                    testResults.get(i).getSportTimeResults().clear();
                }
                for (InitRoute initRoute : initRoutes) {
                    if (!TextUtils.isEmpty(initRoute.getDeviceName())) {
                        for (int i = 0; i < testNum; i++) {
                            SportTimeResult timeResult = new SportTimeResult();
                            timeResult.setRouteName(initRoute.getIndex() + "");
                            testResults.get(i).getSportTimeResults().add(timeResult);
                        }
                    }
                }
                partResultAdapter.notifyDataSetChanged();
            }
        }
        if (roundNo<testResults.size()){
            testCountAdapter.setSelectPosition(roundNo - 1);
            testCountAdapter.notifyDataSetChanged();
            partResultAdapter.replaceData(testResults.get(roundNo - 1).getSportTimeResults());
        }else {
            testCountAdapter.setSelectPosition(0);
            testCountAdapter.notifyDataSetChanged();
            partResultAdapter.replaceData(testResults.get(0).getSportTimeResults());
        }

    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addRightText("项目设置", new View.OnClickListener() {
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
        if (testState != TestState.UN_STARTED){
            toastSpeak("测试中不可设置");
            return;
        }
        IntentUtil.gotoActivityForResult(this, SportSettingActivity.class, 1);
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
//                    InteractUtils.toastSpeak(SportTimerActivity.this, "该考生不存在");
//                    return;
//                } else {
//                    afrFrameLayout.setVisibility(View.GONE);
//                }
//                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//                if (studentItem == null) {
//                    InteractUtils.toastSpeak(SportTimerActivity.this, "无此项目");
//                    return;
//                }
//                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
//                if (results != null && results.size() >= TestConfigs.getMaxTestCount(SportTimerActivity.this)) {
//                    InteractUtils.toastSpeak(SportTimerActivity.this, "该考生已测试");
//                    return;
//                }
//                // 可以直接检录
//                onIndividualCheckIn(student, studentItem, results);
            }
        });
    }


    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        if (student != null)
            LogUtils.all("运动计时检入到学生:" + student.toString());
        if (studentItem != null)
            LogUtils.all("运动计时检入到学生StudentItem:" + studentItem.toString());
        if (results != null)
            LogUtils.all("运动计时检入到学生成绩:" + results.size() + "----" + results.toString());
        if (testState == TestState.UN_STARTED) {
            pair.setStudent(student);
            mStudentItem = studentItem;
            int testNo;
            if (results == null || results.size() == 0) {
                //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
                RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
                testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
                if (student != null)
                    LogUtils.operation("运动计时该学生未测试:" + student.getStudentCode() + ",testNo =  " + testNo);
            } else {
                //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
                RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
                testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo();
                if (student != null)
                    LogUtils.operation("运动计时该学生有成绩:" + student.getStudentCode() + ",testNo = " + testNo);
            }
            roundNo = results.size() + 1;
            LogUtils.operation("运动计时当前轮次 roundNo = " + roundNo);
            sportPresent.showStudent(llStuDetail, student, testNo);
            presetResult(student, testNo);
            timeResultAdapter.notifyDataSetChanged();
            sportPresent.setShowLed(pair.getStudent(),roundNo);
        }
    }

    /**
     * 预设置成绩
     */
    private void presetResult(Student student, int testNo) {
        testResults.clear();
        if (null == student)
            return;
        for (int i = 0; i < testNum; i++) {
            RoundResult roundResult = DBManager.getInstance().queryRoundByRoundNo(student.getStudentCode(), testNo, i + 1);
            if (roundResult == null) {
                testResults.add(new SportTestResult(i + 1, -1, -1, new ArrayList<SportTimeResult>()));
            } else {
                testResults.add(new SportTestResult(i + 1, roundResult.getResult(), roundResult.getResultState(), new ArrayList<SportTimeResult>()));

            }

        }
        setPartResult();
        tvResult.setText("");
    }

    @OnClick({R.id.tv_pair, R.id.txt_waiting, R.id.cb_device_state, R.id.tv_end_result, R.id.tv_part_result,
            R.id.tv_del, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.txt_illegal_return,
            R.id.txt_stop_timing, R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test, R.id.img_AFR})
    public void btnOnClick(View v) {
        switch (v.getId()) {
            case R.id.tv_pair:
                if (testState != TestState.UN_STARTED){
                    toastSpeak("测试中,不允许配对");
                    return;
                }
                startActivity(new Intent(this, SportPairActivity.class));
                break;
            case R.id.txt_waiting:
                if (pair.getStudent()== null){
                    ToastUtils.showShort("当前学生为空");
                    return;
                }
                LogUtils.operation("等待计时");
                Logger.i("运动计时测试次数"+roundNo);
                if (roundNo > testNum) {
                    toastSpeak("已超过测试次数");
                    return;
                }
                if (testState == TestState.UN_STARTED && cbDeviceState.isChecked() && pair.getStudent() != null) {
                    sportPresent.waitStart();
                    testState = TestState.WAIT_RESULT;
                } else {
                    ToastUtils.showShort("当前设备不可用或当前学生为空");
                }
                mHandler.sendEmptyMessage(UPDATE_ON_WAIT);
                break;
            case R.id.cb_device_state:
                deviceDialog = new DeviceDialog(this, deviceStates);
                deviceDialog.show();
                cbDeviceState.setChecked(!cbDeviceState.isChecked());
                break;
            case R.id.tv_end_result:
                endResult.setSelected(true);
                partResult.setSelected(false);
                viewListHead.setVisibility(View.VISIBLE);
                viewPartResult.setVisibility(View.GONE);
                break;
            case R.id.tv_part_result:
                endResult.setSelected(false);
                partResult.setSelected(true);
                viewListHead.setVisibility(View.GONE);
                viewPartResult.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_del:
                deleteDialog();
                break;
            case R.id.tv_foul:
                testResults.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_FOUL);
                tvResult.setText("犯规");
                break;
            case R.id.tv_inBack:
                testResults.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_BACK);
                tvResult.setText("中退");
                break;
            case R.id.tv_abandon:
                testResults.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_WAIVE);
                tvResult.setText("放弃");
                break;
            case R.id.tv_normal:
                testResults.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_NORMAL);
                tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(testResults.get(roundNo - 1).getResult()));
                break;
            case R.id.txt_illegal_return:
                LogUtils.operation("违规返回");
                if (testState == TestState.WAIT_RESULT) {
                    timerTask.stopKeepTime();
                    sportPresent.setDeviceStateStop();
                    receiveTime = 0;
                    testState = TestState.UN_STARTED;
                    setTxtEnable(false);
                    txtWaiting.setEnabled(true);
                    presetResult(pair.getStudent(),roundNo);
                    tvResult.setText("");
                }
                break;
            case R.id.txt_stop_timing:
                LogUtils.operation("停止计时");
                timerTask.stopKeepTime();
                if (testState == TestState.WAIT_RESULT) {
                    sportPresent.setDeviceStateStop();
                    testState = TestState.RESULT_CONFIRM;
                    setTxtEnable(true);
                    receiveTime = 0;
                }
                timeResultAdapter.notifyDataSetChanged();
                sportPresent.getDeviceState();
                break;
            case R.id.tv_print:
                LogUtils.operation("打印");
                if (testState == TestState.UN_STARTED) {
                    printResult();
                } else {
                    toastSpeak("测试成绩未保存不可打印");
                }

                break;
            case R.id.tv_confirm:
                LogUtils.operation("成绩确认");
//                resultMap.put(roundNo,partResultList);
                if (testState == TestState.RESULT_CONFIRM) {
                    sportPresent.clearLed(0);
                    tvResult.setText("");
                    txtDeviceStatus.setText("空闲");
                    tvDelete.setEnabled(false);
                    txtWaiting.setEnabled(true);
                    testState = TestState.UN_STARTED;
                    if (pair.getCurrentRoundNo() != 0){
                        sportPresent.saveResult(pair.getCurrentRoundNo(), mStudentItem, testResults.get(roundNo - 1),true);
                        pair.setCurrentRoundNo(0);
                    }else {
                        sportPresent.saveResult(roundNo, mStudentItem, testResults.get(roundNo - 1),false);
                    }
                    sportPresent.showStuInfo(llStuDetail, pair.getStudent(), testResults);
                    if (roundNo < testNum) {
                        partResultAdapter.replaceData(testResults.get(roundNo).getSportTimeResults());
                        testCountAdapter.setSelectPosition(roundNo);
                        testCountAdapter.notifyDataSetChanged();
                    }else {
                        if (SettingHelper.getSystemSetting().isAutoPrint()){
                            printResult();
                        }
                    }
                    roundNo++;
                    tvConfirm.setEnabled(false);
                    penalize(false);
                }
                if (mStudentItem.getExamType() == 2){
                    txtFinishTest.setEnabled(false);
                    txtWaiting.setEnabled(true);
                    InteractUtils.showStuInfo(llStuDetail, null, null);
                    tvResult.setText("请检录");
                    for (SportTestResult testResult : testResults) {
                        testResult.setResult(-1);
                        List<SportTimeResult> sportTimeResults = testResult.getSportTimeResults();
                        for (SportTimeResult result : sportTimeResults) {
                            result.setResult(-1);
                            result.setPartResult(-1);
                            result.setReceiveIndex(-1);
                        }
                    }
                    testCountAdapter.notifyDataSetChanged();
                    partResultAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.txt_finish_test:
                LogUtils.operation("结束测试");
                if (testState != TestState.UN_STARTED) {
                    toastSpeak("测试成绩未保存不可结束");
                }else {
                    txtFinishTest.setEnabled(false);
                    txtWaiting.setEnabled(true);
                    InteractUtils.showStuInfo(llStuDetail, null, null);
                    tvResult.setText("请检录");
                    for (SportTestResult testResult : testResults) {
                        testResult.setResult(-1);
                        List<SportTimeResult> sportTimeResults = testResult.getSportTimeResults();
                        for (SportTimeResult result : sportTimeResults) {
                            result.setResult(-1);
                            result.setPartResult(-1);
                            result.setReceiveIndex(-1);
                        }
                    }
                    testCountAdapter.notifyDataSetChanged();
                    partResultAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.img_AFR:
                showAFR();
                break;
        }
    }

    private void printResult() {
        List<Student> students = new ArrayList<>();
        students.add(pair.getStudent());
        List<RoundResult> roundResults = DBManager.getInstance().queryResultsByStuItem(mStudentItem);
        Map<Student, List<RoundResult>> map = new HashMap<>();
        map.put(pair.getStudent(), roundResults);
        Map<Student, Integer> m = new HashMap<>();
        m.put(pair.getStudent(), roundNo);
        sportPresent.print(students, this, map, m);
    }

    private void deleteDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("是否确认删除分段成绩")
                .setConfirmText("确认").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                if (testResults.size() <roundNo && testResults.get(roundNo - 1).getSportTimeResults().get(partSelect).getPartResult() > 0) {
                    Logger.i("删除分段成绩" + testResults.get(roundNo - 1).getSportTimeResults().get(partSelect).toString());
                    testResults.get(roundNo - 1).getSportTimeResults().remove(partSelect);
                    partSelect = -1;
                    partResultAdapter.setSelectPosition(partSelect);
                    partResultAdapter.notifyDataSetChanged();
                } else {
                    toastSpeak("此段成绩为空，不能删除");
                }
            }
        }).setCancelText("取消").setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();

            }
        }).show();

    }


    @Override
    public void updateDeviceState(final int deviceId, final int state) {
        if (deviceId > deviceStates.size()){
            return;
        }
        if (deviceStates.get(deviceId - 1).getDeviceState() != state) {
            deviceStates.get(deviceId - 1).setDeviceState(state);
        }

        boolean flag = false;
        for (DeviceState deviceState : deviceStates) {
            if (deviceState.getDeviceState() == 0) {//1 2为连接正常
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
//        mHandler.sendEmptyMessage(UPDATE_ON_WAIT);
    }

    private int lastTime;//上一次接收时间
    @Override
    public void receiveResult(SportResult sportResult) {
        if (receiveTime == 0 && sportResult.getDeviceId() != 1) {
            toastSpeak("不是第一个启动，请重新检查");
        } else {
            if (sportResult.getDeviceId() == 1 && sportResult.getSumTimes() == 1) {
                lastTime = 0;
                initTime = sportResult.getLongTime();
                mHandler.sendEmptyMessage(UPDATE_STOP_ENABLE);
                timerTask.setStart();
                sportPresent.clearLed(0);
            }
            if (receiveTime >= testResults.get(roundNo - 1).getSportTimeResults().size())
                return;
            if ((sportResult.getLongTime()-initTime) <lastTime){
                return;
            }
            final SportTimeResult timeResult = partResultAdapter.getData().get(receiveTime);
            timeResult.setPartResult(sportResult.getLongTime() - initTime);
            lastTime = sportResult.getLongTime() - initTime;
            timeResult.setReceiveIndex(sportResult.getDeviceId());
            int routeName;
            if (!TextUtils.isEmpty(timeResult.getRouteName())) {
                routeName = Integer.parseInt(timeResult.getRouteName());
            } else {
                routeName = -1;
            }
            timeResult.setResultState(sportResult.getDeviceId() == routeName ? RoundResult.RESULT_STATE_NORMAL : RoundResult.RESULT_STATE_FOUL);
            testResults.get(roundNo - 1).setResult(timeResult.getPartResult());
            testResults.get(roundNo - 1).setResultState(testResults.get(roundNo - 1).getResultState() ==
                    RoundResult.RESULT_STATE_FOUL ? RoundResult.RESULT_STATE_FOUL : timeResult.getResultState());
            mHandler.sendEmptyMessage(UPDATE_RESULT);
            receiveTime++;
        }
    }

    @Override
    public void getDeviceStop() {
        if (testState == TestState.WAIT_RESULT) {
            testState = TestState.RESULT_CONFIRM;
            sportPresent.setRunState(0);
            mHandler.sendEmptyMessage(UPDATE_ON_STOP);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sportPresent.setContinueRoll(false);
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
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU) {
            Student student = (Student) baseEvent.getData();
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
            onIndividualCheckIn(student, studentItem, new ArrayList<RoundResult>());
        }else if (baseEvent.getTagInt() == UPDATE_ON_TEXT){
            tvResult.setText(ResultDisplayUtils.getStrResultForDisplay((Integer) baseEvent.getData(), false));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sportPresent.presentRelease();
        timerTask.release();
    }

    @Override
    public void finish() {
        if (testState != TestState.UN_STARTED){
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_STOP_ENABLE:
                    txtStopTiming.setEnabled(true);
                    break;
                case UPDATE_RESULT:
//                    String s =  testResults.get(roundNo - 1).getResultState() == RoundResult.RESULT_STATE_NORMAL?
//                            ResultDisplayUtils.getStrResultForDisplay(testResults.get(roundNo - 1).getResult()):"犯规";
                    partResultAdapter.notifyDataSetChanged();
//                    tvResult.setText(s);
                    break;
                case UPDATE_ON_STOP:
                    txtStopTiming.setEnabled(false);
                    txtIllegalReturn.setEnabled(false);
                    txtDeviceStatus.setText("停止计时");
                    break;
                case UPDATE_ON_WAIT:
                    txtWaiting.setEnabled(false);
                    txtIllegalReturn.setEnabled(true);
                    sportPresent.setRunState(1);
                    testState = TestState.WAIT_RESULT;
                    setTxtEnable(false);
                    if (testResults.size() > 0){
                        testResults.get(roundNo - 1).setTestTime(System.currentTimeMillis() + "");
                    }
                    receiveTime = 0;
                    txtDeviceStatus.setText("计时");
                    break;
//                case UPDATE_ON_TEXT:
//                    tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(msg.arg1, false));
//                    break;

            }
            return false;
        }
    });

    @Override
    public void onTimeTaskUpdate(int time) {
//        Message message = mHandler.obtainMessage();
//        message.what = UPDATE_ON_TEXT;
//        message.arg1 = time;
//        mHandler.sendMessage(message);


        EventBus.getDefault().post(new BaseEvent(time,UPDATE_ON_TEXT));
//        tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(time, false));
        onTimeIOTaskUpdate(time);
    }


    public void onTimeIOTaskUpdate(int time) {
        if (testState == TestState.WAIT_RESULT){
            String formatTime ;
            if (time<60*60*1000){
                formatTime = DateUtil.formatTime1(time, "mm:ss.SSS");
            }else {
                formatTime = DateUtil.formatTime1(time, "HH:mm:ss");
            }
            sportPresent.showLedString(formatTime);
        }
    }


    @Override
    public void setRoundNo(Student student, int roundNo) {
        Student student1 = pair.getStudent();
        if (student1 != null && student1.getStudentCode().equals(student.getStudentCode())){
            pair.setCurrentRoundNo(roundNo);
        }
    }

}
