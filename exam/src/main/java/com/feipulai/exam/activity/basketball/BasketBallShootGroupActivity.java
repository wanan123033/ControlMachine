package com.feipulai.exam.activity.basketball;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.RunTimerManager;
import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.beans.RunTimerConnectState;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.adapter.ShootResultAdapter;
import com.feipulai.exam.activity.basketball.bean.BasketBallShootResult;
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
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.PrintResultUtil;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

public class BasketBallShootGroupActivity extends BaseTitleActivity implements BaseQuickAdapter.OnItemClickListener {
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
    @BindView(R.id.txt_run)
    TextView txtContinueRun;
    @BindView(R.id.txt_stop_timing)
    TextView txtStopTiming;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
    @BindView(R.id.tv_print)
    TextView tvPrint;
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    @BindView(R.id.txt_finish_test)
    TextView txtFinishTest;

    private ShootSetting setting;
    private Group group;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private List<String> resultList = new ArrayList<>();
    private List<BasketBallShootResult> ballShootResults = new ArrayList<>();
    private ShootResultAdapter resultAdapter;
    private int roundNo;
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    protected volatile int state = WAIT_FREE;
    private String[] result;
    private volatile int order;
    private static final String TAG = "BasketBallShootGroupAct";
    private volatile int connect;
    private String name;
    private String testDate;
    private boolean saved;
    List<BaseStuPair> stuPairs;
    protected int setLayoutResID() {
        return R.layout.activity_basket_ball_shoot_group;
    }

    @Override
    protected void initData() {
        //??????????????????
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        if (setting == null)
            setting = new ShootSetting();

        //????????????
        group = (Group) TestConfigs.baseGroupMap.get("group");
        String type = "????????????";
        if (group.getGroupType() == Group.MALE) {
            type = "??????";
        } else if (group.getGroupType() == Group.FEMALE) {
            type = "??????";
        }
        tvGroupName.setText(String.format(Locale.CHINA, "%s???%d???", type, group.getGroupNo()));
        //????????????????????????
        TestCache.getInstance().init();

        stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
        pairs = CheckUtils.newPairs(stuPairs.size(),stuPairs);
//        LogUtils.operation("???????????????????????????:" + pairs.size() + "---" + pairs.toString());
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setOnItemClickListener(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, TestConfigs.getMaxTestCount(this));
        rvTestResult.setLayoutManager(layoutManager);
        result = new String[TestConfigs.getMaxTestCount(this)];
        resultList.addAll(Arrays.asList(result));
        resultAdapter = new ShootResultAdapter(resultList);
        rvTestResult.setAdapter(resultAdapter);
        resultAdapter.notifyDataSetChanged();

        firstCheckTest();

        SerialDeviceManager.getInstance().setRS232ResiltListener(runTimer);
        int hostId = SettingHelper.getSystemSetting().getHostId();
        RunTimerManager.cmdSetting(1, hostId, 1, -1, -1, -1);
        checkConnect();
    }

    private int position() {
        return stuPairAdapter.getTestPosition();
    }

