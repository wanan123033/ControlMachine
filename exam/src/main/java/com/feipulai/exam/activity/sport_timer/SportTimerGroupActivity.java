package com.feipulai.exam.activity.sport_timer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.TimerTask;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.sport_timer.adapter.PartResultAdapter;
import com.feipulai.exam.activity.sport_timer.adapter.SportTestCountAdapter;
import com.feipulai.exam.activity.sport_timer.adapter.TimeResultAdapter;
import com.feipulai.exam.activity.sport_timer.bean.DeviceState;
import com.feipulai.exam.activity.sport_timer.bean.InitRoute;
import com.feipulai.exam.activity.sport_timer.bean.SportTestResult;
import com.feipulai.exam.activity.sport_timer.bean.SportTimeResult;
import com.feipulai.exam.activity.sport_timer.bean.SportTimerSetting;
import com.feipulai.exam.adapter.VolleyBallGroupStuAdapter;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.PrintResultUtil;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class SportTimerGroupActivity extends BaseTitleActivity implements SportContract.SportView, BaseQuickAdapter.OnItemClickListener, TimerTask.TimeUpdateListener {
    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestingPairs;
    @BindView(R.id.tv_group_name)
    TextView tvGroupName;
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
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.tv_end_result)
    TextView endResult;
    @BindView(R.id.tv_part_result)
    TextView partResult;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.view_list_head)
    LinearLayout viewListHead;
    @BindView(R.id.list_item)
    RecyclerView listItem;
    @BindView(R.id.tv_del)
    TextView tvDelete;
    @BindView(R.id.rv_region_mark)
    RecyclerView rvRegionMark;
    @BindView(R.id.view_part_result)
    RelativeLayout viewPartResult;
    @BindView(R.id.rl_group)
    LinearLayout rlGroup;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;

    private SportPresent sportPresent;
    private SportTimerSetting setting;
    private DeviceDialog deviceDialog;
    private List<DeviceState> deviceStates;
    private TimeResultAdapter resultAdapter;
    private SportTestCountAdapter testCountAdapter;
    private PartResultAdapter partResultAdapter;
    private int partSelect;
    private TestState testState;
    private int roundNo = 1;
    private int receiveTime = 0;
    private int initTime;
    private List<SportTestResult> resultList = new ArrayList<>();//保存成绩
    private int testNum;
    private Group group;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private boolean startTest = true;
    private final int UPDATE_STOP = 0XF1;
    private final int UPDATE_RESULT = 0XF2;
    private final int UPDATE_ON_STOP = 0XF3;
    private final int UPDATE_ON_WAIT = 0XF4;
    private final int UPDATE_ON_TEXT = 0XF5;
    private TimerTask timerTask;
    private List<BaseStuPair> stuPairs;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_sport_timer;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SportTimerSetting.class);
        if (setting == null)
            setting = new SportTimerSetting();
        TestConfigs.sCurrentItem.setDigital(setting.getDigital() + 1);
        TestConfigs.sCurrentItem.setCarryMode(setting.getCarryMode() + 1);
        deviceStates = new ArrayList<>();
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
            resultList.add(sportResult);
        }
        listItem.setLayoutManager(new LinearLayoutManager(this));
        testCountAdapter = new SportTestCountAdapter(testTimes);
        listItem.setAdapter(testCountAdapter);
        testCountAdapter.setSelectPosition(0);

        resultAdapter = new TimeResultAdapter(resultList);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(resultAdapter);


        partResultAdapter = new PartResultAdapter(resultList.get(roundNo - 1).getSportTimeResults());
        rvRegionMark.setLayoutManager(new LinearLayoutManager(this));
        rvRegionMark.setAdapter(partResultAdapter);
        txtIllegalReturn.setEnabled(false);
        txtStopTiming.setEnabled(false);

        sportPresent = new SportPresent(this, setting.getDeviceCount());
        sportPresent.rollConnect();

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
                if (resultList.get(position).getResult()!= -1){
                    toastSpeak("该轮次已测试");
                    return;
                }
                partResultAdapter.replaceData(resultList.get(roundNo - 1).getSportTimeResults());
                testCountAdapter.setSelectPosition(position);
                testCountAdapter.notifyDataSetChanged();
            }
        });

        setTxtEnable(false);
        testState = TestState.UN_STARTED;

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
        LogUtils.operation("运动计时获取到分组学生:" + pairs.size() + "---" + pairs.toString());
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setOnItemClickListener(this);

        firstCheckTest();
        int frequency = SettingHelper.getSystemSetting().getUseChannel();
        RadioChannelCommand command = new RadioChannelCommand(frequency);
        LogUtils.normal(command.getCommand().length + "---" + StringUtility.bytesToHexString(command.getCommand()) + "---切频指令");
        sportPresent = new SportPresent(this,setting.getDeviceCount());
        sportPresent.rollConnect();
        sportPresent.setContinueRoll(true);
        showGroupLed("");
        timerTask = new TimerTask(this,100);
        timerTask.keepTime();
    }


    private void showGroupLed(String result) {
        if (pairs.size()> (position()+1)){
            sportPresent.displayGroupLED(pairs.get(position()).getStudent(),roundNo,group.getId(),pairs.get((position()+1)).getStudent().getStudentName(),result);
        }else {
            sportPresent.displayGroupLED(pairs.get(position()).getStudent(),roundNo,group.getId(),"",result);
        }
    }

    /*private void displayCheckedInLED() {
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
            ledManager.showString(hostId, "已有成绩:" + displayResult, 2, 3, false, true);
        }
    }*/

    //左边点击考生
    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

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

    /**
     * 设置自定义路线
     */
    private void setPartRoutes() {
        String route = setting.getInitRoute();
        if (!TextUtils.isEmpty(route)) {
            Gson gson = new Gson();
            List<InitRoute> initRoutes = gson.fromJson(route, new TypeToken<List<InitRoute>>() {
            }.getType());
            if (initRoutes != null && initRoutes.size() > 0) {
                for (int i = 0; i < testNum; i++) {
                    resultList.get(i).getSportTimeResults().clear();
                }
                for (InitRoute initRoute : initRoutes) {
                    if (!TextUtils.isEmpty(initRoute.getDeviceName())) {
                        for (int i = 0; i < testNum; i++) {
                            SportTimeResult timeResult = new SportTimeResult();
                            timeResult.setRouteName(initRoute.getIndex() + "");
                            resultList.get(i).getSportTimeResults().add(timeResult);
                        }
                    }
                }
                partResultAdapter.notifyDataSetChanged();
            }
        }
        testCountAdapter.setSelectPosition(roundNo - 1);
        testCountAdapter.notifyDataSetChanged();
        if (roundNo>testNum)
            return;
        partResultAdapter.replaceData(resultList.get(roundNo - 1).getSportTimeResults());

    }

    @OnClick({R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.txt_waiting, R.id.txt_illegal_return,
            R.id.txt_stop_timing, R.id.tv_end_result, R.id.tv_part_result, R.id.tv_del, R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test,R.id.cb_device_state})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_foul:
                resultList.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_FOUL);
                tvResult.setText("犯规");
                break;
            case R.id.tv_inBack:
                resultList.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_BACK);
                tvResult.setText("中退");
                break;
            case R.id.tv_abandon:
                resultList.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_WAIVE);
                tvResult.setText("放弃");
                break;
            case R.id.tv_normal:
                resultList.get(roundNo-1).setResultState(RoundResult.RESULT_STATE_NORMAL);
                tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(resultList.get(roundNo - 1).getResult()));
                break;
            case R.id.txt_waiting:
                Logger.i("运动计时测试次数>>>>>>>>>等待"+roundNo);
                showGroupLed("");
                if (roundNo > testNum) {
                    toastSpeak("已超过测试次数");
                    return;
                }
                if (testState == TestState.UN_STARTED && cbDeviceState.isChecked()) {
                    sportPresent.waitStart();
                } else {
                    toastSpeak("当前设备不可用或当前学生为空");
                }
                break;
            case R.id.txt_illegal_return:
                if (testState == TestState.WAIT_RESULT) {
                    timerTask.stopKeepTime();
                    sportPresent.setDeviceStateStop();
                    receiveTime = 0;
                    testState = TestState.UN_STARTED;
                    setTxtEnable(false);
                    txtWaiting.setEnabled(true);
                    presetResult();
                    tvResult.setText("");
                }
                break;
            case R.id.txt_stop_timing:
                timerTask.stopKeepTime();
                if (testState == TestState.WAIT_RESULT) {
                    sportPresent.setDeviceStateStop();
                    setTxtEnable(true);
                }
                resultAdapter.notifyDataSetChanged();
                sportPresent.getDeviceState();

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
            case R.id.tv_print:
                showPrintDialog();
                break;
            case R.id.tv_confirm:
                startTest = false;
                if (testState == TestState.RESULT_CONFIRM) {
                    txtDeviceStatus.setText("空闲");
                    tvResult.setText("");
                    tvDelete.setEnabled(false);
                    txtWaiting.setEnabled(true);
                    testState = TestState.UN_STARTED;
                    Student stu = pairs.get(position()).getStudent();
                    if (pairs.get(position()).getCurrentRoundNo() != 0){
                        sportPresent.saveGroupResult(stu, resultList.get(roundNo - 1).getResult(),resultList.get(roundNo - 1).getResultState(),
                                pairs.get(position()).getCurrentRoundNo(),group, resultList.get(roundNo - 1).getTestTime(),true);
                        pairs.get(position()).setCurrentRoundNo(0);
                        List<BaseStuPair> stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
                        if (stuPairs != null){
                            for (BaseStuPair pp : stuPairs){
                                if (pp.getStudent().getStudentCode().equals(pairs.get(position()).getStudent().getStudentCode()))
                                    pp.setRoundNo(0);
                            }
                        }
                    }else {
                        sportPresent.saveGroupResult(stu, resultList.get(roundNo - 1).getResult(), resultList.get(roundNo - 1).getResultState(),
                                roundNo, group, resultList.get(roundNo - 1).getTestTime(),false);
                    }
                    List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(stu.getStudentCode());
                    if (results != null) {
                        TestCache.getInstance().getResults().put(stu, results);
                    }
                    showGroupLed(ResultDisplayUtils.getStrResultForDisplay(resultList.get(roundNo - 1).getResult()));
//                    sportPresent.showStuInfo(llStuDetail, pair.getStudent(), testResults);
                    if (roundNo <= testNum) {
                        partResultAdapter.replaceData(resultList.get(roundNo - 1).getSportTimeResults());
                        testCountAdapter.setSelectPosition(roundNo - 1);
                        testCountAdapter.notifyDataSetChanged();
                        if (setting.getGroupType() == 0){
                            loopTestNext();
                        }else {
                            if (roundNo == testNum){
                                roundNo++;
                            }
                            continuousTest();
                        }
                    }
//                    else {
//                        if (setting.getGroupType() == 1){
//                            roundNo++;
//                            continuousTest();
//                        }
//                    }
                    tvConfirm.setEnabled(false);
                    penalize(false);
                }
                StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),stuPairs.get(position()).getStudent().getStudentCode());
                if (studentItem.getExamType()== 2){
                    //是否测试到最后一位
                    if (position() == pairs.size() - 1) {
                        firstCheckTest();
                    } else {
                        continuousTestNext();
                    }
                }
                break;
            case R.id.txt_finish_test:
                if (testState == TestState.UN_STARTED){
                    finish();
                }
                break;
            case R.id.cb_device_state:
                deviceDialog = new DeviceDialog(this, deviceStates);
                deviceDialog.show();
                cbDeviceState.setChecked(!cbDeviceState.isChecked());
                break;
        }
    }

    private void deleteDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("是否确认删除分段成绩")
                .setConfirmText("确认").setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                if (resultList.size() <roundNo && resultList.get(roundNo - 1).getSportTimeResults().get(partSelect).getPartResult() > 0) {
                    Logger.i("删除分段成绩" + resultList.get(roundNo - 1).getSportTimeResults().get(partSelect).toString());
                    resultList.get(roundNo - 1).getSportTimeResults().remove(partSelect);
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
                                        TestConfigs.getMaxTestCount(SportTimerGroupActivity.this), testCache.getTrackNoMap());
                                break;
                        }
                    }
                }).create().show();
    }

    /**
     * 连续测试
     */
    private void continuousTest() {
        if (roundNo < TestConfigs.getMaxTestCount(this)) {
            //是否存在可以测试位置
            if (isExistTestPlace()) {
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo+1),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo+1));
                LogUtils.operation("运动计时考生:" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + roundNo+1 + "轮测试");
                presetResult();
            } else {
                continuousTestNext();
            }
        } else {
            //是否测试到最后一位
            if (position() == pairs.size() - 1) {
                firstCheckTest();
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

            if (!isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                continue;
            }
            stuPairAdapter.setTestPosition(i);
            rvTestingPairs.scrollToPosition(i);
            presetResult();
            //最后一次测试的成绩
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            LogUtils.operation("运动计时考生:" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + roundNo + "轮测试");

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);
            stuPairAdapter.notifyDataSetChanged();
            return;
        }
        //全部次数测试完，
        firstCheckTest();

    }


    /**
     * 循环测试下一位测试
     */
    private void loopTestNext() {
        for (int i = (position() + 1); i < pairs.size(); i++) {
            if (!isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                continue;
            }
            stuPairAdapter.setTestPosition(i);
            rvTestingPairs.scrollToPosition(i);
            presetResult();
            //最后一次测试的成绩
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            LogUtils.operation("运动计时考生" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + 1 + "次的第" + roundNo + "轮测试");
            stuPairAdapter.notifyDataSetChanged();

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);

            return;
        }
        firstCheckTest();
    }

    private void firstCheckTest() {
        //是否为最后一次测试，开启新的测试
        for (int i = 0; i < pairs.size(); i++) {
            if (isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                stuPairAdapter.setTestPosition(i);
                rvTestingPairs.scrollToPosition(i);
                presetResult();
                isExistTestPlace();
                LogUtils.operation("运动计时考生" + pairs.get(position()).getStudent().getSpeakStuName() + "开始第" + roundNo + "次测试");
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
                stuPairAdapter.notifyDataSetChanged();
                return;
            }
        }
        if (SettingHelper.getSystemSetting().isAutoPrint()) {
            TestCache testCache = TestCache.getInstance();
            InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
        }
        allTestComplete();
    }


    private int position() {
        return stuPairAdapter.getTestPosition();
    }

    private void presetResult() {
        resultList.clear();
        resultAdapter.setSelectPosition(-1);
        Student student = TestCache.getInstance().getAllStudents().get(position());
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        roundNo = (roundResults == null ? 1 : roundResults.size() + 1);
        Log.i("roundNo", "presetResult" + roundNo);
        for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {
            RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId() + "", i + 1);
            if (roundResult == null) {
                resultList.add(new SportTestResult(i + 1, -1, -1, new ArrayList<SportTimeResult>()));
                if (resultAdapter.getSelectPosition() == -1) {
                    resultAdapter.setSelectPosition(i);
                }
            } else {
//                List<MachineResult> machineResultList = DBManager.getInstance().getItemGroupFRoundMachineResult(student.getStudentCode(),
//                        group.getId(), i + 1);
                resultList.add(new SportTestResult(i + 1, roundResult.getResult(), roundResult.getResultState(), new ArrayList<SportTimeResult>()));

            }

        }
        resultAdapter.notifyDataSetChanged();
        setPartRoutes();
    }

    /**
     * 是否存在未测试位置
     */
    private boolean isExistTestPlace() {
        if (resultAdapter.getSelectPosition() == -1)
            return false;
        if (resultList.get(resultAdapter.getSelectPosition()).getResultState() != -1) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getResultState() == -1) {
                    resultAdapter.setSelectPosition(i);
                    if (startTest){
                        roundNo = i + 1;
                        startTest = false;
                    }

                    Log.i("roundNo", "isExistTestPlace" + roundNo);
                    resultAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            return false;
        } else {
            roundNo = resultAdapter.getSelectPosition() + 1;
            Log.i("roundNo", "isExistTestPlace resultAdapter" + roundNo);
            return true;
        }
    }

    /**
     * 考生是否全部测试完
     *
     * @param studentCode
     * @return
     */
    private boolean isStuAllTest(String studentCode) {
        //  查询学生成绩 当有成绩则添加数据跳过测试
        List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                (studentCode, group.getId() + "");
        //成绩数量是否小于测试次数
        return roundResultList.size() < TestConfigs.getMaxTestCount(this);
    }

    /**
     * 全部次数测试完
     */
    private void allTestComplete() {
        LogUtils.operation("运动计时分组模式测试完成");
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        if (group.getIsTestComplete() != 1 &&
                SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN &&
                (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 ||SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP)&&
                SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printA4Result(this, group);
        }
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
        testState = TestState.UN_STARTED;
        finish();
    }

    @Override
    public void updateDeviceState(int deviceId, int state) {

        if (deviceStates.get(deviceId - 1).getDeviceState() != state) {
            deviceStates.get(deviceId - 1).setDeviceState(state);
        }

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

    /**
     * 时间同步完成 等待计时结束，开始触摸出发
     */
    @Override
    public void getDeviceStart() {
        mHandler.sendEmptyMessage(UPDATE_ON_WAIT);
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
                mHandler.sendEmptyMessage(UPDATE_STOP);
                timerTask.setStart();
                sportPresent.clearLed(0);
            }
            if (receiveTime >= resultList.get(roundNo - 1).getSportTimeResults().size())
                return;
            if ((sportResult.getLongTime()-initTime) <lastTime){
                return;
            }
            if (partResultAdapter.getData().size() == 0){
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
            resultList.get(roundNo - 1).setResult(timeResult.getPartResult());
            resultList.get(roundNo - 1).setResultState(resultList.get(roundNo - 1).getResultState() ==
                    RoundResult.RESULT_STATE_FOUL ? RoundResult.RESULT_STATE_FOUL : timeResult.getResultState());

            mHandler.sendEmptyMessage(UPDATE_RESULT);
            receiveTime++;
        }
    }

    /**
     * 计时状态 0 停止计时 1开始计时
     *
     */
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
        sportPresent.setDeviceStateStop();
        sportPresent.presentRelease();
        TestCache.getInstance().clear();
        timerTask.release();
    }

    @Override
    public void onBackPressed() {
        if (testState != TestState.UN_STARTED){
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.onBackPressed();

    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_STOP:
                    txtStopTiming.setEnabled(true);
                    break;
                case UPDATE_RESULT:
//                    final String s =  resultList.get(roundNo - 1).getResultState() == RoundResult.RESULT_STATE_NORMAL?
//                            ResultDisplayUtils.getStrResultForDisplay(resultList.get(roundNo - 1).getResult()):"犯规";
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
                    resultList.get(roundNo - 1).setTestTime(System.currentTimeMillis() + "");
                    receiveTime = 0;
                    txtDeviceStatus.setText("计时");
                    break;
                case UPDATE_ON_TEXT:
                    tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(msg.arg1, false));
                    break;
            }
            return false;
        }
    });

    @Override
    public void onTimeTaskUpdate(int time) {
        Message message = mHandler.obtainMessage();
        message.what = UPDATE_ON_TEXT;
        message.obj = time;
        mHandler.sendMessage(message);

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
}
