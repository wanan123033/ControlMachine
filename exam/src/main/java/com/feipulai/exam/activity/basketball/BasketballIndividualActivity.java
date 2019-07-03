package com.feipulai.exam.activity.basketball;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
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
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.jump_rope.bean.TestCache;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.RoundResultBean;
import com.feipulai.exam.bean.UploadResults;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BasketballIndividualActivity extends BaseTitleActivity implements IndividualCheckFragment.OnIndividualCheckInListener
        , BasketBallListener.BasketBallResponseListener, TimerUtil.TimerAccepListener {

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
    private IndividualCheckFragment individualCheckFragment;
    // 状态 WAIT_FREE---> WAIT_CHECK_IN---> WAIT_BEGIN--->TESTING---->WAIT_STOP---->WAIT_CONFIRM--->WAIT_CHECK_IN
    private static final int WAIT_FREE = 0x0;
    private static final int WAIT_CHECK_IN = 0x1;
    private static final int WAIT_BEGIN = 0x2;
    private static final int TESTING = 0x3;
    private static final int WAIT_STOP = 0x4;
    private static final int WAIT_CONFIRM = 0x5;
    protected volatile int state = WAIT_FREE;
    //    private UdpClient udpClient;
    private BasketBallSetting setting;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private List<BasketBallTestResult> resultList = new ArrayList<>();
    private BasketBallResultAdapter resultAdapter;
    private String testDate;
    private StudentItem mStudentItem;
    private int roundNo;
    private TimerUtil timerUtil;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_individual_basketball;
    }

    @Override
    protected void initData() {
        //获取项目设置
        setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);
        if (setting == null)
            setting = new BasketBallSetting();

        timerUtil = new TimerUtil(this);

        StuDevicePair pair = new StuDevicePair();
        BaseDeviceState deviceState = new BaseDeviceState(BaseDeviceState.STATE_DISCONNECT);
        pair.setBaseDevice(deviceState);
        pair.setDeviceResult(new BasketballResult());
        pairs.add(pair);

        individualCheckFragment = new IndividualCheckFragment();
        individualCheckFragment.setResultView(lvResults);
        individualCheckFragment.setOnIndividualCheckInListener(this);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), individualCheckFragment, R.id.ll_individual_check);
        setOperationUI();
        UdpClient.getInstance().init(1527);

        resultAdapter = new BasketBallResultAdapter(resultList, setting);
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

        prepareForCheckIn();


    }


    @Override
    protected void onResume() {
        super.onResume();
        UdpClient.getInstance().setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(), new BasketBallListener(this));
        //设置精度
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1));
    }


    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.ITEM_SETTING_UPDATE) {
            setting = SharedPrefsUtil.loadFormSource(this, BasketBallSetting.class);

        }
    }

    @Override
    public void onIndividualCheckIn(Student student, StudentItem studentItem, List<RoundResult> results) {
        if (state == WAIT_FREE || state == WAIT_CHECK_IN) {
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
                testNo = 1;
            } else {
                TestCache.getInstance().getResults().put(student, results);
                //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
                RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
                testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo();
            }
            roundNo = results.size() + 1;
            TestCache.getInstance().getTestNoMap().put(student, testNo);

            presetResult(student, testNo);
            resultAdapter.notifyDataSetChanged();

            TestCache.getInstance().setTestingPairs(pairs);
            TestCache.getInstance().getStudentItemMap().put(student, studentItem);
            pairs.get(0).setDeviceResult(new BasketballResult());
            pairs.get(0).setPenalty(0);

            prepareForBegin();
            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(1, UdpLEDUtil.getLedByte(student.getStudentName(), Paint.Align.CENTER)));
        } else {
            toastSpeak("当前考生还未完成测试,拒绝检录");
        }


    }


    @Override
    public void getDeviceStatus(int udpStatus) {
//                  * @param status STATUS_FREE		1		//FREE
//                *               STATUS_WAIT  		2		//WAIT To Start
//                *               STATUS_RUNING 	    3		//Start Run
//                *               STATUS_PREP  		4		//Prepare to stop
//                *               STATUS_PAUSE 		5		//Display stop time,But Timer is Running

        pairs.get(0).getBaseDevice().setState(BaseDeviceState.STATE_FREE);
        switch (udpStatus) {
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
        pairs.get(0).setDeviceResult(result);
        Student student = pairs.get(0).getStudent();
        int testNo = TestCache.getInstance().getTestNoMap().get(student);
        state = WAIT_CONFIRM;
        txtDeviceStatus.setText("中断");
        List<MachineResult> machineResultList = DBManager.getInstance().getItemRoundMachineResult(student.getStudentCode()
                , testNo,
                roundNo);

        MachineResult machineResult = new MachineResult();
        machineResult.setItemCode(TestConfigs.getCurrentItemCode());
        machineResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        machineResult.setTestNo(testNo);
        machineResult.setRoundNo(roundNo);
        machineResult.setStudentCode(student.getStudentCode());
        machineResult.setResult(result.getResult());
        //第一次拦截保存成绩，其他拦截只保存
        if (machineResultList.size() == 0 || machineResultList == null) {
            machineResultList.add(machineResult);
            addRoundResult(result);
            resultList.get(resultAdapter.getSelectPosition()).setMachineResultList(machineResultList);
            resultList.get(resultAdapter.getSelectPosition()).setSelectMachineResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setPenalizeNum(0);
            resultList.get(resultAdapter.getSelectPosition()).setResultState(RoundResult.RESULT_STATE_NORMAL);
        } else {
            machineResultList.add(machineResult);
//            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(mStudentItem.getStudentCode());
            RoundResult testRoundResult = DBManager.getInstance().queryRoundByRoundNo(student.getStudentCode(),
                    testNo, roundNo);
            testRoundResult.setResult(result.getResult());
            testRoundResult.setMachineResult(result.getResult());

            //更新成绩，
            DBManager.getInstance().updateRoundResult(testRoundResult);
            //获取所有成绩设置为非最好成绩
            List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(mStudentItem);
            for (RoundResult roundResult : results) {
                roundResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(roundResult);
            }
            //获取最小成绩设置为最好成绩
            RoundResult dbAscResult = DBManager.getInstance().queryOrderAscScore(student.getStudentCode(), testNo);
            dbAscResult.setIsLastResult(1);
            DBManager.getInstance().updateRoundResult(dbAscResult);
            showStuInfoResult();
            resultList.get(resultAdapter.getSelectPosition()).setSelectMachineResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setResult(result.getResult());
            resultList.get(resultAdapter.getSelectPosition()).getMachineResultList().clear();
            resultList.get(resultAdapter.getSelectPosition()).getMachineResultList().addAll(machineResultList);
        }

        resultAdapter.notifyDataSetChanged();
        DBManager.getInstance().insterMachineResult(machineResult);
        setOperationUI();
        tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital()));

    }

    @Override
    public void getStatusStop(BasketballResult result) {
        //非测试不做处理
        if (state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_CONFIRM) {
            return;
        }
        timerUtil.stop();
        pairs.get(0).setDeviceResult(result);
        state = WAIT_STOP;
        setOperationUI();
        txtDeviceStatus.setText("停止");
        tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital()));
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
                UdpLEDUtil.getLedByte(ResultDisplayUtils.getStrResultForDisplay(result.getResult()), Paint.Align.RIGHT)));


    }

    @Override
    public void timer(Long time) {
        tvResult.setText(DateUtil.caculateTime(time * 10, TestConfigs.sCurrentItem.getDigital(), 0));
    }

    @Nullable
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

    @Override
    public void finish() {
        if (isConfigurableNow()) {
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
        timerUtil.stop();
        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
//        udpClient.close();
    }


    /**
     * 预设置成绩
     */
    private void presetResult(Student student, int testNo) {
        resultList.clear();
        for (int i = 0; i < TestConfigs.getMaxTestCount(this); i++) {
            RoundResult roundResult = DBManager.getInstance().queryRoundByRoundNo(student.getStudentCode(), testNo, i + 1);
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

        }

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
            toastSpeak("该考生已全部测试完成");
            return false;
        } else {
            roundNo = resultAdapter.getSelectPosition() + 1;
            return true;
        }

    }

    /**
     * 跳转项目设置
     */
    private void startProjectSetting() {
        if (!isConfigurableNow()) {
            IntentUtil.gotoActivityForResult(this, BasketBallSettingActivity.class, 1);
        } else {
            toastSpeak("测试中,不允许修改设置");
        }
    }

    /**
     * 是否是使用中
     */
    private boolean isConfigurableNow() {

        return !(state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_BEGIN);
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
                if (pairs.get(0).getStudent() != null) {
                    TestCache testCache = TestCache.getInstance();
                    InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                            TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
                }

                break;
            case R.id.tv_confirm://确定

                if (state == WAIT_CONFIRM) {
                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                }
                if (state == WAIT_CHECK_IN || state == WAIT_CONFIRM || (pairs.get(0).getStudent() != null && state == WAIT_FREE)) {
                    tvResult.setText("");
                    txtDeviceStatus.setText("空闲");
                    onResultConfirmed();
                }
                break;
            case R.id.txt_finish_test:
                if (state == TESTING) {
                    toastSpeak("测试中,不允许跳过本次测试");
                } else {
                    resultAdapter.setSelectPosition(-1);
                    prepareForCheckIn();
                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                    txtDeviceStatus.setText("空闲");
                }

                break;
        }
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
            if ((testResult.getResult() < 0 && (testResult.getResultState() == -999
                    || testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL))) {
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

    /**
     * 成绩确定
     */
    private void onResultConfirmed() {
        StuDevicePair pair = pairs.get(0);
        if (pair.getStudent() == null)
            return;
        int testNo = TestCache.getInstance().getTestNoMap().get(pair.getStudent());
        List<RoundResult> updateResult = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++) {
            BasketBallTestResult testResult = resultList.get(i);
            if (testResult.getResult() >= 0) {
                RoundResult roundResult = DBManager.getInstance().queryRoundByRoundNo(pair.getStudent().getStudentCode(),
                        testNo, i + 1);
                if (roundResult != null) {
                    //是否有进行更改，有更改成绩为未上传
                    if (roundResult.getResult() != testResult.getResult() || roundResult.getPenaltyNum() != testResult.getPenalizeNum()
                            || roundResult.getResultState() != testResult.getResultState()) {
                        roundResult.setUpdateState(0);
                        roundResult.setMachineResult(testResult.getSelectMachineResult());
                        roundResult.setResult(testResult.getResult());
                        roundResult.setPenaltyNum(testResult.getPenalizeNum());
                        roundResult.setResultState(testResult.getResultState());
                        updateResult.add(roundResult);
                    }

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
                    roundResult.setRoundNo(resultList.get(i).getRoundNo());
                    roundResult.setTestNo(testNo);
                    roundResult.setExamType(mStudentItem.getExamType());
                    roundResult.setScheduleNo(mStudentItem.getScheduleNo());
                    roundResult.setUpdateState(0);
                    roundResult.setIsLastResult(0);
                    DBManager.getInstance().insertRoundResult(roundResult);
                    resultList.get(i).setResult(0);
                    resultAdapter.notifyDataSetChanged();
                }

            }

        }
        //更新修改成绩
        if (updateResult.size() > 0) {
            DBManager.getInstance().updateRoundResult(updateResult);
        }

        List<RoundResult> dbRoundResult = DBManager.getInstance().queryResultsByStuItem(mStudentItem);
        if (dbRoundResult != null) {
            TestCache.getInstance().getResults().put(pair.getStudent(), dbRoundResult);
        }
        //获取所有成绩设置为非最好成绩
        for (RoundResult roundResult : dbRoundResult) {
            roundResult.setIsLastResult(0);
            DBManager.getInstance().updateRoundResult(roundResult);
        }
        //获取最小成绩设置为最好成绩
        RoundResult dbAscResult = DBManager.getInstance().queryOrderAscScore(mStudentItem.getStudentCode(), testNo);
        if (dbAscResult != null) {
            dbAscResult.setIsLastResult(1);
            DBManager.getInstance().updateRoundResult(dbAscResult);
        } else if (dbRoundResult != null && dbRoundResult.size() > 0) {
            dbRoundResult.get(0).setIsLastResult(1);
            DBManager.getInstance().updateRoundResult(dbRoundResult.get(0));
        }

        //Todo 确定成绩播报的时刻

        int result = pair.getDeviceResult().getResult();
//        if (SettingHelper.getSystemSetting().isAutoBroadcast()) {
//            TtsManager.getInstance().speak(ResultDisplayUtils.getStrResultForDisplay(result));
//        }
        uploadResult(pair.getStudent());

        showStuInfoResult();
        // 是否需要进行下一次测试
        if (shouldContinue(result)) {
            prepareForBegin();
        } else {
            prepareForFinish();
        }


    }

    /**
     * 拦截添加新成绩
     *
     * @param basketballResult
     */
    private void addRoundResult(BasketballResult basketballResult) {
        Student student = pairs.get(0).getStudent();
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(mStudentItem.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(basketballResult.getResult());
        roundResult.setMachineResult(basketballResult.getResult());
        roundResult.setRoundNo(roundNo);
        roundResult.setTestNo(TestCache.getInstance().getTestNoMap().get(student));
        roundResult.setExamType(mStudentItem.getExamType());
        roundResult.setScheduleNo(mStudentItem.getScheduleNo());
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(testDate);
        roundResult.setUpdateState(0);

        // 重新判断最好成绩
        RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), TestCache.getInstance().getTestNoMap().get(student));
        // Log.i("james", "\nroundResult:" + roundResult.toString());
        if (bestResult != null && bestResult.getResult() < roundResult.getResult()) {
            roundResult.setIsLastResult(0);
            // Log.i("james", "bestResult" +  bestResult.toString());
        } else {
            roundResult.setIsLastResult(1);
            if (bestResult != null) {
                bestResult.setIsLastResult(0);
                DBManager.getInstance().updateRoundResult(bestResult);
                Logger.i("更新成绩:" + bestResult.toString());
            }
        }

        DBManager.getInstance().insertRoundResult(roundResult);

        showStuInfoResult();
    }

    private void showStuInfoResult() {
        Student student = pairs.get(0).getStudent();
        List<RoundResult> scoreResultList = new ArrayList<>();
        // 重新判断最好成绩
        RoundResult bestResult = DBManager.getInstance().queryBestScore(student.getStudentCode(), TestCache.getInstance().getTestNoMap().get(student));
        if (bestResult != null)
            scoreResultList.add(bestResult);
        InteractUtils.showStuInfo(llStuDetail, student, scoreResultList);
    }

    /**
     * 等待
     */
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

    /**
     * 结束
     */
    private void prepareForFinish() {
        TestCache testCache = TestCache.getInstance();

        if (SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
        }

        state = WAIT_FREE;
        setOperationUI();
    }

    /**
     * 上传成绩
     *
     * @param student
     */
    private void uploadResult(Student student) {
        if (SettingHelper.getSystemSetting().isRtUpload() && !TextUtils.isEmpty(TestConfigs.sCurrentItem.getItemCode())) {
            String testNo = TestCache.getInstance().getTestNoMap().get(student) + "";
            StudentItem studentItem = TestCache.getInstance().getStudentItemMap().get(student);
            List<RoundResult> roundResultList = TestCache.getInstance().getResults().get(student);
            String scheduleNo = studentItem.getScheduleNo();

            List<UploadResults> uploadResults = new ArrayList<>();
            uploadResults.add(new UploadResults(scheduleNo,
                    TestConfigs.getCurrentItemCode(), student.getStudentCode()
                    , testNo, "", RoundResultBean.beanCope(roundResultList)));
            Logger.i("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    /**
     * 是否需要进行下一次测试
     *
     * @param result
     * @return
     */
    private boolean shouldContinue(int result) {
        TestCache testCache = TestCache.getInstance();
        Student student = testCache.getAllStudents().get(0);
        boolean hasRemain = isExistTestPlace();// 测试次数未完成
        boolean fullSkip = setting.isFullSkip();
        if (fullSkip) {
            if (student.getSex() == Student.MALE) {
                fullSkip = result <= setting.getMaleFullScore();
            } else {
                fullSkip = result <= setting.getFemaleFullScore();
            }
        }
        return hasRemain && !fullSkip;
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
