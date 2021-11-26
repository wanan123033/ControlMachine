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
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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

import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.PullUpManager;
import com.feipulai.device.serial.beans.PullUpStateResult;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.base.PenalizeDialog;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.adapter.BasePersonTestResultAdapter;
import com.feipulai.exam.activity.pullup.pair.PullSitUpPairActivity;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.pullup.setting.PullUpSettingActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.WaitDialog;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PullAndSitUpIndividualActivity extends BaseTitleActivity
        implements PullSitUpTestFacade.Listener,
        IndividualCheckFragment.OnIndividualCheckInListener, PenalizeDialog.PenalizeListener, BaseAFRFragment.onAFRCompareListener {

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

    private PullSitUpTestFacade facade;
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
    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;

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

        facade = new PullSitUpTestFacade(SettingHelper.getSystemSetting().getHostId(), this);
        ledManager.resetLEDScreen(SettingHelper.getSystemSetting().getHostId(), TestConfigs.machineNameMap.get(TestConfigs.sCurrentItem.getMachineCode()));
        prepareForCheckIn();
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
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
    protected void onResume() {
        super.onResume();
        facade.setLinking(false);
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
                    startActivity(new Intent(PullAndSitUpIndividualActivity.this, LEDSettingActivity.class));
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
            startActivity(new Intent(PullAndSitUpIndividualActivity.this, PullUpSettingActivity.class));
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
            TestCache.getInstance().getTestNoMap().put(student, 1);
        } else {
            lastResult = results.get(results.size() - 1);
            TestCache.getInstance().getResults().put(student, results);
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            int testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
            TestCache.getInstance().getTestNoMap().put(student, testNo);
        }

        TestCache.getInstance().setTestingPairs(pairs);
        TestCache.getInstance().getStudentItemMap().put(student, studentItem);

        pairs.get(0).setDeviceResult(new PullUpStateResult());
        pairs.get(0).setPenalty(0);

        tvResult.setText(student.getStudentName());
        prepareForBegin();
        displayCheckedInLED(lastResult);
    }

    protected void displayCheckedInLED(RoundResult lastResult) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        ledManager.showString(hostId, pairs.get(0).getStudent().getLEDStuName(), 5, 0, true, lastResult == null);
        if (lastResult != null) {
            String displayResult = ResultDisplayUtils.getStrResultForDisplay(lastResult.getResult());
            ledManager.showString(hostId, "已有成绩:" + displayResult, 2, 3, false, true);
        }
    }

    @OnClick({R.id.tv_start_test, R.id.tv_stop_test, R.id.tv_print, R.id.tv_led_setting, R.id.tv_confirm,
            R.id.tv_punish, R.id.tv_abandon_test, R.id.tv_finish_test, R.id.tv_exit_test, R.id.tv_pair, R.id.img_AFR})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_led_setting:
                LogUtils.operation("点击LED设置按钮");

                if (isConfigurableNow()) {
                    startActivity(new Intent(this, LEDSettingActivity.class));
                } else {
                    toastSpeak("测试中,不能进行外接屏幕设置");
                }
                break;

            case R.id.tv_start_test:
                LogUtils.operation("点击开始按钮");
                prepareForTesting();
                break;

            case R.id.tv_stop_test:
                LogUtils.operation("点击停止按钮");
                facade.stopTest();
                SoundPlayUtils.play(12);
                prepareForConfirmResult();
                break;

            case R.id.tv_print:
                LogUtils.operation("点击打印按钮");
                TestCache testCache = TestCache.getInstance();
                InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                        TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                break;

            case R.id.tv_confirm:
                LogUtils.operation("点击确定按钮");
                tvResult.setText("");
                InteractUtils.saveResults(pairs, testDate);
                onResultConfirmed();
                break;

            case R.id.tv_punish:
                LogUtils.operation("点击判罚按钮");
                showPenalizeDialog(pairs.get(0).getDeviceResult().getResult());
                break;

            case R.id.tv_abandon_test:
                LogUtils.operation("点击放弃按钮");
                facade.abandonTest();
                state = WAIT_BEGIN;
                prepareForBegin();
                break;

            case R.id.tv_finish_test:
                LogUtils.operation("点击测试完成按钮");
                prepareForCheckIn();
                break;

            case R.id.tv_exit_test:
                LogUtils.operation("点击退出按钮");
                prepareForCheckIn();
                break;

            case R.id.tv_pair:
                LogUtils.operation("点击配对按钮");
                IntentUtil.gotoActivity(this, PullSitUpPairActivity.class);
                break;
            case R.id.img_AFR:
                showAFR();
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
            prepareForBegin();
        } else {
            prepareForFinish();
        }
    }

    private boolean shouldContinue() {
        int maxTestNo = TestConfigs.getMaxTestCount(this);
        TestCache testCache = TestCache.getInstance();
        Student student = testCache.getAllStudents().get(0);
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
    }

    private void prepareForBegin() {
        TestCache testCache = TestCache.getInstance();
        Student student = pairs.get(0).getStudent();
        InteractUtils.showStuInfo(llStuDetail, student, testCache.getResults().get(student));
//        LogUtils.operation(student.getStudentName() + " 考生进行第" + testCache.getResults().get(student).size() + 1 + "轮");
        tvResult.setText(student.getStudentName());

        prepareView(true, false, true, true,
                false, false, false,
                false, true, false);

        state = WAIT_BEGIN;
    }

    private void prepareForTesting() {
        if (pairs.get(0).getBaseDevice().getState() == BaseDeviceState.STATE_DISCONNECT) {
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
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        rvTestResult.setLayoutManager(layoutManager);
        Student student = TestCache.getInstance().getAllStudents().get(0);
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
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
        Log.e("TAG=====", max + "");
        PenalizeDialog dialog = new PenalizeDialog(this);
        dialog.setPenalizeListener(this);
        dialog.setMinMaxValue(max * -1, max);
        dialog.show();
    }


    @Override
    public void onNoPairResponseArrived() {

    }

    @Override
    public void onNewDeviceConnect() {

    }

    @Override
    public void onInvalid(int invalid) {
//        toastSpeak("不规则数据"+invalid);
    }

    @Override
    protected void onStop() {
        super.onStop();
        facade.setLinking(true);
    }


    @Override
    public void penalize(int value) {
        if (value != pairs.get(0).getPenalty()) {
            ledManager.showString(systemSetting.getHostId(), "判罚:" + ResultDisplayUtils.getStrResultForDisplay(value), 1, 2, false, false);
            ledManager.showString(systemSetting.getHostId(),
                    "最终:" + ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult() + value),
                    1, 3, false, true);
        }
        pairs.get(0).setPenalty(value);
        toastSpeak("判罚成功");
        updateResult();
    }

    private void updateResult() {
        if (pairs.get(0).getPenalty() != 0) {
            tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult()) + "(判罚:" + pairs.get(0).getPenalty() + ")");
        } else {
            tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult()));
        }
    }


    @Override
    public void dismisson(DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    public boolean getPenalize() {
        return true;
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (student == null) {
                    InteractUtils.toastSpeak(PullAndSitUpIndividualActivity.this, "该考生不存在");
                    return;
                } else {
                    afrFrameLayout.setVisibility(View.GONE);
                }
                final StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                if (studentItem == null) {
                    InteractUtils.toastSpeak(PullAndSitUpIndividualActivity.this, "无此项目");
                    return;
                }
                final List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
                if (results != null && results.size() >= TestConfigs.getMaxTestCount(PullAndSitUpIndividualActivity.this)) {
                    InteractUtils.toastSpeak(PullAndSitUpIndividualActivity.this, "该考生已测试");
                    return;
                }
                LogUtils.operation("检入考生：" + student.toString());
                onIndividualCheckIn(student, studentItem, results);
            }
        });
    }

    @Override
    public void setRoundNo(Student student, int roundNo) {
        for (StuDevicePair pair : pairs){
            Student student1 = pair.getStudent();
            if (student1 != null && student1.getStudentCode().equals(student.getStudentCode())){
                pair.setCurrentRoundNo(roundNo);
            }
        }
    }
}