    private void firstCheckTest() {
        //????????????????????????????????????????????????
        for (int i = 0; i < pairs.size(); i++) {
            if (!isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                stuPairAdapter.setTestPosition(i);
                rvTestingPairs.scrollToPosition(i);
                presetResult();
                isExistTestPlace();
                prepareForBegin();
                LogUtils.operation("????????????" + pairs.get(position()).getStudent().getSpeakStuName() + "?????????" + roundNo + "?????????");
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

    private void presetResult() {
        resultList.clear();
        ballShootResults.clear();
        Student student = TestCache.getInstance().getAllStudents().get(position());
        Map<Student, List<RoundResult>> map = TestCache.getInstance().getResults();
        if (map != null) {
            List<RoundResult> results = map.get(student);
            roundNo = (results == null ? 1 : results.size() + 1);
            for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {
                if (results != null && results.size()>i) {
                    result[i] = InteractUtils.getDisplayResult(results.get(i));
                    ballShootResults.add(new BasketBallShootResult(roundNo, InteractUtils.getDisplayResult(results.get(i)),
                            results.get(i).getResult(), 0, results.get(i).getResultState()));
                } else {
                    ballShootResults.add(new BasketBallShootResult(roundNo, "", -999, 0, -999));
                    result[i] = "";
                    if (resultAdapter.getSelectPosition() == -1) {
                        resultAdapter.setSelectPosition(i);
                    }
                }

            }
            resultList.addAll(Arrays.asList(result));

        } else {
            roundNo = 1;
        }
        resultAdapter.notifyDataSetChanged();
    }

    /**
     * ??????
     */
    private void prepareForBegin() {
        LogUtils.operation("????????????:" + pairs.get(position()).getStudent().getSpeakStuName() + "?????????" + roundNo + "?????????");
        Student student = pairs.get(position()).getStudent();
        name = student.getStudentName();
        tvResult.setText(name);
        state = WAIT_CHECK_IN;
        setOperationUI();
//        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, student.getLEDStuName(), Paint.Align.LEFT);
//        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.CENTER);
    }

    private boolean isFullSkip(int result, int resultState) {
        Student student = pairs.get(position()).getStudent();
        if (setting.isFullSkip() && resultState == RoundResult.RESULT_STATE_NORMAL) {
            if (student.getSex() == Student.MALE) {
                return result <= setting.getMaleFullShoot();
            } else {
                return result <= setting.getFemaleFullShoot();
            }
        }
        return false;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param studentCode
     * @return
     */
    private boolean isStuAllTest(String studentCode) {
        //  ?????????????????? ???????????????????????????????????????
        List<RoundResult> roundResultList = DBManager.getInstance().queryGroupRound
                (studentCode, group.getId() + "");
        //????????????????????????????????????
        if (roundResultList.size() < TestConfigs.getMaxTestCount(this)) {
            boolean isSkip = false;
            for (RoundResult roundResult : roundResultList) {
                //??????????????????????????????
                if (isFullSkip(roundResult.getResult(), roundResult.getResultState())) {
                    LogUtils.operation("????????????:" + studentCode + "???" + roundNo + "????????????,????????????");
                    isSkip = true;
                }
            }
            if (!isSkip) {
                return false;
            }
        }
        return true;

    }

    /**
     * ???????????????????????????
     */
    private boolean isExistTestPlace() {
        if (resultAdapter.getSelectPosition() == -1)
            return false;
        if (ballShootResults.get(resultAdapter.getSelectPosition()).getResultState() != -999) {
            for (int i = 0; i < resultList.size(); i++) {
                if (ballShootResults.get(i).getResultState() == -999) {
                    resultAdapter.setSelectPosition(i);
                    roundNo = i + 1;
                    resultAdapter.notifyDataSetChanged();
                    return true;
                } else {
                    if (isFullSkip(ballShootResults.get(i).getResult(), ballShootResults.get(i).getResultState())) {
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

    /**
     * ?????????????????????
     */
    private void allTestComplete() {
        LogUtils.operation("??????????????????????????????");
        //????????????????????????
        toastSpeak("???????????????????????????????????????????????????");
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
        state = WAIT_FREE;
        setOperationUI();

    }

    /**
     * ??????????????????????????????UI
     */
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

    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {

    }

    private RunTimerImpl runTimer = new RunTimerImpl(new RunTimerImpl.RunTimerListener() {
        @Override
        public void onGetTime(RunTimerResult result) {
            getResult(result);
        }

        @Override
        public void onConnected(RunTimerConnectState connectState) {

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
        }
    });

    private void getResult(RunTimerResult result) {
        if (result.getTrackNum() == 1) {//???????????????1??????
            order = result.getOrder();
            mHandler.sendEmptyMessage(UPDATE_RESULT);
        }
    }

    public void disposeConnect(RunTimerConnectState connectState) {
        mHandler.sendEmptyMessage(CONNECT);
        connect = 0;
    }

    /**
     * @param testState
     */
    public void changeState(int testState) {
        switch (testState) {
            case 0://??????
            case 1:
            case 5://????????????
                state = WAIT_FREE;
                break;
            case 2://????????????
                state = WAIT_BEGIN;
                break;
            case 3://??????
                state = TESTING;
                if (timer!= null){
                    timer.dispose();
                }
                countDownTime(60);
                saved = false;
                testDate = System.currentTimeMillis() + "";
                break;
            case 4://???????????????
                state = TESTING;
                break;
            case 6://????????????
                state = WAIT_FREE;
                break;

        }
        setOperationUI();
    }

    private Disposable timer;

    private void countDownTime(final int time) {
        timer = Observable.interval(1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (timer == null) {
                            return;
                        }
                        if (!timer.isDisposed() && aLong <= time) {
                            txtDeviceStatus.setText(String.format("?????? %d", time - aLong));
                        }
                        if (aLong > time) {
                            timer.dispose();
                            txtDeviceStatus.setText("??????");
                            RunTimerManager.stopRun();
                        }
                    }

                });

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
                    String result = ResultDisplayUtils.getStrResultForDisplay(order);
                    tvResult.setText(name + result);
                    break;
            }
            return false;
        }
    });

    @OnClick({R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_run, R.id.txt_stop_timing,
            R.id.tv_print, R.id.tv_confirm, R.id.txt_finish_test})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.txt_waiting:
                RunTimerManager.waitStart();
                break;
            case R.id.txt_illegal_return:
                RunTimerManager.illegalBack();
                break;
            case R.id.txt_run:
                RunTimerManager.forceStart();
                break;
            case R.id.txt_stop_timing:
                if (timer != null && !timer.isDisposed()) {
                    timer.dispose();
                }

                break;

            case R.id.tv_print:
                if (state == TESTING){
                    toastSpeak("?????????????????????");
                    return;
                }
                LogUtils.operation("?????????????????????");
                showPrintDialog();
                break;
            case R.id.tv_confirm:
                if (state == TESTING){
                    toastSpeak("????????????????????????");
                    return;
                }
                if (saved) {
                    toastSpeak("??????????????????????????????");
                    return;
                }
                saveResult();
                saved = true;
                break;
            case R.id.txt_finish_test:

                break;
        }
    }

    private void showPrintDialog() {
        String[] printType = new String[]{"??????", "??????"};
        new AlertDialog.Builder(this).setTitle("????????????????????????")
                .setItems(printType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TestCache testCache = TestCache.getInstance();
                        switch (which) {
                            case 0:
                                List<RoundResult> stuResult = testCache.getResults().get(pairs.get(position()).getStudent());
                                if (stuResult == null || stuResult.size() == 0) {
                                    toastSpeak("????????????????????????");
                                    return;
                                }
                                PrintResultUtil.printResult(pairs.get(position()).getStudent().getStudentCode());
                                break;
                            case 1:

                                InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                                        TestConfigs.getMaxTestCount(BasketBallShootGroupActivity.this), testCache.getTrackNoMap());
                                break;
                        }
                    }
                }).create().show();
    }

    private void saveResult() {
        if (pairs.get(position()) == null)
            return;
        resultList.clear();
        result[roundNo - 1] = ResultDisplayUtils.getStrResultForDisplay(order);
        resultList.addAll(Arrays.asList(result));
        resultAdapter.notifyDataSetChanged();
        ballShootResults.get(roundNo - 1).setResult(order);
        ballShootResults.get(roundNo - 1).setResultState(RoundResult.RESULT_STATE_NORMAL);
        addRoundResult(order);
        uploadResults();
        nextTest();
    }

    /**
     * ???????????????????????????????????????
     */
    private void nextTest() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //????????????
                if (setting.getTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
                    continuousTest();
                } else {
                    //??????
                    loopTestNext();
                }
            }
        }, 3000);
    }

    /**
     * ????????????
     */
    private void continuousTest() {
        if (roundNo < TestConfigs.getMaxTestCount(this)) {
            //??????????????????????????????
            if (isExistTestPlace()) {
                prepareForBegin();
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
                LogUtils.operation("????????????:" + pairs.get(position()).getStudent().getSpeakStuName() + "?????????" + roundNo + "?????????");
            } else {
                continuousTestNext();
            }
        } else {
            //???????????????????????????
            if (position() == pairs.size() - 1) {
                firstCheckTest();
            } else {
                continuousTestNext();
            }


        }
    }

    /**
     * ?????????????????????
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
            //???????????????????????????
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            LogUtils.operation("????????????:" + pairs.get(position()).getStudent().getSpeakStuName() + "?????????" + roundNo + "?????????");

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);
            stuPairAdapter.notifyDataSetChanged();

            return;
        }
        //????????????????????????
        firstCheckTest();

    }

    /**
     * ???????????????????????????
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
            //???????????????????????????
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            LogUtils.operation("????????????" + pairs.get(position()).getStudent().getSpeakStuName() + "?????????" + 1 + "?????????" + roundNo + "?????????");
            stuPairAdapter.notifyDataSetChanged();

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);

            return;
        }
        firstCheckTest();
    }

    /**
     * ?????????????????????
     */
    private void addRoundResult(int result) {
        Student student = pairs.get(position()).getStudent();
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(result);
        roundResult.setMachineResult(result);
        if (pairs.get(position()).getCurrentRoundNo() != 0){
            roundResult.setRoundNo(pairs.get(position()).getCurrentRoundNo());
            pairs.get(position()).setCurrentRoundNo(0);
            roundResult.setResultTestState(RoundResult.RESULT_RESURVEY_STATE);
        }else {
            roundResult.setRoundNo(roundNo);
            roundResult.setResultTestState(0);
        }
        roundResult.setTestNo(1);
        GroupItem groupItem = DBManager.getInstance().getItemStuGroupItem(group,student.getStudentCode());
        if (group.getExamType() == StudentItem.EXAM_MAKE){
            roundResult.setExamType(group.getExamType());
        }else {
            roundResult.setExamType(groupItem.getExamType());
        }
        roundResult.setScheduleNo(group.getScheduleNo());
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(testDate);
        roundResult.setEndTime(System.currentTimeMillis() + "");
        roundResult.setGroupId(group.getId());
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
        // ????????????????????????
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
        LogUtils.operation("??????????????????????????????: " + roundResult.toString());
        DBManager.getInstance().insertRoundResult(roundResult);

        //??????????????????????????????????????????
        List<RoundResult> results = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
        TestCache.getInstance().getResults().put(student, results);
        if (groupItem!=null&&groupItem.getExamType() == StudentItem.EXAM_MAKE){
            continuousTestNext();
        }

        SystemSetting systemSetting = SettingHelper.getSystemSetting();
        //?????????????????????????????? ????????????????????????????????????
        if (systemSetting.isGroupCheck() && setting.getTestPattern() == TestConfigs.GROUP_PATTERN_LOOP){
            finish();
        }
        if (systemSetting.isGroupCheck() && setting.getTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE && TestConfigs.getMaxTestCount() == roundNo){
            finish();
        }
    }

    /**
     * ????????????
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
            Logger.i("??????????????????:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.dispose();
        }
    }


}
