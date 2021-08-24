package com.feipulai.exam.activity.basketball;

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
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.adapter.DribbleShootAdapter;
import com.feipulai.exam.activity.basketball.adapter.DribbleShootResultAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallResult;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.basketball.util.RunTimerImpl;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.VolleyBallGroupStuAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.PrintResultUtil;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DribbleShootGroupActivity extends BaseTitleActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestingPairs;
    @BindView(R.id.tv_group_name)
    TextView tvGroupName;
    @BindView(R.id.cb_connect)
    CheckBox cbConnect;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.txt_device_status)
    TextView txtDeviceStatus;
    @BindView(R.id.tv_punish_add)
    TextView tvPunishAdd;
    @BindView(R.id.tv_punish_subtract)
    TextView tvPunishSubtract;
//    @BindView(R.id.tv_foul)
//    TextView tvFoul;
//    @BindView(R.id.tv_inBack)
//    TextView tvInBack;
//    @BindView(R.id.tv_abandon)
//    TextView tvAbandon;
//    @BindView(R.id.tv_normal)
//    TextView tvNormal;
    @BindView(R.id.txt_waiting)
    TextView txtWaiting;
    @BindView(R.id.txt_illegal_return)
    TextView txtIllegalReturn;
    @BindView(R.id.txt_continue_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.view_list_head)
    LinearLayout viewListHead;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.rv_state)
    RecyclerView rvState;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;
    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dribble_shoot_group);
