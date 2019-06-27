package com.feipulai.exam.activity.basketball;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.adapter.BasketBallResultAdapter;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.basketball.util.TimerUtil;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.check.CheckUtils;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.VolleyBallGroupStuAdapter;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

public class BasketBallGroupActivity extends BaseTitleActivity implements BasketBallListener.BaketBallResponseListener, TimerUtil.TimerAccepListener, BaseQuickAdapter.OnItemClickListener {


    @BindView(R.id.rv_testing_pairs)
    RecyclerView rvTestingPairs;
    @BindView(R.id.tv_group_name)
    TextView tvGroupName;
    @BindView(R.id.tv_result)
    TextView tvResult;
    @BindView(R.id.rv_test_result)
    RecyclerView rvTestResult;
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
    @BindView(R.id.tv_confirm)
    TextView tvConfirm;
    // 状态 WAIT_FREE---> WAIT_CHECK_IN---> WAIT_BEGIN--->TESTING---->WAIT_STOP---->WAIT_CONFIRM--->WAIT_CHECK_IN
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    protected volatile int state = WAIT_FREE;
    private Group group;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private VolleyBallGroupStuAdapter stuPairAdapter;
    private List<BasketBallTestResult> resultList = new ArrayList<>();
    private BasketBallResultAdapter resultAdapter;

    private BasketBallSetting setting;
    private TimerUtil timerUtil;
    private String testDate;
    private int roundNo;
    private StudentItem mStudentItem;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_basketball;
    }

    @Override
    protected void initData() {
        //获取项目设置
        setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
        if (setting == null)
            setting = new BasketBallSetting();
        UdpClient.getInstance().init(1527);
        timerUtil = new TimerUtil(this);
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
        stuPairAdapter.setOnItemClickListener(this);

        resultAdapter = new BasketBallResultAdapter(resultList, setting);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(resultAdapter);
        presetResult();
        resultAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isConfigurableNow()) {
                    resultAdapter.setSelectPosition(position);
                    resultAdapter.notifyDataSetChanged();
                }
            }
        });
        setOperationUI();
        UdpClient.getInstance().setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(), new BasketBallListener(this));
        //设置精度
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1));
        prepareForBegin();
    }


    @Override
    public void finish() {
        if (isConfigurableNow()) {
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
        timerUtil.stop();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
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

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
    }

    @Override
    public void getDeviceStatus(int status) {
        switch (status) {
            case 1:
                state = WAIT_FREE;
                txtDeviceStatus.setText("空闲");
                break;
            case 2:
                state = WAIT_BEGIN;
                txtDeviceStatus.setText("等待");
                tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(0));
                break;
            case 3:
                state = TESTING;
                txtDeviceStatus.setText("计时");
                testDate = System.currentTimeMillis() + "";
                break;
            case 4:
                state = WAIT_STOP;
                txtDeviceStatus.setText("停止");
                break;
            case 5:
                state = WAIT_CONFIRM;
                txtDeviceStatus.setText("中断");
                break;
        }
        setOperationUI();
    }

    @Override
    public void triggerStart() {
        state = TESTING;
        txtDeviceStatus.setText("计时");
        testDate = System.currentTimeMillis() + "";
        setOperationUI();
        timerUtil.startTime(10);
    }


    @Override
    public void getResult(BasketballResult result) {
        timerUtil.stop();
        //非测试不做处理
        if (state == WAIT_FREE || state == WAIT_CHECK_IN) {
            return;
        }
        pairs.get(position()).setDeviceResult(result);

        state = WAIT_CONFIRM;
        txtDeviceStatus.setText("中断");
        List<MachineResult> machineResultList = DBManager.getInstance().getItemRoundMachineResult(pairs.get(position()).getStudent().getStudentCode()
                , 1,
                roundNo);

        MachineResult machineResult = new MachineResult();
        machineResult.setItemCode(TestConfigs.getCurrentItemCode());
        machineResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        machineResult.setTestNo(1);
        machineResult.setRoundNo(roundNo);
        machineResult.setStudentCode(pairs.get(0).getStudent().getStudentCode());
        machineResult.setResult(result.getResult());
        //第一次拦截保存成绩，其他拦截只保存
        if (machineResultList.size() == 0 || machineResultList == null) {
            machineResultList.add(machineResult);
            InteractUtils.saveResults(pairs, testDate);
            resultList.get(resultAdapter.getSelectPosition()).setMachineResultList(machineResultList);
            resultList.get(resultAdapter.getSelectPosition()).setSelectMachineResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setPenalizeNum(0);
            resultList.get(resultAdapter.getSelectPosition()).setResultState(RoundResult.RESULT_STATE_NORMAL);
        } else {
            machineResultList.add(machineResult);
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(pairs.get(position()).getStudent().getStudentCode());
            testRoundResult.setResult(result.getResult());
            //更新成绩，最后一次成绩保存
            DBManager.getInstance().updateRoundResult(testRoundResult);
            resultList.get(resultAdapter.getSelectPosition()).setSelectMachineResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setResult(result.getResult());
            resultList.get(resultAdapter.getSelectPosition()).getMachineResultList().clear();
            resultList.get(resultAdapter.getSelectPosition()).getMachineResultList().addAll(machineResultList);
        }

        resultAdapter.notifyDataSetChanged();
        DBManager.getInstance().insterMachineResult(machineResult);
        setOperationUI();
        tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(result.getResult()));
    }

    @Override
    public void getStatusStop(BasketballResult result) {
        //非测试不做处理
        if (state == WAIT_FREE || state == WAIT_CHECK_IN) {
            return;
        }
        timerUtil.stop();
        pairs.get(position()).setDeviceResult(result);
        state = WAIT_STOP;
        txtDeviceStatus.setText("停止");
        setOperationUI();
        tvResult.setText(ResultDisplayUtils.getStrResultForDisplay(result.getResult()));
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
                UdpLEDUtil.getLedByte(ResultDisplayUtils.getStrResultForDisplay(result.getResult()), Paint.Align.RIGHT)));
    }

    @Override
    public void timer(Long time) {
        tvResult.setText(DateUtil.caculateTime(time * 10, TestConfigs.sCurrentItem.getDigital(), 0));
    }

    @Override
    public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
