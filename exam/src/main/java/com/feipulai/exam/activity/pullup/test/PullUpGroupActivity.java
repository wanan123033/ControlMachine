package com.feipulai.exam.activity.pullup.test;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.adapter.VolleyBallGroupStuAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.WaitDialog;
import com.feipulai.exam.view.baseToolbar.BaseToolbar;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class PullUpGroupActivity extends BaseTitleActivity
        implements PullUpTestFacade.Listener,
        BaseQuickAdapter.OnItemClickListener {

    private static final int UPDATE_SCORE = 0x3;

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
    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestingPairs;
    @BindView(R.id.tv_group_name)
    TextView tvGroupName;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.lv_results)
    ListView lvResults;

    private PullUpTestFacade facade;
    // 状态  WAIT_BEGIN--->TESTING---->WAIT_CONFIRM--->WAIT_BEGIN
    private static final int WAIT_BEGIN = 0x1;
    private static final int TESTING = 0x2;
    private static final int WAIT_CONFIRM = 0x3;
    protected volatile int state = WAIT_BEGIN;
    private Handler handler = new MyHandler(this);
    private PullUpSetting setting;
    private LEDManager ledManager = new LEDManager();
    private String testDate;
    private SystemSetting systemSetting;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private Group group;
    private WaitDialog changBadDialog;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_pullup;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();
        setting = SharedPrefsUtil.loadFormSource(this, PullUpSetting.class);

        group = (Group) TestConfigs.baseGroupMap.get("group");
        String type = "男女混合";
        if (group.getGroupType() == Group.MALE) {
            type = "男子";
        } else if (group.getGroupType() == Group.FEMALE) {
            type = "女子";
        }
        tvGroupName.setText(String.format(Locale.CHINA, "%s第%d组", type, group.getGroupNo()));

        TestCache.getInstance().init();
        pairs = CheckUtils.newPairs(((List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu")).size());
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);

        facade = new PullUpTestFacade(SettingHelper.getSystemSetting().getHostId(), this);
        stuPairAdapter.setOnItemClickListener(this);

        prepareForBegin();
    }

    @OnClick({R.id.tv_start_test, R.id.tv_stop_test, R.id.tv_print, R.id.tv_led_setting, R.id.tv_confirm,
            R.id.tv_punish, R.id.tv_abandon_test,R.id.tv_pair})
    public void onViewClicked(View view) {
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
                prepareForConfirmResult();
                break;

            case R.id.tv_print:
                TestCache testCache = TestCache.getInstance();
                InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                        TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                break;

            case R.id.tv_confirm:
                onResultConfirmed();
                break;

            case R.id.tv_punish:
                showPenalizeDialog(pairs.get(position()).getDeviceResult().getResult());
                break;

            case R.id.tv_abandon_test:
                facade.abandonTest();
                prepareForBegin();
                break;

            case R.id.tv_pair:
                changeBadDevice();
                break;

        }
    }

    private void onResultConfirmed() {
        tvResult.setText("");
        List<StuDevicePair> pairList = new ArrayList<>(1);
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

        if (systemSetting.isAutoBroadcast()) {
            TtsManager.getInstance().speak(ResultDisplayUtils.getStrResultForDisplay(roundResults.get(roundResults.size() - 1).getResult()));
        }

        boolean isAllTest = isAllTest(roundResults, student);
        if (isAllTest) {
            uploadResults();
        }

        dispatch(isAllTest);
    }

    private void switchToPosition(int position) {
        int oldPosition = position();
        stuPairAdapter.setTestPosition(position);
        stuPairAdapter.notifyItemChanged(oldPosition);
        stuPairAdapter.notifyItemChanged(position);
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
                nextPosition = position();
                ToastUtils.showShort("所有人均测试完成");
                group.setIsTestComplete(Group.FINISHED);
                DBManager.getInstance().updateGroup(group);
                if (systemSetting.isAutoPrint()) {
                    TestCache testCache = TestCache.getInstance();
                    InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                            TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                }
            }
        }
        switchToPosition(nextPosition);
    }

    private int nextPosition() {
        for (int i = position() + 1; i < pairs.size() + position(); i++) {
            int j = i % pairs.size();
            StuDevicePair pair = pairs.get(j);
            Student student = pair.getStudent();
            List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
            if (!isAllTest(roundResults, student)) {
                return j;
            }
        }
        return -1;// 所有人都测试完成了
    }

    private boolean isAllTest(List<RoundResult> roundResults, Student student) {
        if (roundResults == null || roundResults.size() == 0) {
            return false;
        }
        return !hasRemains(roundResults);
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
                    , testNo, groupNo, RoundResultBean.beanCope(roundResultList));
            uploadResults.add(uploadResult);
            Logger.i("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    private boolean hasRemains(List<RoundResult> roundResults) {
        return roundResults.size() < TestConfigs.getMaxTestCount(this);
    }

    private void prepareView(boolean tvPrintEnable, boolean tvStartTestEnable, boolean tvAbandonTestEnable,
                             boolean tvConfirmEnable, boolean tvStopTestEnable, boolean tvPunishEnable) {
        tvPrint.setVisibility(tvPrintEnable ? View.VISIBLE : View.GONE);

        tvStartTest.setVisibility(tvStartTestEnable ? View.VISIBLE : View.GONE);
        tvAbandonTest.setVisibility(tvAbandonTestEnable ? View.VISIBLE : View.GONE);
        tvConfirm.setVisibility(tvConfirmEnable ? View.VISIBLE : View.GONE);

        tvStopTest.setVisibility(tvStopTestEnable ? View.VISIBLE : View.GONE);
        tvPunish.setVisibility(tvPunishEnable ? View.VISIBLE : View.GONE);

        setAdapter();
    }

    protected void displayCheckedInLED() {
        Student student = TestCache.getInstance().getAllStudents().get(position());
        List<RoundResult> results = TestCache.getInstance().getResults().get(student);

        RoundResult lastResult = null;
        if (results != null && results.size() > 0) {
            lastResult = results.get(results.size() - 1);
        }
        int hostId = systemSetting.getHostId();
        ledManager.showString(hostId, pairs.get(position()).getStudent().getStudentName(), 5, 0, true, lastResult == null);
        if (lastResult != null) {
            String displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
            ledManager.showString(hostId, "已有成绩:" + displayResult, 2, 3, false, true);
        }
    }

    private void prepareForBegin() {
        Student student = pairs.get(position()).getStudent();
        tvResult.setText(student.getStudentName());

        List<RoundResult> results = TestCache.getInstance().getResults().get(student);

        prepareView(true,
                results == null || results.size() < TestConfigs.getMaxTestCount(this),
                false, false, false,
                false);

        displayCheckedInLED();

        rvTestingPairs.smoothScrollToPosition(position());
        state = WAIT_BEGIN;
    }

    private void prepareForTesting() {
        if (pairs.get(position()).getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT) {
            ToastUtils.showShort("设备未连接,不能开始测试");
            return;
        }

        prepareView(false, false,
                true, false, false,
                false);

        tvResult.setText("准备");
        testDate = System.currentTimeMillis() + "";
        facade.startTest();
        state = TESTING;
    }

    private void prepareForConfirmResult() {
        state = WAIT_CONFIRM;
        prepareView(false, false,
                false, true, false,
                setting.isPenalize());
    }

    @Override
    protected void handleMessage(Message msg) {

        switch (msg.what) {
            case PullUpManager.STATE_DISCONNECT:
                cbDeviceState.setChecked(false);
                pairs.get(position()).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
                break;

            case PullUpManager.STATE_FREE:
                cbDeviceState.setChecked(true);
                pairs.get(position()).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                break;

            case UPDATE_SCORE:
                PullUpStateResult result = (PullUpStateResult) msg.obj;
                pairs.get(position()).setDeviceResult(result);
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(position()).getDeviceResult().getResult());
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
        // onScoreArrived(new PullUpResult());
        ledManager.showString(SettingHelper.getSystemSetting().getHostId(), pairs.get(position()).getStudent().getStudentName(), 5, 0, true, true);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                prepareView(false, false,
                        true, false, true,
                        false);
            }
        });
    }

    @Override
    public void finish() {
        if (!isConfigurableNow()) {
            ToastUtils.showShort("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
        facade.stopTotally();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
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
        return state == WAIT_BEGIN;
    }

    private void setAdapter() {
        int maxTestNo = TestConfigs.getMaxTestCount(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rvTestResult.setLayoutManager(layoutManager);
        Student student = TestCache.getInstance().getAllStudents().get(position());
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        // Log.i("james", roundResults.toString());
        List<String> results = new ArrayList<>(maxTestNo);
        if (roundResults != null) {
            for (RoundResult result : roundResults) {
                results.add(ResultDisplayUtils.getStrResultForDisplay(result.getResult()));
            }
        }
        BasePersonTestResultAdapter adapter = new BasePersonTestResultAdapter(results);
        rvTestResult.setAdapter(adapter);
    }

    public void showPenalizeDialog(int max) {
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(0);
        numberPicker.setValue(pairs.get(position()).getPenalty());
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
                        if (value != pairs.get(position()).getPenalty()) {
                            ledManager.showString(systemSetting.getHostId(), "判罚:" + ResultDisplayUtils.getStrResultForDisplay(value), 1, 2, false, false);
                            ledManager.showString(systemSetting.getHostId(),
                                    "最终:" + ResultDisplayUtils.getStrResultForDisplay(pairs.get(position()).getDeviceResult().getResult() + value),
                                    1, 3, false, true);
                        }
                        pairs.get(position()).setPenalty(value);
                        ToastUtils.showShort("判罚成功");
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        if (isConfigurableNow()) {
            stuPairAdapter.setTestPosition(i);
            stuPairAdapter.notifyDataSetChanged();
            prepareForBegin();
        } else {
            ToastUtils.showShort("测试中,不能更换考生");
        }
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title =  TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private int position() {
        return stuPairAdapter.getTestPosition();
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

}