//    }
    private static final String TAG = "DribbleShootGroupAct";
    private ShootSetting setting;
    private Group group;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private List<BasketBallTestResult> resultList = new ArrayList<>();
    private DribbleShootResultAdapter resultAdapter;
    private int roundNo;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    protected volatile int state = WAIT_FREE;
    private boolean saved;
    private String testDate;
    private int startNo;
    private int back1No;
    private int back2No;
    private int timeResult;
    private volatile int connect;
    private volatile int trackNum;
    private List<BasketBallResult> dateList = new ArrayList<>();
    private DribbleShootAdapter interceptAdapter;
    private String[] interceptRound = new String[]{"1起点", "2折返1", "3投篮", "4折返2", "5投篮", "6折返1", "7投篮", "8折返2", "9投篮"};
    private List<MachineResult> machineResultList = new ArrayList<>();
    private Student student;
    private List<BaseStuPair> stuPairs;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_dribble_shoot_group;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        if (setting == null)
            setting = new ShootSetting();

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
        LogUtils.operation("篮球获取到分组学生:" + pairs.size() + "---" + pairs.toString());
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setOnItemClickListener(this);

        resultAdapter = new DribbleShootResultAdapter(resultList);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isConfigurableNow()) {
                    resultAdapter.setSelectPosition(position);
                    resultAdapter.notifyDataSetChanged();
                }
            }
        });

        for (int i = 0; i < setting.getInterceptNo(); i++) {
            BasketBallResult ballResult = new BasketBallResult();
            ballResult.setName(interceptRound[i]);
            ballResult.setState(false);
            dateList.add(ballResult);
        }
        startNo = setting.getStartNo();
        back1No = setting.getBack1No();
        back2No = setting.getBack2No();
        interceptAdapter = new DribbleShootAdapter(this, dateList);
        rvState.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvState.setAdapter(interceptAdapter);
        firstCheckTest();
        SerialDeviceManager.getInstance().setRS232ResiltListener(runTimer);
        int hostId = SettingHelper.getSystemSetting().getHostId();
        RunTimerManager.cmdSetting(setting.getInterceptNo(), hostId, 1, -1, -1, -1);
        checkConnect();
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

    private void allTestComplete() {
        LogUtils.operation("篮球分组模式测试完成");
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
        state = WAIT_FREE;
        setOperationUI();
    }

    private void setOperationUI() {
        switch (state) {
            case WAIT_BEGIN:
                txtContinueRun.setEnabled(true);
                txtIllegalReturn.setEnabled(true);
                txtStopTiming.setEnabled(false);
                txtWaiting.setEnabled(false);
                break;
            case WAIT_FREE:
                txtContinueRun.setEnabled(false);
                txtIllegalReturn.setEnabled(false);
                txtStopTiming.setEnabled(false);
                txtWaiting.setEnabled(true);
                break;
            case TESTING:
                txtContinueRun.setEnabled(false);
                txtIllegalReturn.setEnabled(true);
                txtStopTiming.setEnabled(true);
                txtWaiting.setEnabled(false);
                break;
        }
    }

    private void prepareForBegin() {
        LogUtils.operation("篮球考生:" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + roundNo + "轮测试");
        Student student = pairs.get(position()).getStudent();
        tvResult.setText(student.getStudentName());
        state = WAIT_CHECK_IN;
        setOperationUI();
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
                        return false;
                    }
                }
            }
            return false;
        } else {
            roundNo = resultAdapter.getSelectPosition() + 1;
            return true;
        }
    }

    private boolean isFullSkip(int result, int resultState) {
        Student student = pairs.get(position()).getStudent();
        if (setting.isFullSkip() && resultState == RoundResult.RESULT_STATE_NORMAL) {
//            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            if (student.getSex() == Student.MALE) {
                return result <= setting.getMaleFullDribble() * 1000;
            } else {
                return result <= setting.getFemaleFullDribble() * 1000;
            }
        }
        return false;
    }

    /**
     * 考生是否全部测试完成或满分
     *
     * @param studentCode
     * @return
     */
    private boolean isStuAllTest(String studentCode) {
        //  查询学生成绩 当有成绩则添加数据跳过测试
        List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                (studentCode, group.getId() + "");
        SystemSetting setting = SettingHelper.getSystemSetting();
        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),stuPairs.get(stuPairAdapter.getTestPosition()).getStudent().getStudentCode());
        //判断是否开启补考需要加上是否已完成本次补考,并将学生改为已补考
        if ((setting.isResit() || studentItem.getMakeUpType() == 1) && !stuPairs.get(stuPairAdapter.getTestPosition()).isResit()){
            roundResultList.clear();
        }
        //成绩数量是否小于测试次数
        if (roundResultList.size() < setTestCount()) {
            boolean isSkip = false;
            for (RoundResult roundResult : roundResultList) {
                //成绩是否存在满分跳过
                if (isFullSkip(roundResult.getResult(), roundResult.getResultState())) {
                    LogUtils.operation("篮球考生:" + studentCode + "第" + roundNo + "轮已满分,跳过测试");
                    isSkip = true;
                }
            }
            if (!isSkip) {
                return false;
            }
        }
        return true;

    }

    private void presetResult() {
        resultList.clear();
        resultAdapter.setSelectPosition(-1);
        Student student = TestCache.getInstance().getAllStudents().get(position());
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        roundNo = (roundResults == null ? 1 : roundResults.size() + 1);
        for (int i = 0; i < setTestCount(); i++) {
            RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId() + "", i + 1);
            if (roundResult == null) {
                resultList.add(new BasketBallTestResult(i + 1, new ArrayList<MachineResult>(), 0, -999, 0, -999));
                if (resultAdapter.getSelectPosition() == -1) {
                    resultAdapter.setSelectPosition(i);
                }
            } else {
                List<MachineResult> machineResultList = DBManager.getInstance().getItemGroupFRoundMachineResult(student.getStudentCode(),
                        group.getId(), i + 1);
                resultList.add(new BasketBallTestResult(i + 1, machineResultList, roundResult.getMachineResult(), roundResult.getResult(), roundResult.getPenaltyNum(), roundResult.getResultState()));

            }

        }
        resultAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
        if (!isConfigurableNow()) {
            resultAdapter.setSelectPosition(-1);
            stuPairAdapter.setTestPosition(i);
            rvTestingPairs.scrollToPosition(i);
            stuPairAdapter.notifyDataSetChanged();
            presetResult();
            if (isExistTestPlace()) {
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
                prepareForBegin();
            } else {
                state = WAIT_FREE;
                setOperationUI();
            }

        } else {
            ToastUtils.showShort("测试中,不能更换考生");
        }
    }

    private boolean isConfigurableNow() {
        return !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
    }

    private int position() {
        return stuPairAdapter.getTestPosition();
    }

    private RunTimerImpl runTimer = new RunTimerImpl(new RunTimerImpl.RunTimerListener() {
        @Override
        public void onGetTime(RunTimerResult result) {
            getResult(result);
        }

        @Override
        public void onConnected(RunTimerConnectState connectState) {
            Log.i(TAG, connectState.toString());
            disposeConnect(connectState);
        }

        @Override
        public void onTestState(final int testState) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    changeState(testState);
                }
            });