//        if (!isConfigurableNow()) {
//            resultAdapter.setSelectPosition(-1);
//            stuPairAdapter.setTestPosition(i);
//            stuPairAdapter.notifyDataSetChanged();
//            presetResult();
//            if (isExistTestPlace())
//                prepareForBegin();
//        } else {
//            ToastUtils.showShort("测试中,不能更换考生");
//        }
    }

    @OnClick({R.id.tv_punish_add, R.id.tv_punish_subtract, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.tv_print, R.id.tv_confirm
            , R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_continue_run, R.id.txt_stop_timing, R.id.txt_finish_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting://等待发令
                if ((state == WAIT_CHECK_IN || state == WAIT_CONFIRM || state == WAIT_STOP) && isExistTestPlace()) {
                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
                }
                break;
            case R.id.txt_illegal_return://违例返回
                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
                break;
            case R.id.txt_continue_run://继续运行
                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(3));
                break;
            case R.id.txt_stop_timing://停止计时
                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());

                break;
            case R.id.tv_punish_add: //违例+
                setPunish(1);
                break;
            case R.id.tv_punish_subtract://违例-
                setPunish(-1);
                break;
            case R.id.tv_foul://犯规
                setResultState(RoundResult.RESULT_STATE_FOUL);
                break;
            case R.id.tv_inBack://中退
                setResultState(RoundResult.RESULT_STATE_BACK);
                break;
            case R.id.tv_abandon://放弃 ;
                setResultState(RoundResult.RESULT_STATE_WAIVE);
                break;
            case R.id.tv_normal://正常
                setResultState(RoundResult.RESULT_STATE_NORMAL);
                break;
            case R.id.tv_print://打印
                break;
            case R.id.tv_confirm://确定
                tvResult.setText("");
