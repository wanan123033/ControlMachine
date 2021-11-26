package com.feipulai.exam.activity.basketball.motion;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.newRadioTimer.TimerTask;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.BasketBallSettingActivity;
import com.feipulai.exam.activity.basketball.adapter.BasketBallResultAdapter;
import com.feipulai.exam.activity.basketball.pair.BasketBallPairActivity;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.SportContract;
import com.feipulai.exam.activity.sport_timer.SportPresent;
import com.feipulai.exam.activity.sport_timer.TestState;
import com.feipulai.exam.activity.sport_timer.bean.DeviceState;
import com.feipulai.exam.activity.sport_timer.pair.SportPairActivity;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BasketBallMotionTestActivity extends BaseTitleActivity implements BaseAFRFragment.onAFRCompareListener, IndividualCheckFragment.OnIndividualCheckInListener, SportContract.SportView, TimerTask.TimeUpdateListener {
    private static final int UPDATE_ON_STOP = 0XF3;
    private IndividualCheckFragment individualCheckFragment;
    protected volatile int state = WAIT_FREE;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    private static final int WAIT_SCORE_CONFRIM = 0x6;

    private final int UPDATE_ON_TEXT = 0XF5;
    private final int UPDATE_ON_WAIT = 0XF4;

    private BaseAFRFragment afrFragment;
    private FrameLayout afrFrameLayout;
    private BasketBallSetting setting;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private List<BasketBallTestResult> resultList = new ArrayList<>();
    private BasketBallResultAdapter resultAdapter;
    private SportPresent sportPresent;
    private TimerTask timerTask;

    @BindView(R.id.ll_stu_detail)
    LinearLayout llStuDetail;

    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.lv_results)
    ListView lvResults;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_continue_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.tv_pair)
    TextView tvPair;
    @BindView(R.id.cb_near)
    CheckBox cbDeviceState;
    private StudentItem mStudentItem;
    private int roundNo;

    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ON_TEXT:
                    int time = msg.arg1;
                    String formatTime ;
                    if (time<60*60*1000){
                        formatTime = DateUtil.formatTime1(time, "mm:ss.SSS");
                    }else {
                        formatTime = DateUtil.formatTime1(time, "HH:mm:ss");
                    }
                    tvResult.setText(formatTime);
                    break;
                case UPDATE_ON_WAIT:
                    txtWaiting.setEnabled(false);
                    txtIllegalReturn.setEnabled(true);
                    sportPresent.setRunState(1);
                    state = WAIT_SCORE_CONFRIM;
                    txtDeviceStatus.setText("计时");
                    break;
                case UPDATE_ON_STOP:
                    txtStopTiming.setEnabled(false);
                    txtIllegalReturn.setEnabled(false);
                    txtDeviceStatus.setText("停止计时");
                    break;
            }
            return true;
        }
    });
    private List<DeviceState> deviceStates;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        int hostId = SettingHelper.getSystemSetting().getHostId();
        return builder.setTitle("篮球运球"+hostId+"号机").addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    private void gotoItemSetting() {
        if (!isConfigurableNow()) {
            LogUtils.operation("跳转至篮球项目设置界面");
            IntentUtil.gotoActivityForResult(this, BasketBallSettingActivity.class, 1);
        } else {
            toastSpeak("测试中,不允许修改设置");
        }
    }
    private boolean isConfigurableNow() {
        boolean flag = !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
        return flag;
    }
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_individual_basketball;
    }

    @Override
    protected void initData() {
        tvPair.setVisibility(View.VISIBLE);
        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }

        //获取项目设置
        setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
        if (setting == null)
            setting = new BasketBallSetting();
        LogUtils.operation("项目设置" + setting.toString());
        sportPresent = new SportPresent(this,2);

        StuDevicePair pair = new StuDevicePair();
        BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_DISCONNECT);
        pair.setBaseDevice(deviceState);
        pair.setDeviceResult(new BasketballResult());
        pairs.add(pair);

        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setResultView(lvResults);
        individualCheckFragment.setOnIndividualCheckInListener(this);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);

        resultAdapter = new BasketBallResultAdapter(resultList, setting);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(resultAdapter);

        resultAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isConfigurableNow() || state == WAIT_STOP) {
                    resultAdapter.setSelectPosition(position);
                    resultAdapter.notifyDataSetChanged();

                }
            }
        });

        deviceStates = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            DeviceState deviceState1 = new DeviceState();
            deviceState1.setDeviceId(i + 1);
            deviceState1.setDeviceState(0);
            deviceStates.add(deviceState1);
        }

        timerTask = new TimerTask(this,100);
        prepareForCheckIn();
        state = WAIT_FREE;
        setOperationUI();
        txtContinueRun.setVisibility(View.GONE);
    }
    /**
     * 检入
     */
    private void prepareForCheckIn() {
        resultList.clear();
        resultAdapter.notifyDataSetChanged();
        TestCache.getInstance().clear();
        pairs.get(0).setStudent(null);
        InteractUtils.showStuInfo(llStuDetail, null, null);
        tvResult.setText("请检录");
        state = WAIT_CHECK_IN;
        setOperationUI();
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
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (student == null) {
                    InteractUtils.toastSpeak(BasketBallMotionTestActivity.this, "该考生不存在");
                    return;
                } else {
                    afrFrameLayout.setVisibility(View.GONE);
                }
                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
                if (studentItem == null) {
                    InteractUtils.toastSpeak(BasketBallMotionTestActivity.this, "无此项目");
                    return;
                }
                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
                if (results != null && results.size() >= TestConfigs.getMaxTestCount()) {
                    InteractUtils.toastSpeak(BasketBallMotionTestActivity.this, "该考生已测试");
                    return;
                }
                // 可以直接检录
                onIndividualCheckIn(student, studentItem, results);
            }
        });
    }

    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        if (state == WAIT_FREE || state == WAIT_CHECK_IN) {
            pairs.get(0).setStudent(student);
            for (RoundResult result : results) {
                if (isFullSkip(result.getResult(), result.getResultState())) {
                    toastSpeak("满分");
                    pairs.get(0).setStudent(null);
                    LogUtils.operation("篮球检入该学生已满分跳过测试:" + student.getStudentName());
                    return;
                }
            }

            resultAdapter.setSelectPosition(-1);
            mStudentItem = studentItem;
            state = WAIT_CHECK_IN;
            setOperationUI();
            pairs.get(0).setStudent(student);
            TestCache.getInstance().init();
            TestCache.getInstance().getAllStudents().add(student);
            TestCache.getInstance().getResults().put(student, results);

            int testNo;
            if (results == null || results.size() == 0) {
                TestCache.getInstance().getResults().put(student,
                        results != null ? results
                                : new ArrayList<RoundResult>(TestConfigs.getMaxTestCount(this)));
                //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
                RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
                testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
                if (student != null)
                    LogUtils.operation("足球该学生未测试:" + student.getStudentCode() + ",测试次数 =  " + testNo);
            } else {
                TestCache.getInstance().getResults().put(student, results);
                //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
                RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
                testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo();
                if (student != null)
                    LogUtils.operation("篮球该学生有成绩:" + student.getStudentCode() + ",测试次数 = " + testNo);
            }
            roundNo = results.size() + 1;
            LogUtils.operation("篮球当前轮次：" + roundNo);
            TestCache.getInstance().getTestNoMap().put(student, testNo);

            presetResult(student, testNo);
            resultAdapter.notifyDataSetChanged();

            TestCache.getInstance().setTestingPairs(pairs);
            TestCache.getInstance().getStudentItemMap().put(student, studentItem);
            pairs.get(0).setDeviceResult(new BasketballResult());
            pairs.get(0).setPenalty(0);

            prepareForBegin();

        } else {
            toastSpeak("当前考生还未完成测试,拒绝检录");
        }
    }
    private boolean isFullSkip(int result, int resultState) {
        Student student = pairs.get(0).getStudent();
        if (setting.isFullSkip() && resultState == RoundResult.RESULT_STATE_NORMAL) {
//            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            if (student.getSex() == Student.MALE) {
                return result <= setting.getMaleFullScore() * 1000;
            } else {
                return result <= setting.getFemaleFullScore() * 1000;
            }
        }
        return false;
    }
    private void setOperationUI() {
        switch (state) {
            case WAIT_FREE:
                txtWaiting.setEnabled(false);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(false);

                break;
            case WAIT_CHECK_IN:
                txtWaiting.setEnabled(true);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(false);
                break;
            case WAIT_BEGIN:
                txtWaiting.setEnabled(false);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(true);
                break;
            case TESTING:
                txtIllegalReturn.setEnabled(true);
                txtWaiting.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(true);
                break;
            case WAIT_STOP:
                txtWaiting.setEnabled(true);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(false);
                txtStopTiming.setEnabled(false);
                break;
            case WAIT_CONFIRM:
                txtWaiting.setEnabled(true);
                txtIllegalReturn.setEnabled(false);
                txtContinueRun.setEnabled(true);
                txtStopTiming.setEnabled(false);
                break;
        }
    }
    /**
     * 预设置成绩
     */
    private void presetResult(Student student, int testNo) {
        resultList.clear();
        for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {
            RoundResult roundResult = DBManager.getInstance().queryRoundByRoundNo(student.getStudentCode(), testNo, i + 1);
            StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),student.getStudentCode());
            if (studentItem.getExamType() == 2){
                roundResult = null;
            }
            if (roundResult == null) {
                resultList.add(new BasketBallTestResult(i + 1, null, 0, -999, 0, -999));
                if (resultAdapter.getSelectPosition() == -1) {
                    resultAdapter.setSelectPosition(i);
                }
            } else {
                List<MachineResult> machineResultList = DBManager.getInstance().getItemRoundMachineResult(student.getStudentCode(),
                        testNo, i + 1);
                resultList.add(new BasketBallTestResult(i + 1, machineResultList, roundResult.getMachineResult(), roundResult.getResult(), roundResult.getPenaltyNum(), roundResult.getResultState()));
            }
            Log.e("TAG----",resultList.size()+"---");
        }
    }
    @OnClick({R.id.txt_waiting,R.id.txt_illegal_return,R.id.txt_continue_run,R.id.txt_stop_timing,R.id.tv_print,R.id.tv_confirm,
            R.id.txt_finish_test,R.id.tv_punish_add,R.id.tv_punish_subtract,R.id.tv_foul,R.id.tv_inBack,R.id.tv_abandon,R.id.tv_normal})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.txt_waiting:
                if ((state == WAIT_CHECK_IN || state == WAIT_CONFIRM || state == WAIT_STOP) && isExistTestPlace()) {
                    sportPresent.waitStart();
                    state = WAIT_BEGIN;
                    setOperationUI();
                } else {
                    ToastUtils.showShort("当前设备不可用或当前学生为空");
                }

                break;
            case R.id.txt_illegal_return:
                showIllegalReturnDialog();
                break;
            case R.id.txt_continue_run:
                break;
            case R.id.txt_stop_timing:
                timerTask.stopKeepTime();
                sportPresent.setDeviceStateStop();
                state = WAIT_STOP;
                setOperationUI();
                break;
            case R.id.tv_print:
                if (state == WAIT_SCORE_CONFRIM){
                    printResult();
                }else {
                    toastSpeak("测试成绩未保存不可打印");
                }
                break;
            case R.id.tv_confirm:
                state = WAIT_SCORE_CONFRIM;
                InteractUtils.saveResults(pairs,System.currentTimeMillis()+"");

                break;
            case R.id.txt_finish_test:
                state = WAIT_CHECK_IN;
                setOperationUI();
                InteractUtils.showStuInfo(llStuDetail, null, null);
                break;
            case R.id.tv_punish_add:
                setPunish(1);
                break;
            case R.id.tv_punish_subtract:
                setPunish(-1);
                break;
            case R.id.tv_foul:
                setResultState(RoundResult.RESULT_STATE_FOUL);
                break;
            case R.id.tv_inBack:
                setResultState(RoundResult.RESULT_STATE_BACK);
                break;
            case R.id.tv_abandon:
                setResultState(RoundResult.RESULT_STATE_WAIVE);
                break;
            case R.id.tv_normal:
                setResultState(RoundResult.RESULT_STATE_NORMAL);
                break;

        }
    }
    private void setResultState(int resultState){
        if (state == TESTING || state == WAIT_BEGIN) {
            toastSpeak("测试中,不允许更改考试成绩状态");
        } else {
            if (resultAdapter.getSelectPosition() == -1)
                return;
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            if (testResult.getResult() == 0 && testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL
                    && resultState == RoundResult.RESULT_STATE_NORMAL) {
                toastSpeak("成绩不存在，不允许修改为正常状态");
                return;
            }
            if (testResult.getResult() < 0 && resultState == RoundResult.RESULT_STATE_NORMAL) {
                resultList.get(resultAdapter.getSelectPosition()).setResultState(-999);
            } else {
                resultList.get(resultAdapter.getSelectPosition()).setResultState(resultState);
            }

            resultAdapter.notifyDataSetChanged();
            LogUtils.operation("修改成绩状态:resultState=" + resultState);
        }
    }
    private void setPunish(int punishType) {
        if (state == TESTING || state == WAIT_BEGIN) {
            toastSpeak("测试中,不允许更改考试成绩");
        } else {
            if (resultAdapter.getSelectPosition() == -1)
                return;
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            if ((testResult.getResult() <= 0 && (testResult.getResultState() == -999
                    || testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL))) {
                toastSpeak("成绩不存在");
                return;
            }
            int penalizeNum = testResult.getPenalizeNum();
            Logger.i("原始成绩:" + penalizeNum + "判罚:" + punishType);
            if (punishType >= 0) {//+
                testResult.setPenalizeNum(penalizeNum + 1);
            } else {//-
                if (penalizeNum > 0) {
                    testResult.setPenalizeNum(penalizeNum - 1);
                }
            }
            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * (int)(setting.getPenaltySecond() * 1000.0));
            testResult.setResult(result);

            resultAdapter.notifyDataSetChanged();
        }
    }

    private void printResult() {
        List<Student> students = new ArrayList<>();
        students.add(pairs.get(0).getStudent());
        List<RoundResult> roundResults = DBManager.getInstance().queryResultsByStuItem(mStudentItem);
        Map<Student, List<RoundResult>> map = new HashMap<>();
        map.put(pairs.get(0).getStudent(), roundResults);
        Map<Student, Integer> m = new HashMap<>();
        m.put(pairs.get(0).getStudent(), roundNo);
        sportPresent.print(students, this, map, m);
    }
    private boolean isExistTestPlace() {
        if (resultAdapter.getSelectPosition() == -1)
            return false;
        if (resultList.get(resultAdapter.getSelectPosition()).getResultState() != -999) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getResultState() == -999) {
                    resultAdapter.setSelectPosition(i);
                    roundNo = i + 1;
                    resultAdapter.notifyDataSetChanged();
                    return true;
                } else {
                    if (isFullSkip(resultList.get(i).getResult(), resultList.get(i).getResultState())) {
                        toastSpeak("满分");
                        return false;
                    }
                }
            }
            toastSpeak("该考生已全部测试完成");

            return false;
        } else {
            roundNo = resultAdapter.getSelectPosition() + 1;
            return true;
        }

    }

    private void prepareForBegin() {
        TestCache testCache = TestCache.getInstance();
        Student student = pairs.get(0).getStudent();
        List<RoundResult> scoreResultList = new ArrayList<>();
        RoundResult result = DBManager.getInstance().queryBestScore(student.getStudentCode(), testCache.getTestNoMap().get(student));
        if (result != null) {
            scoreResultList.add(result);
        }
        InteractUtils.showStuInfo(llStuDetail, student, scoreResultList);
        tvResult.setText(student.getStudentName());
        state = WAIT_CHECK_IN;
        setOperationUI();
    }

    @OnClick(R.id.tv_pair)
    public void onViewClicked() {
        LogUtils.operation("跳转至篮球设备配对界面");
        IntentUtil.gotoActivity(this, SportPairActivity.class);
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
    private void showIllegalReturnDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.illegal_return_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                LogUtils.operation("篮球违规返回弹窗点击了确定...");
                sweetAlertDialog.dismissWithAnimation();
                timerTask.stopKeepTime();
                sportPresent.setDeviceStateStop();
                state = WAIT_CHECK_IN;
                setOperationUI();
                InteractUtils.showStuInfo(llStuDetail,null,null);
                resultList.clear();
                resultAdapter.notifyDataSetChanged();
                tvResult.setText("");
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                LogUtils.operation("篮球违规返回弹窗点击了取消...");
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

    }

    @Override
    public void updateDeviceState(int deviceId, int state) {
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
        mHandler.sendEmptyMessage(UPDATE_ON_WAIT);
    }

    @Override
    public void receiveResult(SportResult sportResult) {

    }

    @Override
    public void getDeviceStop() {
        sportPresent.setRunState(0);
        mHandler.sendEmptyMessage(UPDATE_ON_STOP);
    }

    @Override
    public void onTimeTaskUpdate(int time) {
        Message message = mHandler.obtainMessage();
        message.what = UPDATE_ON_TEXT;
        message.arg1 = time;
        mHandler.sendMessage(message);
    }
}