//            switch (testState) {
//                case 0:
//                case 1:
//                case 5://违规返回
//
//                    break;
//                case 2://等待计时
//
//                    break;
//                case 3://启动
//                    break;
//                case 4://获取到结果
//                case 6://停止计时
//                    break;
//            }
        }
    });


    private void getResult(RunTimerResult result) {
        Log.i(TAG, "resultTime" + result.toString());

        if (result.getTrackNum() == startNo && result.getOrder() == 1) {
//            baseTimer = System.currentTimeMillis() - baseTimer;
            timeResult = 0;
            machineResultList.clear();
            student = pairs.get(position()).getStudent();
            keepTime();

        } else {
//            timeResult = (int) (result.getResult() - baseTimer);
            timeResult = rxTime.intValue()*100;
        }
        trackNum = result.getTrackNum();
        Message msg = mHandler.obtainMessage();
        msg.what = UPDATE_RESULT;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    public void disposeConnect(RunTimerConnectState connectState) {
        mHandler.sendEmptyMessage(CONNECT);
        connect = 0;
    }


    public void changeState(int testState) {
        switch (testState) {
            case 0://设置
            case 1:
            case 5://违规返回
                state = WAIT_FREE;
                break;
            case 2://等待计时
                state = WAIT_BEGIN;
                break;
            case 3://启动
                state = TESTING;
                if (timer!= null){
                    timer.dispose();
                }
                keepTime();
                saved = false;
                testDate = System.currentTimeMillis() + "";
                break;
            case 4://获取到结果
                state = TESTING;
                break;
            case 6://停止计时
                state = WAIT_FREE;
                break;

        }
        setOperationUI();
    }

    Disposable timer;
    private Long rxTime = new Long(0);

    private void keepTime() {
        testDate = DateUtil.getCurrentTime()+"";
        timer = Observable.interval(100, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (timer == null) {
                            return;
                        }
                        rxTime = aLong;
                        if (!timer.isDisposed()) {
                            txtDeviceStatus.setText(String.format("计时%s", ResultDisplayUtils.getStrResultForDisplay(aLong.intValue() * 100, false)));
                        }

                    }

                });

    }


    @OnClick({R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_continue_run, R.id.txt_stop_timing,
            R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting:
                RunTimerManager.waitStart();
                break;
            case R.id.txt_illegal_return:
                RunTimerManager.illegalBack();
                break;
            case R.id.txt_continue_run:
                RunTimerManager.forceStart();
                saved = false;
                break;
            case R.id.txt_stop_timing:
                RunTimerManager.stopRun();
                break;
            case R.id.tv_print:
                showPrintDialog();
                break;
            case R.id.tv_confirm:
                if (state == TESTING)
                    return;
                if (saved) {
                    toastSpeak("已保存，请勿重复点击");
                    return;
                }
                saveResult();
                saved = true;
                break;
            case R.id.txt_finish_test:

                break;
        }
    }

    ScheduledExecutorService service = Executors
            .newSingleThreadScheduledExecutor();

    private void checkConnect() {
        Runnable runnable = new Runnable() {
            public void run() {
                if (connect > 3 && cbConnect.isChecked()) {
                    mHandler.sendEmptyMessage(UN_CONNECT);
                }
                connect++;
            }
        };
        service.scheduleAtFixedRate(runnable, 1, 1, TimeUnit.SECONDS);

    }

    private static final int CONNECT = 1;
    private static final int UN_CONNECT = 2;
    private static final int UPDATE_RESULT = 3;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT:
                    cbConnect.setChecked(true);
                    break;
                case UN_CONNECT:
                    cbConnect.setChecked(false);
                    break;
                case UPDATE_RESULT:
                    RunTimerResult result = (RunTimerResult) msg.obj;
                    tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(rxTime.intValue()*100, false));
                    updateMachineDate();
                    if (trackNum == 1){//投篮
                        if (result.getOrder() == 1){
                            dateList.get(2).setState(true);
                        }else if (dateList.size()>4 && result.getOrder() == 2){
                            dateList.get(4).setState(true);
                        }else if (dateList.size()>7 && result.getOrder() == 3){
                            dateList.get(6).setState(true);
                        }else if (dateList.size()==9 && result.getOrder() == 4){
                            dateList.get(8).setState(true);
                        }
                    }
                    else if (trackNum == startNo){//起点
                        dateList.get(0).setState(true);
                    }else if (trackNum == back1No){
                        if (result.getOrder() == 1){
                            dateList.get(1).setState(true);
                        }else if (dateList.size()>5 && result.getOrder() == 2){
                            dateList.get(5).setState(true);
                        }

                    }
                    else if (trackNum == back2No){
                        if (result.getOrder() == 1){
                            dateList.get(3).setState(true);
                        }else if (dateList.size()>8 && result.getOrder() == 2){
                            dateList.get(7).setState(true);
                        }

                    }

                    interceptAdapter.notifyDataSetChanged();
                    boolean stop = true;
                    for (int i = 0; i < dateList.size(); i++) {
                        if (!dateList.get(i).isState()){
                            stop = false;
                            break;
                        }
                    }
                    if (stop){//所有拦截完成
                        timer.dispose();
                        RunTimerManager.stopRun();
                    }
                    break;
            }
            return false;
        }
    });

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
                                        TestConfigs.getMaxTestCount(DribbleShootGroupActivity.this), testCache.getTrackNoMap());
                                break;
                        }
                    }
                }).create().show();
    }

    /**
     * 判罚成绩
     *
     * @param punishType 正数 +1 负数 -1
     */
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
            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            testResult.setResult(result);

            resultAdapter.notifyDataSetChanged();
        }
    }

    private void updateMachineDate(){
        MachineResult machineResult = new MachineResult();
        machineResult.setItemCode(TestConfigs.getCurrentItemCode());
        machineResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        machineResult.setTestNo(1);
        machineResult.setRoundNo(roundNo);
        machineResult.setStudentCode(student.getStudentCode());
        machineResult.setResult(timeResult);
        machineResultList.add(machineResult);
        resultList.get(position()).setMachineResultList(machineResultList);
        resultAdapter.notifyDataSetChanged();
    }

    private void saveResult() {

        addRoundResult();//将成绩保存到数据库
        showLedConfirmedResult();//LED显示
        uploadResults();
        nextTest();

        dateList.clear();
        for (int i = 0; i < setting.getInterceptNo(); i++) {
            BasketBallResult ballResult = new BasketBallResult();
            ballResult.setName(interceptRound[i]);
            ballResult.setState(false);
            dateList.add(ballResult);
        }
        interceptAdapter.notifyDataSetChanged();

    }

    private void showLedConfirmedResult() {

    }

    /**
     * 保存数据
     * @param
     */
    private void addRoundResult() {

        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(timeResult);
        roundResult.setMachineResult(timeResult);
        if (pairs.get(position()).getCurrentRoundNo() != 0){
            roundResult.setRoundNo(pairs.get(position()).getCurrentRoundNo());
            pairs.get(position()).setCurrentRoundNo(0);
        }else {
            roundResult.setRoundNo(roundNo);
        }
        roundResult.setTestNo(1);
//        roundResult.setExamType(group.getExamType());
        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),student.getStudentCode());
        if (studentItem != null){
            roundResult.setExamType(studentItem.getExamType());
        }
        roundResult.setScheduleNo(group.getScheduleNo());
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(testDate);
        roundResult.setEndTime(System.currentTimeMillis() + "");
        roundResult.setGroupId(group.getId());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        // 重新判断最好成绩
        RoundResult bestResult = InteractUtils.getBestResult(TestCache.getInstance().getResults().get(student));
        // Log.i("james", "\nroundResult:" + roundResult.toString());
        if (bestResult != null && bestResult.getResult() < roundResult.getResult()) {
            roundResult.setIsLastResult(0);
            // Log.i("james", "bestResult" +  bestResult.toString());
        } else {
            roundResult.setIsLastResult(1);
            if (bestResult != null) {
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
            }
        }
        LogUtils.operation("篮球确认保存成绩:result = " + roundResult.getResult() + "---" + roundResult.toString());
        DBManager.getInstance().insertRoundResult(roundResult);
        //获取所有成绩设置为非最好成绩
        SystemSetting setting = SettingHelper.getSystemSetting();
        //判断是否开启补考需要加上是否已完成本次补考,并将学生改为已补考
        if ((setting.isResit() || studentItem.getMakeUpType() == 1) && !stuPairs.get(stuPairAdapter.getTestPosition()).isResit()){
            stuPairs.get(stuPairAdapter.getTestPosition()).setResit(true);
        }
        List<RoundResult> results = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
        TestCache.getInstance().getResults().put(student, results);
        if (studentItem.getExamType() == 2){
            continuousTestNext();
        }

    }

    /**
     * 下一轮测试或下一个学生测试
     */
    private void nextTest() {
        timeResult = 0;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                txtDeviceStatus.setText("空闲");
                //连续测试
                if (setting.getTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                    continuousTest();
                } else {
                    //循环
                    loopTestNext();
                }
            }
        }, 3000);
    }

    /**
     * 连续测试
     */
    private void continuousTest() {
        if (roundNo < TestConfigs.getMaxTestCount(this)) {
            //是否存在可以测试位置
            if (isExistTestPlace()) {
                prepareForBegin();
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
                LogUtils.operation("篮球考生:" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + roundNo + "轮测试");
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

            if (isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                continue;
            }
            stuPairAdapter.setTestPosition(i);
            rvTestingPairs.scrollToPosition(i);
            prepareForBegin();
            presetResult();
            //最后一次测试的成绩
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            LogUtils.operation("篮球考生:" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + roundNo + "轮测试");

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
            LogUtils.operation("篮球考生" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + 1 + "次的第" + roundNo + "轮测试");
            stuPairAdapter.notifyDataSetChanged();

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);

            return;
        }
        firstCheckTest();
    }

    /**
     * 上传成绩
     */
    private void uploadResults() {
        if (SettingHelper.getSystemSetting().isRtUpload()) {
            Student student = pairs.get(position()).getStudent();
            List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
            if (TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
                return;
            }
            if (roundResultList.size() == 0 || roundResultList == null) {
                return;
            }
            List<UploadResults> uploadResults = new ArrayList<>();
            String groupNo = group.getGroupNo() + "";
            String scheduleNo = group.getScheduleNo();
            String testNo = "1";
            UploadResults uploadResult = new UploadResults(scheduleNo,
                    TestConfigs.getCurrentItemCode(), student.getStudentCode()
                    , testNo, group, RoundResultBean.beanCope(roundResultList, group));
            uploadResults.add(uploadResult);
            Logger.i("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer!=null)
            timer.dispose();
    }
    private int setTestCount() {
//        SystemSetting setting = SettingHelper.getSystemSetting();
//        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),stuPairs.get(position()).getStudent().getStudentCode());
//        if (setting.isResit() || studentItem.getMakeUpType()==1){
//            return stuPairs.get(position()).getTestNo() == -1 ? TestConfigs.getMaxTestCount() : stuPairs.get(position()).getTestNo();
//        }
        return TestConfigs.getMaxTestCount();
    }
}