//                pairs.get(0).getDeviceResult().setResult(pairs.get(0).getDeviceResult().getResult());
//                InteractUtils.saveResults(pairs, testDate);
                if (state == WAIT_CONFIRM) {
                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                }
                onResultConfirmed();
                break;
            case R.id.txt_finish_test:
                if (state == TESTING) {
                    toastSpeak("测试中,不允许跳过本次测试");
                } else {
                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                    //TODO 下一位
                }

                break;
        }
    }


    /**
     * 是否是使用中
     */
    private boolean isConfigurableNow() {
        return !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
    }

    private int position() {
        return stuPairAdapter.getTestPosition();
    }

    /**
     * 判罚成绩
     *
     * @param punishType 正数 +1 负数 -1
     */
    private void setPunish(int punishType) {
        if (state == TESTING || state == WAIT_STOP || state == WAIT_BEGIN) {
            toastSpeak("测试中,不允许更改考试成绩");
        } else {
            if (resultAdapter.getSelectPosition() == -1)
                return;
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            if (testResult.getResult() < 0 && testResult.getResultState() == -999) {
                toastSpeak("成绩不存在");
                return;
            }
            int penalizeNum = testResult.getPenalizeNum();
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

    /**
     * 修改成绩状态
     *
     * @param resultState
     */
    private void setResultState(int resultState) {
        //TESTING---->WAIT_STOP
        if (state == TESTING || state == WAIT_STOP) {
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
        }

    }

    private void presetResult() {
        resultList.clear();
        Student student = TestCache.getInstance().getAllStudents().get(position());
        mStudentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        roundNo = (roundResults == null ? 1 : roundResults.size() + 1);
        // Log.i("james", roundResults.toString());
        for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {
            if (roundResults!=null&&roundResults.size() > 0 && i < roundResults.size()) {
                List<MachineResult> machineResultList = DBManager.getInstance().getItemRoundMachineResult(student.getStudentCode(),
                        1, i + 1);
                if (machineResultList.size() > 0)
                    resultList.add(new BasketBallTestResult(i + 1, machineResultList, roundResults.get(i).getMachineResult(), roundResults.get(i).getResult(), roundResults.get(i).getPenaltyNum(), roundResults.get(i).getResultState()));

            } else {
                resultList.add(new BasketBallTestResult(i + 1, null, 0, -999, 0, -999));
                if (resultAdapter.getSelectPosition() == -1) {
                    resultAdapter.setSelectPosition(i);
                }
            }
        }
        resultAdapter.notifyDataSetChanged();
    }

    /**
     * 是否存在未测试位置
     */
    private boolean isExistTestPlace() {
        if (resultAdapter.getSelectPosition() == -1)
            return false;
        if (resultList.get(resultAdapter.getSelectPosition()).getMachineResultList() != null
                && resultList.get(resultAdapter.getSelectPosition()).getMachineResultList().size() > 0) {

            for (int i = 0; i < resultList.size(); i++) {
                List<MachineResult> machineResultList = resultList.get(i).getMachineResultList();
                if (machineResultList == null || machineResultList.size() == 0) {
                    resultAdapter.setSelectPosition(i);
                    roundNo = i + 1;
                    resultAdapter.notifyDataSetChanged();
                    return true;
                }
            }
            state = WAIT_FREE;
            toastSpeak("该考生已全部测试完成");
            return false;
        } else {
            roundNo = resultAdapter.getSelectPosition() + 1;
            return true;
        }

    }

    /**
     * 成绩确定
     */
    private void onResultConfirmed() {
        if (pairs.get(position()) == null)
            return;
        List<RoundResult> dbResultList = TestCache.getInstance().getResults().get(pairs.get(position()).getStudent());
        for (int i = 0; i < resultList.size(); i++) {
            BasketBallTestResult testResult = resultList.get(i);
            if (testResult.getResult() >= 0) {
                if (dbResultList.size() > 0) {
                    //是否有进行更改，有更改成绩为未上传
                    if (dbResultList.get(i).getResult() != testResult.getResult() || dbResultList.get(i).getPenaltyNum() != testResult.getPenalizeNum()
                            || dbResultList.get(i).getResultState() != testResult.getResultState()) {
                        dbResultList.get(i).setUpdateState(0);
                    }
                    dbResultList.get(i).setResult(testResult.getResult());
                    dbResultList.get(i).setPenaltyNum(testResult.getPenalizeNum());
                    dbResultList.get(i).setResultState(testResult.getResultState());
                } else {
                    //TODO PC可以重复测试 流程认为有误需要讨论
                    RoundResult roundResult = new RoundResult();
                    roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    roundResult.setStudentCode(mStudentItem.getStudentCode());
                    roundResult.setItemCode(TestConfigs.getCurrentItemCode());
                    roundResult.setResult(testResult.getResult());
                    roundResult.setMachineResult(testResult.getResult());
                    roundResult.setResultState(testResult.getResultState());
                    roundResult.setTestTime(System.currentTimeMillis() + "");
                    roundResult.setRoundNo(i);
                    roundResult.setTestNo(1);
                    roundResult.setExamType(mStudentItem.getExamType());
                    roundResult.setScheduleNo(mStudentItem.getScheduleNo());
                    roundResult.setUpdateState(0);
                    DBManager.getInstance().insertRoundResult(roundResult);
                }
            } else {
                if (testResult.getResultState() != -999 && testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    RoundResult roundResult = new RoundResult();
                    roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    roundResult.setStudentCode(mStudentItem.getStudentCode());
                    roundResult.setItemCode(TestConfigs.getCurrentItemCode());
                    roundResult.setResult(0);
                    roundResult.setMachineResult(0);
                    roundResult.setResultState(testResult.getResultState());
                    roundResult.setTestTime(System.currentTimeMillis() + "");
                    roundResult.setRoundNo(i);
                    roundResult.setTestNo(1);
                    roundResult.setExamType(mStudentItem.getExamType());
                    roundResult.setScheduleNo(mStudentItem.getScheduleNo());
                    roundResult.setUpdateState(0);
                    DBManager.getInstance().insertRoundResult(roundResult);
                    resultList.get(i).setResult(0);
                    resultAdapter.notifyDataSetChanged();
                }

            }

        }
        if (dbResultList.size() > 0) {
            DBManager.getInstance().updateRoundResult(dbResultList);
        }
        //Todo 确定成绩播报的时刻
//        StuDevicePair pair = pairs.get(position());
//        int result = pair.getDeviceResult().getResult();
//
//        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
//            TtsManager.getInstance().speak(ResultDisplayUtils.getStrResultForDisplay(result));
//        }
        uploadResults();
        // 是否需要进行下一次测试
//        if (shouldContinue(result)) {
//            prepareForBegin();
//        } else {
//            prepareForFinish();
//        }
    }

    /**
     * 上传成绩
     */
    private void uploadResults() {
        if (SettingHelper.getSystemSetting().isRtUpload()) {
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

    /**
     * 等待
     */
    private void prepareForBegin() {
        Student student = pairs.get(position()).getStudent();
        tvResult.setText(student.getStudentName());
        state = WAIT_CHECK_IN;
        setOperationUI();
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(1, UdpLEDUtil.getLedByte(student.getStudentName(), Paint.Align.CENTER)));

    }

    /**
     * 结束
     */
    private void prepareForFinish() {
        TestCache testCache = TestCache.getInstance();
        Student student = pairs.get(position()).getStudent();
        if (SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
        }

        state = WAIT_FREE;
        setOperationUI();
    }

    /**
     * 根据测试状态显示操作UI
     */
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
                txtWaiting.setEnabled(false);
                txtIllegalReturn.setEnabled(true);
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
}
