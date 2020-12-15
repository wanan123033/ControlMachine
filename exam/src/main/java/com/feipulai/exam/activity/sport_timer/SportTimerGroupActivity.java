package com.feipulai.exam.activity.sport_timer;

import android.os.Bundle;
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
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.BasketBallGroupActivity;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
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
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SportTimerGroupActivity extends BaseTitleActivity implements SportContract.SportView, BaseQuickAdapter.OnItemClickListener {
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
    private TimeResultAdapter timeResultAdapter;
    private SportTestCountAdapter testCountAdapter;
    private PartResultAdapter partResultAdapter;
    private int partSelect;
    private TestState testState;
    private int roundNo = 1;
    private int receiveTime = 0;
    private int initTime;
    private List<SportTestResult> testResults = new ArrayList<>();//保存成绩
    private int testNum;
    private Group group;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private VolleyBallGroupStuAdapter stuPairAdapter;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_sport_timer;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SportTimerSetting.class);
        if (setting == null)
            setting = new SportTimerSetting();
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
                partResultAdapter.replaceData(testResults.get(roundNo - 1).getSportTimeResults());
                testCountAdapter.setSelectPosition(position);
                testCountAdapter.notifyDataSetChanged();
            }
        });

        setTxtEnable(false);
        testState = TestState.UN_STARTED;
        setPartRoutes();
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
        pairs = CheckUtils.newPairs(((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu")).size());
        LogUtils.operation("运动计时获取到分组学生:" + pairs.size() + "---" + pairs.toString());
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setOnItemClickListener(this);

        firstCheckTest();
    }

    private void setTxtEnable(boolean enable) {
        tvFoul.setEnabled(enable);
        tvInBack.setEnabled(enable);
        tvAbandon.setEnabled(enable);
        tvNormal.setEnabled(enable);
        tvDelete.setEnabled(enable);
        tvPrint.setEnabled(enable);
        tvConfirm.setEnabled(enable);
        txtFinishTest.setEnabled(enable);
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
        testCountAdapter.setSelectPosition(roundNo - 1);
        testCountAdapter.notifyDataSetChanged();
        partResultAdapter.replaceData(testResults.get(roundNo - 1).getSportTimeResults());
    }

    @OnClick({R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.txt_waiting, R.id.txt_illegal_return,
            R.id.txt_stop_timing, R.id.tv_end_result, R.id.tv_part_result, R.id.tv_del, R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_foul:

                break;
            case R.id.tv_inBack:
                break;
            case R.id.tv_abandon:
                break;
            case R.id.tv_normal:
                break;
            case R.id.txt_waiting:
                if (roundNo >= testNum) {
                    toastSpeak("已超过测试次数");
                }
                if (testState == TestState.UN_STARTED && cbDeviceState.isChecked()) {
                    sportPresent.waitStart();
                } else {
                    ToastUtils.showShort("当前设备不可用或当前学生为空");
                }
                break;
            case R.id.txt_illegal_return:
                if (testState == TestState.WAIT_RESULT) {
                    sportPresent.setDeviceStateStop();
                    receiveTime = 0;
                    testState = TestState.UN_STARTED;
                    setTxtEnable(false);
                    txtWaiting.setEnabled(true);
                }
                break;
            case R.id.txt_stop_timing:
                if (testState == TestState.WAIT_RESULT) {
                    sportPresent.setDeviceStateStop();
                    setTxtEnable(true);
                }
                timeResultAdapter.notifyDataSetChanged();
                sportPresent.getDeviceState();
                break;
            case R.id.tv_end_result:
                break;
            case R.id.tv_part_result:
                break;
            case R.id.tv_del:
                break;
            case R.id.tv_print:
                break;
            case R.id.tv_confirm:
                break;
            case R.id.txt_finish_test:
                break;
        }
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
            if (!isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                stuPairAdapter.setTestPosition(i);
                rvTestingPairs.scrollToPosition(i);
                presetResult();
                isExistTestPlace();
                prepareForBegin();
                LogUtils.operation("篮球考生" + pairs.get(position()).getStudent().getSpeakStuName() + "开始第" + roundNo + "次测试");
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

    private void prepareForBegin() {

    }

    private int position() {
        return stuPairAdapter.getTestPosition();
    }

    private void presetResult() {


    }

    /**
     * 是否存在未测试位置
     */
    private boolean isExistTestPlace() {

        return true;
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
                SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4 &&
                SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printA4Result(this, group);
        }
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
        testState = TestState.UN_STARTED;
    }

    @Override
    public void updateDeviceState(int deviceId, int state) {

        if (deviceStates.get(deviceId - 1).getDeviceState() != state) {
            deviceStates.get(deviceId - 1).setDeviceState(state);
        }

        boolean flag = false;
        for (DeviceState deviceState : deviceStates) {
            if (deviceState.getDeviceState() != 1) {
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
    public void getTimeUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtWaiting.setEnabled(false);
                txtStopTiming.setEnabled(true);
                txtIllegalReturn.setEnabled(true);
                sportPresent.setRunState(1);
                testState = TestState.WAIT_RESULT;
                setTxtEnable(false);
                testResults.get(roundNo - 1).setTestTime(System.currentTimeMillis() + "");
                receiveTime = 0;
            }
        });
    }

    @Override
    public void receiveResult(SportResult sportResult) {

    }

    /**
     * 计时状态 0 停止计时 1开始计时
     *
     * @param deviceState
     */
    @Override
    public void getDeviceState(int deviceState) {
        if (deviceState == 0 && testState == TestState.WAIT_RESULT) {
            testState = TestState.RESULT_CONFIRM;
            receiveTime = 0;
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

    }
}
