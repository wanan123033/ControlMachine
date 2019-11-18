package com.feipulai.exam.activity.basketball;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityUtils;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.udp.UDPBasketBallConfig;
import com.feipulai.device.udp.UdpClient;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.adapter.BasketBallResultAdapter;
import com.feipulai.exam.activity.basketball.bean.BallDeviceState;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.basketball.util.TimerUtil;
import com.feipulai.exam.activity.basketball.pair.BasketBallPairActivity;
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
import cn.pedant.SweetAlert.SweetAlertDialog;

public class BasketballIndividualActivity extends BaseTitleActivity implements IndividualCheckFragment.OnIndividualCheckInListener
        , BasketBallListener.BasketBallResponseListener, TimerUtil.TimerAccepListener {

    private static final int GET_STATE = 22;

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
    CheckBox cbNear;
    @BindView(R.id.cb_far)
    CheckBox cbFar;
    @BindView(R.id.cb_led)
    CheckBox cbLed;


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

    private BallManager ballManager;
    private BasketBallRadioFacade facade;
    private long timerDate;

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

        facade = new BasketBallRadioFacade(setting.getTestType(), this);
        ballManager = new BallManager.Builder((setting.getTestType())).setHostIp(setting.getHostIp()).setInetPost(1527).setPost(setting.getPost())
                .setUdpListerner(new BasketBallListener(this)).build();
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


        if (setting.getTestType() == 0) {
            ballManager.setRadioStopTime(SettingHelper.getSystemSetting().getHostId());
            cbNear.setVisibility(View.GONE);
            cbFar.setVisibility(View.GONE);
            cbLed.setVisibility(View.GONE);
        } else {
            ballManager.setRadioFreeStates(SettingHelper.getSystemSetting().getHostId());
            tvPair.setVisibility(View.VISIBLE);
            cbNear.setVisibility(View.VISIBLE);
            cbFar.setVisibility(View.GONE);
            cbLed.setVisibility(View.VISIBLE);
        }


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

        prepareForCheckIn();
        state = WAIT_FREE;
        setOperationUI();


    }


    @Override
    protected void onResume() {
        super.onResume();

        if (setting.getTestType() == 1) {
            RadioManager.getInstance().setOnRadioArrived(facade);
            facade.resume();
            facade.setInterceptSecond(setting.getInterceptSecond());
        } else {
            ballManager.setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(), new BasketBallListener(this));
//            UdpClient.getInstance().setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(), new BasketBallListener(this));
            //设置精度
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1));
        }
        //设置精度
        ballManager.sendSetPrecision(SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(),
                setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        facade.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        facade.finish();
        facade = null;
        RadioManager.getInstance().setOnRadioArrived(null);
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

            pairs.get(0).setStudent(student);

            for (RoundResult result : results) {
                if (isFullSkip(result.getResult(), result.getResultState())) {
                    toastSpeak("满分");
                    pairs.get(0).setStudent(null);
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
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, student.getLEDStuName(), Paint.Align.CENTER);
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.CENTER);
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(1, UdpLEDUtil.getLedByte(student.getStudentName(), Paint.Align.CENTER)));
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2, UdpLEDUtil.getLedByte("", Paint.Align.CENTER)));

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
        //6检入
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
//                testDate = System.currentTimeMillis() + "";
                break;
            case 4:
                state = WAIT_STOP;
                txtDeviceStatus.setText("停止");
                break;
            case 5:
                state = WAIT_CONFIRM;
                txtDeviceStatus.setText("中断");
                break;
            case 6:
                txtDeviceStatus.setText("空闲");
                state = WAIT_CHECK_IN;
                break;
        }
        setOperationUI();
    }

    @Override
    public void triggerStart(BasketballResult basketballResult) {

        testDate = System.currentTimeMillis() + "";
        timerUtil.startTime(10);
        state = TESTING;
        txtDeviceStatus.setText("计时");
        setOperationUI();

    }

    @Override
    public void getResult(BasketballResult result) {
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
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            int pResult = result.getResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            testResult.setSelectMachineResult(machineResult.getResult());
            testResult.setResult(pResult);
            testResult.getMachineResultList().clear();
            testResult.getMachineResultList().addAll(machineResultList);

//            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(mStudentItem.getStudentCode());
            RoundResult testRoundResult = DBManager.getInstance().queryRoundByRoundNo(student.getStudentCode(),
                    testNo, roundNo);
            testRoundResult.setPenaltyNum(testResult.getPenalizeNum());
            testRoundResult.setResult(pResult);
            testRoundResult.setMachineResult(result.getResult());
            Logger.i("拦截更新成绩" + testRoundResult.toString());
            //更新成绩，
            DBManager.getInstance().updateRoundResult(testRoundResult);
            //获取所有成绩设置为非最好成绩
            List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(mStudentItem);
            //获取最小成绩设置为最好成绩
            RoundResult dbAscResult = DBManager.getInstance().queryOrderAscScore(student.getStudentCode(), testNo);
            for (RoundResult roundResult : results) {
                if (roundResult.getResult() == dbAscResult.getResult()) {
                    roundResult.setIsLastResult(1);
                } else {
                    roundResult.setIsLastResult(0);
                }
                DBManager.getInstance().updateRoundResult(roundResult);
            }

//            dbAscResult.setIsLastResult(1);
//            DBManager.getInstance().updateRoundResult(dbAscResult);

            if (results != null) {
                TestCache.getInstance().getResults().put(student, results);
            }


            showStuInfoResult();

        }

        resultAdapter.notifyDataSetChanged();
        DBManager.getInstance().insterMachineResult(machineResult);
        setOperationUI();
        tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital()));

    }

    @Override
    public void getStatusStop(BasketballResult result) {
        //非测试不做处理
        if (state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_CONFIRM) {
            return;
        }
        timerUtil.stop();
        txtDeviceStatus.setText("停止");
        //还未开始停止返回检入考生状态
        if (state == WAIT_BEGIN) {
            state = WAIT_CHECK_IN;
            setOperationUI();
            tvResult.setText(DateUtil.caculateFormatTime(0, TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital()));
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
//                    UdpLEDUtil.getLedByte("", Paint.Align.RIGHT)));
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.RIGHT);
        } else {
            pairs.get(0).setDeviceResult(result);
            state = WAIT_STOP;
            setOperationUI();
            tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital()));
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
//                    UdpLEDUtil.getLedByte(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital()), Paint.Align.RIGHT)));
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital()), Paint.Align.RIGHT);
        }


    }

    @Override
    public void timer(Long time) {
        if (state == TESTING) {
            tvResult.setText(DateUtil.caculateTime(time * 10, TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital(), 0));
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title).addRightText("项目设置", new View.OnClickListener() {
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
        facade.finish();
        if (setting.getTestType() == 0) {
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
            ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
        } else {
            ballManager.setRadioFreeStates(SettingHelper.getSystemSetting().getHostId());
        }
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

//        (resultList.get(resultAdapter.getSelectPosition()).getMachineResultList() != null
//                && resultList.get(resultAdapter.getSelectPosition()).getMachineResultList().size() > 0)
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

    @OnClick(R.id.tv_pair)
    public void onViewClicked() {
        IntentUtil.gotoActivity(this, BasketBallPairActivity.class);
    }

    @OnClick({R.id.tv_punish_add, R.id.tv_punish_subtract, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.tv_print, R.id.tv_confirm
            , R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_continue_run, R.id.txt_stop_timing, R.id.txt_finish_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting://等待发令
                if ((state == WAIT_CHECK_IN || state == WAIT_CONFIRM || state == WAIT_STOP) && isExistTestPlace()) {
                    timerUtil.stop();

//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 2);
                }

                break;
            case R.id.txt_illegal_return://违例返回
                showIllegalReturnDialog();
                break;
            case R.id.txt_continue_run://继续运行
                if (setting.getTestType() == 0) {
//                   UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(3));
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 3);
                } else {
                    Basketball868Result result = new Basketball868Result();
                    int[] time = TimeUtil.getTestResult(timerDate);
                    if (time != null) {
                        result.setHour(time[0]);
                        result.setMinth(time[1]);
                        result.setSencond(time[2]);
                        result.setMinsencond(time[3]);
                        ballManager.setRadioLedStartTime(SettingHelper.getSystemSetting().getHostId(), result);
                    }
                }
                break;
            case R.id.txt_stop_timing://停止计时
                if (setting.getTestType() == 0) {
                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                }
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
                print();

                break;
            case R.id.tv_confirm://确定
                timerUtil.stop();
                if (state == WAIT_CONFIRM) {
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                    ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 1);
                }
                if (state != TESTING && pairs.get(0).getStudent() != null) {
                    tvResult.setText("");
                    txtDeviceStatus.setText("空闲");
                    onResultConfirmed();
                }
                break;
            case R.id.txt_finish_test:
                if (state == TESTING) {
                    toastSpeak("测试中,不允许跳过本次测试");
                } else {
                    timerUtil.stop();
                    resultAdapter.setSelectPosition(-1);
                    prepareForCheckIn();
                    txtDeviceStatus.setText("空闲");
                    if (setting.getTestType() == 0) {
                        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(1,
                                UdpLEDUtil.getLedByte("", Paint.Align.RIGHT)));
                        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
                                UdpLEDUtil.getLedByte("", Paint.Align.RIGHT)));
                    }else {

                    }
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, "", Paint.Align.RIGHT);
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.RIGHT);
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(1,
//                            UdpLEDUtil.getLedByte("", Paint.Align.RIGHT)));
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
//                            UdpLEDUtil.getLedByte("", Paint.Align.RIGHT)));

                }

                break;
        }
    }

    private void sleep() {

        try {
            //两个指令相间隔100MS
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void print() {
        if (pairs.get(0).getStudent() != null) {
            TestCache testCache = TestCache.getInstance();
            InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
        }
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
        //TESTING ---->WAIT_BEGIN
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
        showLedConfirmedResult();

        //更新修改成绩
        if (updateResult.size() > 0) {
            DBManager.getInstance().updateRoundResult(updateResult);
        }

        List<RoundResult> dbRoundResult = DBManager.getInstance().queryResultsByStuItem(mStudentItem);

        //获取最小成绩设置为最好成绩
        RoundResult dbAscResult = DBManager.getInstance().queryOrderAscScore(mStudentItem.getStudentCode(), testNo);

        if (dbAscResult != null) {
            //获取所有成绩设置为非最好成绩
            for (RoundResult roundResult : dbRoundResult) {
                if (roundResult.getResult() == dbAscResult.getResult()) {
                    roundResult.setIsLastResult(1);

                } else {
                    roundResult.setIsLastResult(0);
                }
                DBManager.getInstance().updateRoundResult(roundResult);
            }
//            dbAscResult.setIsLastResult(1);
//            DBManager.getInstance().updateRoundResult(dbAscResult);
        } else if (dbRoundResult != null && dbRoundResult.size() > 0) {
            dbRoundResult.get(0).setIsLastResult(1);
            DBManager.getInstance().updateRoundResult(dbRoundResult.get(0));
        }
        if (dbRoundResult != null) {
            TestCache.getInstance().getResults().put(pair.getStudent(), dbRoundResult);
        }

        int result = pair.getDeviceResult().getResult();
        uploadResult(pair.getStudent());

        showStuInfoResult();


        // 是否需要进行下一次测试
        if (shouldContinue(result)) {
            prepareForBegin();
        } else {
            prepareForFinish();
        }


    }

    private void showLedConfirmedResult() {
        //1:正常 2:犯规 3:中退 4:弃权
        BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
        switch (testResult.getResultState()) {
            case RoundResult.RESULT_STATE_NORMAL:
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, ResultDisplayUtils.getStrResultForDisplay(testResult.getResult()), testResult.getPenalizeNum() + "", Paint.Align.CENTER);
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2, UdpLEDUtil.getLedByte(ResultDisplayUtils.getStrResultForDisplay(testResult.getResult()), testResult.getPenalizeNum() + "", Paint.Align.CENTER)));
                break;
            case RoundResult.RESULT_STATE_FOUL:
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "犯规", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2, UdpLEDUtil.getLedByte("犯规", testResult.getPenalizeNum() + "", Paint.Align.CENTER)));
                break;
            case RoundResult.RESULT_STATE_BACK:
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "中退", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2, UdpLEDUtil.getLedByte("中退", testResult.getPenalizeNum() + "", Paint.Align.CENTER)));
                break;
            case RoundResult.RESULT_STATE_WAIVE:
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "弃权", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2, UdpLEDUtil.getLedByte("弃权", testResult.getPenalizeNum() + "", Paint.Align.CENTER)));
                break;

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
        Logger.i("保存成绩:" + roundResult.toString());
        //获取所有成绩设置为非最好成绩
        List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(mStudentItem);
        TestCache.getInstance().getResults().put(student, results);
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
            if (roundResultList == null || roundResultList.size() == 0) {
                return;
            }
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

        return hasRemain;
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

    private void showIllegalReturnDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.illegal_return_hint))
                .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                timerUtil.stop();
                BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
                List<MachineResult> machineResultList = testResult.getMachineResultList();
                if (machineResultList != null && machineResultList.size() > 0) {
                    Student student = pairs.get(0).getStudent();
                    int testNo = TestCache.getInstance().getTestNoMap().get(student);
                    DBManager.getInstance().deleteStuResult(student.getStudentCode(), testNo, roundNo, RoundResult.DEAFULT_GROUP_ID);
                    DBManager.getInstance().deleteStuMachineResults(student.getStudentCode(), testNo, roundNo, RoundResult.DEAFULT_GROUP_ID);
                    testResult.setSelectMachineResult(0);
                    testResult.setResult(-999);
                    testResult.setResultState(-999);
                    testResult.setMachineResultList(null);
                    showStuInfoResult();
                    resultAdapter.notifyDataSetChanged();
                }
                state = WAIT_CONFIRM;

                ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                sleep();
                ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 2);
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
//                sleep();
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();

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
}
