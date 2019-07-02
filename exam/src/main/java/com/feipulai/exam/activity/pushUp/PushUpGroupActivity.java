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
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.SitPushUpManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.device.sitpullup.SitPullLinker;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.pushUp.check.PushUpCheckActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.volleyball.VolleyBallSetting;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
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
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class PushUpGroupActivity extends BaseTitleActivity
        implements PushUpResiltListener.Listener,
        BaseQuickAdapter.OnItemClickListener, SitPullLinker.SitPullPairListener {

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
    @BindView(R.id.tv_device_pair)
    TextView tvDevicePair;
    @BindView(R.id.lv_results)
    ListView lvResults;

    private PushUpResiltListener facade;
    // 状态  WAIT_BEGIN--->TESTING---->WAIT_CONFIRM--->WAIT_BEGIN
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
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private Group group;
    private int intervalCount = 0;
    private WaitDialog changBadDialog;
    private SitPullLinker linker;
    protected final int TARGET_FREQUENCY = SerialConfigs.sProChannels.get(TestConfigs.sCurrentItem.getMachineCode()) + SettingHelper.getSystemSetting().getHostId() - 1;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_pushup;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();
        setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);

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

        facade = new PushUpResiltListener(SettingHelper.getSystemSetting().getHostId(), setting, this);
        stuPairAdapter.setOnItemClickListener(this);

        prepareForBegin();
        if (setting.getTestType() == PushUpSetting.WIRED_TYPE) {
            tvDevicePair.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        facade.setTimeLimit(setting);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.ITEM_SETTING_UPDATE) {
            setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
            if (setting.getTestType() == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() > 1) {
                finish();
                IntentUtil.gotoActivity(PushUpGroupActivity.this, PushUpCheckActivity.class);
            }
            facade.setTimeLimit(setting);
        }
    }

    @OnClick({R.id.tv_start_test, R.id.tv_stop_test, R.id.tv_print, R.id.tv_led_setting, R.id.tv_device_pair, R.id.tv_confirm,
            R.id.tv_abandon_test})
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
                if (intervalCount > 0) {
                    //显示最终成绩
                    ledManager.showString(systemSetting.getHostId(),
                            "最终成绩:" + ResultDisplayUtils.getStrResultForDisplay(pairs.get(position()).getDeviceResult().getResult() - intervalCount),
                            1, 3, false, true);
                }
                break;

            case R.id.tv_print:
                TestCache testCache = TestCache.getInstance();
                InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                        TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                break;

            case R.id.tv_confirm:
                onResultConfirmed();
                break;

            case R.id.tv_abandon_test:
                facade.abandonTest();
                prepareForBegin();
                break;
            case R.id.tv_device_pair:
                changeBadDevice();
                break;
        }
    }

    private void onResultConfirmed() {
        tvResult.setText("");
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

        if (systemSetting.isAutoBroadcast()) {

            TtsManager.getInstance().speak(student.getSpeakStuName()+ResultDisplayUtils.getStrResultForDisplay(roundResults.get(roundResults.size() - 1).getResult()));
        }
        uploadResults();

        boolean isAllTest = isAllTest(roundResults, student);
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
        boolean fullSkip = fullSkip(roundResults, student);
        return fullSkip || !hasRemains(roundResults);
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
                    , testNo, groupNo, RoundResultBean.beanCope(roundResultList));
            uploadResults.add(uploadResult);
            Logger.i("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    private boolean hasRemains(List<RoundResult> roundResults) {
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
        ledManager.showString(hostId, pairs.get(position()).getStudent().getLEDStuName(), 5, 0, true, lastResult == null);
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
                false, false, false, false,
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
                true, false, true, false,
                false);

        tvResult.setText("准备");
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
                cbDeviceState.setChecked(false);
                pairs.get(position()).getBaseDevice().setState(BaseDeviceState.STATE_DISCONNECT);
                break;

            case SitPushUpManager.STATE_FREE:
                cbDeviceState.setChecked(true);
                pairs.get(position()).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
                break;

            case UPDATE_SCORE:
                SitPushUpStateResult result = (SitPushUpStateResult) msg.obj;
                intervalCount = msg.arg1;
                pairs.get(position()).setDeviceResult(result);
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(position()).getDeviceResult().getResult());
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
        // tickInUI("开始");
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
        if (!isConfigurableNow()) {
            ToastUtils.showShort("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
        facade.stopTotally();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
    }

    @Override
    public void onTestingTimerTick(long tick) {
        tickInUI(tick + "");
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
        title = TestConfigs.machineNameMap.get(machineCode)
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
        facade.deviceManager.setFrequency(ItemDefault.CODE_FWC,
                originFrequency,
                deviceId,
                SettingHelper.getSystemSetting().getHostId());
    }

    public void cancelChangeBad() {
        facade.mLinking = false;
        if (linker != null) {
            linker.cancelPair();
        }
        facade = null;
        facade = new PushUpResiltListener(SettingHelper.getSystemSetting().getHostId(), setting, this);
    }

    public void changeBadDevice() {
        if (linker == null) {
            linker = new SitPullLinker(TestConfigs.sCurrentItem.getMachineCode(), TARGET_FREQUENCY, this);
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
                changBadDialog.dismiss();
            }
        });
    }

}
