package com.feipulai.exam.activity.footBall;

import android.graphics.Paint;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
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
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.AgainTestDialog;
import com.feipulai.exam.activity.base.BaseAFRFragment;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.base.ResitDialog;
import com.feipulai.exam.activity.basketball.BasketBallListener;
import com.feipulai.exam.activity.basketball.BasketBallRadioFacade;
import com.feipulai.exam.activity.basketball.BasketballIndividualActivity;
import com.feipulai.exam.activity.basketball.TimeUtil;
import com.feipulai.exam.activity.basketball.bean.BallDeviceState;
import com.feipulai.exam.activity.basketball.pair.BasketBallPairActivity;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.basketball.util.TimerUtil;
import com.feipulai.exam.activity.footBall.adapter.FootBallResultAdapter;
import com.feipulai.exam.activity.footBall.pair.FootBallPairActivity;
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
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.netUtils.netapi.ServerMessage;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.EditResultDialog;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class FootballIndividualActivity extends BaseTitleActivity implements IndividualCheckFragment.OnIndividualCheckInListener,
        TimerUtil.TimerAccepListener, BasketBallListener.BasketBallResponseListener, BaseAFRFragment.onAFRCompareListener {
    private static final String TAG = "FootballActivity";
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
    @BindView(R.id.tv_resurvey)
    TextView tvResurvey;
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
    private FootBallSetting setting;
    private List<StuDevicePair> pairs = new ArrayList<>(1);
    private List<BasketBallTestResult> resultList = new ArrayList<>();
    private FootBallResultAdapter resultAdapter;
    private String testDate;
    private StudentItem mStudentItem;
    private int roundNo;
    private TimerUtil timerUtil;
    private int useMode;


    private BallManager ballManager;
    private BasketBallRadioFacade facade;
    private long timerDate;
    private String startTime;
    private FrameLayout afrFrameLayout;
    private BaseAFRFragment afrFragment;
    private EditResultDialog editResultDialog;
    private LEDManager ledManager = new LEDManager(LEDManager.LED_VERSION_4_8);

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_individual_basketball;
    }

    @Override
    protected void initData() {
        //获取项目设置
        setting = SharedPrefsUtil.loadFormSource(this, FootBallSetting.class);
        if (setting == null)
            setting = new FootBallSetting();
        LogUtils.all("项目设置" + setting.toString());
        facade = new BasketBallRadioFacade(setting.getTestType(), setting.getAutoPenaltyTime(), setting.getUseLedType(), this);
        facade.setDeviceVersion(setting.getDeviceVersion());
        ballManager = new BallManager.Builder((setting.getTestType())).setHostIp(setting.getHostIp()).setInetPost(1527).setPost(setting.getPost())
                .setUdpListerner(new BasketBallListener(this, setting.getAutoPenaltyTime())).build();

        useMode = setting.getUseMode();
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

        resultAdapter = new FootBallResultAdapter(resultList, setting);
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

        if (setting.getTestType() == 0) {
            ballManager.setRadioStopTime(SettingHelper.getSystemSetting().getHostId());
            cbNear.setVisibility(View.GONE);
            cbFar.setVisibility(View.GONE);
            cbLed.setVisibility(View.GONE);
        } else {
            ballManager.setRadioFreeStates(SettingHelper.getSystemSetting().getHostId());
            tvPair.setVisibility(View.VISIBLE);
            cbNear.setVisibility(View.VISIBLE);
            cbFar.setVisibility(View.VISIBLE);
            cbLed.setVisibility(View.VISIBLE);
        }
        testDate = DateUtil.getCurrentTime() + "";

        if (SettingHelper.getSystemSetting().getCheckTool() == 4 && setAFRFrameLayoutResID() != 0) {
            afrFrameLayout = findViewById(setAFRFrameLayoutResID());
            afrFragment = new BaseAFRFragment();
            afrFragment.setCompareListener(this);
            initAFR();
        }
        if (SettingHelper.getSystemSetting().isAgainTest()) {
            tvResurvey.setVisibility(View.VISIBLE);
        }
        editResultDialog = new EditResultDialog(this);
        editResultDialog.setListener(new EditResultDialog.OnInputResultListener() {

            @Override
            public void inputResult(String result, int state) {
                BasketballResult deviceResult = (BasketballResult) pairs.get(0).getDeviceResult();
                if (deviceResult == null) {
                    pairs.get(0).setDeviceResult(new BasketballResult());
                    deviceResult = (BasketballResult) pairs.get(0).getDeviceResult();
                }
                deviceResult.setSecond(Integer.valueOf(result));
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(0).getDeviceResult().getResult());
                tvResult.setText(displayResult);
                addRoundResult(deviceResult);
                resultList.get(resultAdapter.getSelectPosition()).setSelectMachineResult(deviceResult.getResult());
                resultList.get(resultAdapter.getSelectPosition()).setResult(deviceResult.getResult());
                resultList.get(resultAdapter.getSelectPosition()).setPenalizeNum(0);
                resultList.get(resultAdapter.getSelectPosition()).setResultState(RoundResult.RESULT_STATE_NORMAL);
                resultAdapter.notifyDataSetChanged();
                editResultDialog.dismissDialog();
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (individualCheckFragment.dispatchKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (facade.getUseLedType() != setting.getUseLedType()) {
            facade.finish();
            facade = new BasketBallRadioFacade(setting.getTestType(), setting.getAutoPenaltyTime(), setting.getUseLedType(), this);
            facade.setDeviceVersion(setting.getDeviceVersion());
        }
        ballManager.setUseLedType(setting.getUseLedType());
        if (setting.getTestType() == 1) {
            RadioManager.getInstance().setOnRadioArrived(facade);
            facade.resume();
            facade.setInterceptSecond(setting.getInterceptSecond());
        } else {
            ballManager.setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(),
                    new BasketBallListener(this, setting.getAutoPenaltyTime()));
        }
        //设置精度
        ballManager.sendSetPrecision(SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(),
                setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() - 1);
        ballManager.sendSetDelicacy(SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(),
                setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1);
        ballManager.sendSetBlockertime(SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(),
                setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1);
        if (setting.getUseLedType() == 1) {
            String title = TestConfigs.machineNameMap.get(machineCode)
                    + " " + SettingHelper.getSystemSetting().getHostId();
            ledManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, title, 0, true, false, LEDManager.MIDDLE);
            ledManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, "请检录", 1, false, true, LEDManager.MIDDLE);
            ledManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, "菲普莱体育", 3, 3, false, true);
        }
        if (setting.getUseLedType() == 1) {
            cbLed.setVisibility(View.GONE);
        } else {
            cbLed.setVisibility(View.VISIBLE);
        }

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
            setting = SharedPrefsUtil.loadFormSource(this, FootBallSetting.class);

        } else if (baseEvent.getTagInt() == EventConfigs.BALL_STATE_UPDATE) {
            BallDeviceState deviceState = (BallDeviceState) baseEvent.getData();

            if (deviceState.getDeviceId() == 1) {
                cbNear.setChecked(deviceState.getState() != BaseDeviceState.STATE_DISCONNECT);
            }
            if (deviceState.getDeviceId() == 2) {
                cbFar.setChecked(deviceState.getState() != BaseDeviceState.STATE_DISCONNECT);
            }
            if (deviceState.getDeviceId() == 0) {
                cbLed.setChecked(deviceState.getState() != BaseDeviceState.STATE_DISCONNECT);
            }
        } else if (baseEvent.getTagInt() == EventConfigs.TEMPORARY_ADD_STU) {
            Student student = (Student) baseEvent.getData();
            StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
            onIndividualCheckIn(student, studentItem, new ArrayList<RoundResult>());
        } else if (baseEvent.getTagInt() == EventConfigs.WIFI_STATE) {
            if (setting.getTestType() == 0 && baseEvent.getData() == NetworkInfo.State.CONNECTED) {//有线模式UDP
                //配置网络
                if (SettingHelper.getSystemSetting().isAddRoute() && !TextUtils.isEmpty(NetWorkUtils.getLocalIp())) {
                    String locatIp = NetWorkUtils.getLocalIp();
                    String routeIp = locatIp.substring(0, locatIp.lastIndexOf("."));
                    UdpLEDUtil.shellExec("ip route add " + routeIp + ".0/24 dev eth0 proto static scope link table wlan0 \n");
                }
            }
        } else if (baseEvent.getTagInt() == EventConfigs.BALL_STATE) {
            Basketball868Result result = (Basketball868Result) baseEvent.getData();
            if (result.getDeviceId() == 1) {
                getDeviceStateString(cbNear, "近红外", result.getState());
            }
            if (result.getDeviceId() == 2) {
                getDeviceStateString(cbFar, "远红外", result.getState());
            }
            if (result.getDeviceId() == 0) {
                getDeviceStateString(cbLed, "显示屏", result.getState());
            }
        }
    }

    private void getDeviceStateString(CheckBox cb, String name, int state) {
        switch (state) {
            /**
             * 离线：0x00
             * 空闲：0x01
             * 等待：0x02
             * 计时：0x03
             * 暂停：0x05（暂停显示时间，不停表只针对显示屏）
             * 结束：0x06
             */

            case 1:
                cb.setText(name + "空闲");
                cb.setTextColor(ContextCompat.getColor(this, R.color.result_points));
                break;
            case 2:
                cb.setText(name + "等待");
                cb.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
                break;
            case 3:
                cb.setText(name + "计时");
                cb.setTextColor(ContextCompat.getColor(this, R.color.OrangeRed));
                break;
            case 5:
                cb.setText(name + "暂停");
                cb.setTextColor(ContextCompat.getColor(this, R.color.Maroon));
                break;
            case 6:
                cb.setText(name + "结束");
                cb.setTextColor(ContextCompat.getColor(this, R.color.SaddleBrown));
                break;
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
                    LogUtils.operation("足球该学生已满分跳过测试:" + result.getStudentCode());
                    return;
                }
            }

            resultAdapter.setSelectPosition(-1);
            mStudentItem = studentItem;
            state = WAIT_CHECK_IN;
            setOperationUI();
            pairs.get(0).setStudent(student);
            TestCache.getInstance().clear();
            TestCache.getInstance().init();
            TestCache.getInstance().getAllStudents().add(student);
            TestCache.getInstance().getResults().put(student, results);

            int testNo;
            if (results == null || results.size() == 0) {
                TestCache.getInstance().getResults().put(student,
                        results != null ? results
                                : new ArrayList<RoundResult>(TestConfigs.getMaxTestCount(student.getStudentCode())));
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
                    LogUtils.operation("足球该学生有成绩:" + student.getStudentCode() + ",测试次数 = " + testNo);
            }
            roundNo = results.size() + 1;
            LogUtils.operation("足球当前轮次 ： " + roundNo);
            TestCache.getInstance().getTestNoMap().put(student, testNo);

            presetResult(student, testNo);
            resultAdapter.notifyDataSetChanged();

            TestCache.getInstance().setTestingPairs(pairs);
            TestCache.getInstance().getStudentItemMap().put(student, studentItem);
            pairs.get(0).setDeviceResult(new BasketballResult());
            pairs.get(0).setPenalty(0);

            prepareForBegin();
            if (setting.getUseLedType() == 0) {
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, student.getLEDStuName(), Paint.Align.LEFT);
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.CENTER);
            } else {
                showBeginLed();
            }
        } else {
            toastSpeak("当前考生还未完成测试,拒绝检录");
        }


    }


    @Override
    public void getDeviceStatus(int udpStatus) {
//                  * @param status STATUS_FREE		1		//FREE
//                *               STATUS_WAIT  		2		//WAIT To Start
//                *               STATUS_RUNING 	    3		//Start Run
//                *               STATUS_PREP  		4		//Prepare to release
//                *               STATUS_PAUSE 		5		//Display release time,But Timer is Running
        LogUtils.all("足球设备状态值:" + udpStatus);
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
                if (isExistTestPlace()) {
                    state = WAIT_CHECK_IN;
                } else {
                    state = WAIT_FREE;
                }
                break;
        }
        setOperationUI();
    }

    @Override
    public void triggerStart(BasketballResult result) {
        if (result != null)
            LogUtils.all("足球开始计时:useMode=" + useMode + ",result.gettNum()=" + result.gettNum());
        Log.i(TAG, "triggerStart:" + result.toString());
        switch (useMode) {
            case 0://单拦截
                doTriggerStart();
                break;
            case 1://"2:起点1:终点"
            case 4://2:起终点1:折返点
                if (result.gettNum() == 2) { //起点触发
                    doTriggerStart();
                } else {
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 2);
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
                }
                break;
            case 2://2:终点1:起点
            case 3://2:折返点1:起终点
                if (result.gettNum() == 1) { //起点触发
                    doTriggerStart();
                } else {
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 2);
                }
                break;

        }

    }

    /**
     * 开始
     */
    private void doTriggerStart() {
        testDate = System.currentTimeMillis() + "";
        switch (TestConfigs.sCurrentItem.getDigital()) {
            case 1:
                timerUtil.startTime(100);
                break;
            case 2:
                timerUtil.startTime(10);
                break;
            case 3:
                timerUtil.startTime(1);
                break;
            default:
                timerUtil.startTime(100);
                break;
        }
        state = TESTING;
        txtDeviceStatus.setText("计时");
        setOperationUI();
        if (setting.getUseLedType() == 1) {
            ledManager.ballTimeControl(SettingHelper.getSystemSetting().getHostId(), true, true, true, 0, setting.getAccuracy(), false, 2);
        }
    }

    @Override
    public void getResult(BasketballResult result) {
        LogUtils.operation("足球获取到成绩:" + result.toString());

        //非测试不做处理
        if (state == WAIT_FREE || state == WAIT_CHECK_IN || TextUtils.isEmpty(testDate)) {
            return;
        }

        switch (useMode) {

            case 1://"2:起点1:终点"
                if (result.gettNum() == 2) {//拦截到了起点，重新计时
                    tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital()));
                } else {//拦截到终点，正常
                    doGetResult(result);
                }

                break;
            case 2://2:终点1:起点

                if (result.gettNum() == 1) {//拦截到了起点，重新计时
                    tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital()));

                    int[] time = TimeUtil.getTestResult(result.getResult());
                    if (time != null) {
                        Basketball868Result basketball868Result = new Basketball868Result();
                        basketball868Result.setHour(time[0]);
                        basketball868Result.setMinth(time[1]);
                        basketball868Result.setSencond(time[2]);
                        basketball868Result.setMinsencond(time[3]);
                        ballManager.setRadioLedStartTime(SettingHelper.getSystemSetting().getHostId(), basketball868Result);
                    }
                } else {//拦截到终点，正常
                    doGetResult(result);
                }
                break;
            case 0://单拦截
            case 3://2:折返点1:起终点
            case 4://2:起终点1:折返点
                doGetResult(result);
                break;

        }
//        tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital()));
//        doGetResult(result);
//        String time = DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital());
        String time = ResultDisplayUtils.getStrResultForDisplay(result.getResult());
        if (time.charAt(0) == '0' && time.charAt(1) == '0') {
            time = time.substring(3, time.toCharArray().length);
        } else if (time.charAt(0) == '0') {
            time = time.substring(1, time.toCharArray().length);
        }
        tvResult.setText(time);
        if (setting.getUseLedType() == 1) {
            ballManager.sendSetStopStatusTo(SettingHelper.getSystemSetting().getHostId(),
                    DateUtil.caculateTimeLong(result.getResult(), TestConfigs.sCurrentItem.getDigital(), TestConfigs.sCurrentItem.getCarryMode()), setting.getAccuracy());
        } else {
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, Paint.Align.RIGHT);
        }
    }

    private void doGetResult(BasketballResult result) {
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
        List<RoundResult> results1 = DBManager.getInstance().queryResultsByStudentCode(TestConfigs.getCurrentItemCode(), student.getStudentCode(), roundNo);

        if (machineResultList.size() == 0 || machineResultList == null || results1 == null || results1.size() == 0) {
            machineResultList.add(machineResult);
            addRoundResult(result);
            if (resultList.isEmpty()) {
                resultList.add(new BasketBallTestResult(roundNo, machineResultList, 0, -999, 0, -999));
            }
            resultList.get(resultAdapter.getSelectPosition()).setMachineResultList(machineResultList);
            resultList.get(resultAdapter.getSelectPosition()).setSelectMachineResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setPenalizeNum(0);
            resultList.get(resultAdapter.getSelectPosition()).setResultState(RoundResult.RESULT_STATE_NORMAL);
        } else {
            machineResultList.add(machineResult);
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            int pResult = result.getResult() + (testResult.getPenalizeNum() * ((int) (setting.getPenaltySecond() * 1000.0)));
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
    }

    @Override
    public void getStatusStop(BasketballResult result) {
        if (result != null)
            LogUtils.all("足球停止计时:state=" + state + "---" + result.toString());
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
            tvResult.setText(DateUtil.caculateFormatTime(0, TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital()));
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
//                    UdpLEDUtil.getLedByte(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital()), Paint.Align.RIGHT)));
            String time = DateUtil.caculateFormatTime(0, TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital());
            if (time.charAt(0) == '0' && time.charAt(1) == '0') {
                time = time.substring(3, time.toCharArray().length);
            } else if (time.charAt(0) == '0') {
                time = time.substring(1, time.toCharArray().length);
            }
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, Paint.Align.RIGHT);

        }

        if (setting.getUseLedType() == 1) {
            ballManager.hiddenTime(SettingHelper.getSystemSetting().getHostId(), setting.getAccuracy());
        }
    }

    @Override
    public void timer(Long time) {
        switch (TestConfigs.sCurrentItem.getDigital()) {
            case 1:
                timerDate = time * 100;
                break;
            case 2:
                timerDate = time * 10;
                break;
            case 3:
                timerDate = time;
                break;
            default:
                timerDate = time * 10;
                break;
        }
        if (state == TESTING) {
            tvResult.setText(DateUtil.caculateTime(timerDate, TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital(), 0));
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
        for (int i = 0; i < TestConfigs.getMaxTestCount(student.getStudentCode()); i++) {
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
            IntentUtil.gotoActivityForResult(this, FootBallSettingActivity.class, 1);
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
        IntentUtil.gotoActivity(this, FootBallPairActivity.class);
    }

    @OnClick({R.id.tv_punish_add, R.id.tv_punish_subtract, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.tv_print, R.id.tv_confirm
            , R.id.tv_result, R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_continue_run, R.id.txt_stop_timing,
            R.id.txt_finish_test, R.id.img_AFR, R.id.tv_resurvey})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting://等待发令
                LogUtils.operation("足球点击了等待发令按钮");
                if ((state == WAIT_CHECK_IN || state == WAIT_CONFIRM || state == WAIT_STOP) && isExistTestPlace()) {
                    if ((setting.getTestType() == 1 && facade.isDeviceNormal()) || setting.getTestType() == 0) {
                        if (setting.getUseLedType() == 0) {
                            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, pairs.get(0).getStudent().getLEDStuName(), Paint.Align.LEFT);
                        } else {
                            ballManager.hiddenTime(SettingHelper.getSystemSetting().getHostId(), setting.getAccuracy());
                            showBeginLed();
                        }
                        timerUtil.stop();
                        if (setting.getTestType() == 0) {
                            //有线需要发停止命令重置时间
                            ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                        }
                        sleep();
                        ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 2);
                        startTime = System.currentTimeMillis() + "";
                        if (setting.getTestType() == 1) {
                            facade.awaitState();
                        }
                    } else {
                        toastSpeak("存在未连接设备，请配对");
                    }
                }
                break;
            case R.id.txt_illegal_return://违例返回
                LogUtils.operation("足球点击了违例返回按钮");
                showIllegalReturnDialog();
                break;
            case R.id.txt_continue_run://继续运行
                LogUtils.operation("足球点击了继续运行按钮");
//                UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(3));
                if (setting.getTestType() == 0) {
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 3);
                } else {
                    Basketball868Result result = new Basketball868Result();
                    int[] time = TimeUtil.getTestTime(timerDate);
                    if (time != null) {
                        result.setHour(time[0]);
                        result.setMinth(time[1]);
                        result.setSencond(time[2]);
                        result.setMinsencond(time[3]);
                        if (setting.getUseLedType() == 0) {
                            ballManager.setRadioLedStartTime(SettingHelper.getSystemSetting().getHostId(), result);
                        } else {
                            ballManager.setRadioLedStartTimeTo(SettingHelper.getSystemSetting().getHostId(), timerDate, setting.getAccuracy());
                        }
                        state = TESTING;
                        setOperationUI();
                    }
                }
                break;
            case R.id.txt_stop_timing://停止计时
                LogUtils.operation("足球点击了停止计时按钮");
                ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                getStatusStop(null);
                break;
            case R.id.tv_punish_add: //违例+
                LogUtils.operation("足球点击了违例+按钮");
                setPunish(1);
                break;
            case R.id.tv_punish_subtract://违例-
                LogUtils.operation("足球点击了违例-按钮");
                setPunish(-1);
                break;
            case R.id.tv_foul://犯规
                LogUtils.operation("足球点击了犯规按钮");
                setResultState(RoundResult.RESULT_STATE_FOUL);
                break;
            case R.id.tv_inBack://中退
                LogUtils.operation("足球点击了中退按钮");
                setResultState(RoundResult.RESULT_STATE_BACK);
                break;
            case R.id.tv_abandon://放弃 ;
                LogUtils.operation("足球点击了放弃按钮");
                setResultState(RoundResult.RESULT_STATE_WAIVE);
                break;
            case R.id.tv_normal://正常
                LogUtils.operation("足球点击了正常按钮");
                setResultState(RoundResult.RESULT_STATE_NORMAL);
                break;
            case R.id.tv_print://打印
                LogUtils.operation("足球点击了打印按钮");
                print();

                break;
            case R.id.tv_confirm://确定
                LogUtils.operation("足球点击了确定按钮");
                if (SettingHelper.getSystemSetting().isInputTest()) {
                    onResultConfirmed();
                    return;
                }
                timerUtil.stop();
                if (state == WAIT_CONFIRM) {
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                    ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 1);
                }
                if (state != TESTING && pairs.get(0).getStudent() != null) {
                    tvResult.setText("");
                    txtDeviceStatus.setText("空闲");
                    if (TextUtils.isEmpty(testDate)) {
                        testDate = DateUtil.getCurrentTime() + "";
                    }
                    onResultConfirmed();
                }
                break;
            case R.id.txt_finish_test:
                LogUtils.operation("足球点击了跳过按钮");
                if (state == TESTING) {
                    toastSpeak("测试中,不允许跳过本次测试");
                } else {
                    timerUtil.stop();
                    resultAdapter.setSelectPosition(-1);
                    prepareForCheckIn();
                    txtDeviceStatus.setText("空闲");
                    if (setting.getTestType() == 0) {
//                        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                        ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                    } else {
                        ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 1);
                    }
                    if (setting.getUseLedType() == 0) {
                        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, "", Paint.Align.RIGHT);
                        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.RIGHT);
                    } else {
                        String title = TestConfigs.machineNameMap.get(machineCode)
                                + " " + SettingHelper.getSystemSetting().getHostId();
                        ballManager.hiddenTime(SettingHelper.getSystemSetting().getHostId(), setting.getAccuracy());
                        ledManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, title, 0, true, true, LEDManager.MIDDLE);
                        ledManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, "请检录", 1, false, true, LEDManager.MIDDLE);
                        ledManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, "菲普莱体育", 3, 3, false, true);
                    }
                }

                break;
            case R.id.img_AFR:
                showAFR();
                break;
            case R.id.tv_result:
                if (pairs.size() > 0 && pairs.get(0).getStudent() != null) {
                    if (SettingHelper.getSystemSetting().isInputTest()) {
                        editResultDialog.showDialog(pairs.get(0).getStudent());
                    } else {
                        if (setting.getTestType() == 0) {
                            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("手动获取成绩")
                                    .setContentText("是否进行拦截成绩获取").setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                    ballManager.getUDPResultTime();
                                }
                            }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismissWithAnimation();
                                }
                            }).show();
                        }

                    }
                }

                break;
            case R.id.tv_resurvey:
                showResurvey();

                break;
        }
    }

    private void showResurvey() {
        if (pairs.get(0).getStudent() == null) {
            return;
        }
        final int testNo = TestCache.getInstance().getTestNoMap().get(pairs.get(0).getStudent());
        AgainTestDialog dialog = new AgainTestDialog();
        RoundResult roundResult = DBManager.getInstance().queryRoundByRoundNo(pairs.get(0).getStudent().getStudentCode(), testNo, (resultAdapter.getSelectPosition() + 1));
        if (roundResult == null) {
            toastSpeak("当前轮次无成绩，请进行测试");
            return;
        }
        List<RoundResult> results = new ArrayList<>();
        results.add(roundResult);
        dialog.setArguments(pairs.get(0).getStudent(), results, mStudentItem);
        dialog.setOnIndividualCheckInListener(new ResitDialog.onClickQuitListener() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onCommitPattern(Student student, StudentItem studentItem, List<RoundResult> results, int updateRoundNo) {
                LogUtils.operation(pairs.get(0).getStudent().getStudentCode() + "重测第" + (resultAdapter.getSelectPosition() + 1) + "轮成绩");
                resultList.remove(resultAdapter.getSelectPosition());
                resultList.add(resultAdapter.getSelectPosition(), new BasketBallTestResult(updateRoundNo, null, 0, -999, 0, -999));
                //清除机器成绩
                DBManager.getInstance().deleteStuMachineResults(student.getStudentCode(), testNo, updateRoundNo, RoundResult.DEAFULT_GROUP_ID);

                //设置测试轮次
                pairs.get(0).setCurrentRoundNo(updateRoundNo);
                roundNo = updateRoundNo;
                resultAdapter.notifyDataSetChanged();
                prepareForBegin();
                if (setting.getUseLedType() == 0) {
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, student.getLEDStuName(), Paint.Align.LEFT);
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.CENTER);
                } else {
                    showBeginLed();
                }
            }

            @Override
            public void onCommitGroup(Student student, GroupItem groupItem, List<RoundResult> results, int roundNo) {

            }
        });
        dialog.show(getSupportFragmentManager(), "AgainTestDialog");

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
            Logger.i("原始成绩:" + penalizeNum + "判罚:" + punishType);
            if (punishType >= 0) {//+
                testResult.setPenalizeNum(penalizeNum + 1);
            } else {//-
                if (penalizeNum > 0) {
                    testResult.setPenalizeNum(penalizeNum - 1);
                }
            }
            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * ((int) (setting.getPenaltySecond() * 1000.0)));
            testResult.setResult(result);

            resultAdapter.notifyDataSetChanged();
            LogUtils.operation("判罚成功，判罚值为：=" + penalizeNum);
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
        LogUtils.operation("修改成绩状态:=" + resultState);
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
                        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
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
                    roundResult.setTestTime(testDate);
                    roundResult.setEndTime(System.currentTimeMillis() + "");
                    if (pair.getCurrentRoundNo() != 0) {
                        roundResult.setRoundNo(pair.getCurrentRoundNo());
                        roundResult.setResultTestState(RoundResult.RESULT_RESURVEY_STATE);
                        pair.setCurrentRoundNo(0);
                    } else {
                        roundResult.setRoundNo(resultList.get(i).getRoundNo());
                    }
                    roundResult.setTestNo(testNo);
                    roundResult.setExamType(mStudentItem.getExamType());
                    roundResult.setScheduleNo(mStudentItem.getScheduleNo());
                    roundResult.setUpdateState(0);
                    roundResult.setIsLastResult(0);
                    roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
                    DBManager.getInstance().insertRoundResult(roundResult);
                    resultList.get(i).setResult(0);
                    resultAdapter.notifyDataSetChanged();
                    LogUtils.operation("足球保存成绩:" + roundResult.toString());
                }

            }

        }
        showLedConfirmedResult();

        //更新修改成绩
        if (updateResult.size() > 0) {
            LogUtils.operation("足球更新成绩到数据库:" + updateResult.toString());
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
                    LogUtils.operation("足球设置最好成绩到数据库:" + updateResult.toString());
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
        if (setting.getUseLedType() == 0) {
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, pairs.get(0).getStudent().getLEDStuName(), testResult.getPenalizeNum() + "", Paint.Align.LEFT);

        } else {
            try {
                byte[] buffer = new byte[16];
                byte[] nameByte = pairs.get(0).getStudent().getLEDStuName().getBytes("GB2312");
                System.arraycopy(nameByte, 0, buffer, 0, nameByte.length);
                nameByte = ("第" + (resultAdapter.getSelectPosition() + 1) + "次").getBytes("GB2312");
                System.arraycopy(nameByte, 0, buffer, 10, nameByte.length);
                ledManager.showString(SettingHelper.getSystemSetting().getHostId(), buffer, 0, 0, true, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        switch (testResult.getResultState()) {
            case RoundResult.RESULT_STATE_NORMAL:
                String time = ResultDisplayUtils.getStrResultForDisplay(testResult.getResult());
                if (time.charAt(0) == '0' && time.charAt(1) == '0') {
                    time = time.substring(3, time.toCharArray().length);
                } else if (time.charAt(0) == '0') {
                    time = time.substring(1, time.toCharArray().length);
                }
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, testResult.getPenalizeNum() + "", Paint.Align.CENTER);
                if (setting.getUseLedType() == 0) {
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, Paint.Align.RIGHT);
                } else {

                    ballManager.sendSetStopStatusTo(SettingHelper.getSystemSetting().getHostId(),
                            DateUtil.caculateTimeLong(testResult.getResult(), TestConfigs.sCurrentItem.getDigital(), TestConfigs.sCurrentItem.getCarryMode())
                            , setting.getAccuracy());

                }
                break;
            case RoundResult.RESULT_STATE_FOUL:
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "犯规", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
                if (setting.getUseLedType() == 0) {
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "犯规", Paint.Align.RIGHT);
                } else {
                    ballManager.hiddenTime(SettingHelper.getSystemSetting().getHostId(), setting.getAccuracy());
                    ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "犯规", 2, false, true, LEDManager.MIDDLE);
                }
                break;
            case RoundResult.RESULT_STATE_BACK:
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "中退", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
                if (setting.getUseLedType() == 0) {
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "中退", Paint.Align.RIGHT);
                } else {
                    ballManager.hiddenTime(SettingHelper.getSystemSetting().getHostId(), setting.getAccuracy());
                    ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "中退", 2, false, true, LEDManager.MIDDLE);
                }
                break;
            case RoundResult.RESULT_STATE_WAIVE:
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "弃权", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
                if (setting.getUseLedType() == 0) {
                    ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "弃权", Paint.Align.RIGHT);
                } else {
                    ballManager.hiddenTime(SettingHelper.getSystemSetting().getHostId(), setting.getAccuracy());
                    ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "弃权", 2, false, true, LEDManager.MIDDLE);
                }
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
        if (pairs.get(0).getCurrentRoundNo() != 0) {
            roundResult.setRoundNo(pairs.get(0).getCurrentRoundNo());
            roundResult.setResultTestState(RoundResult.RESULT_RESURVEY_STATE);
            pairs.get(0).setCurrentRoundNo(0);
        } else {
            roundResult.setRoundNo(roundNo);
            roundResult.setResultTestState(0);
        }
        roundResult.setTestNo(TestCache.getInstance().getTestNoMap().get(student));
        roundResult.setExamType(mStudentItem.getExamType());
        roundResult.setScheduleNo(mStudentItem.getScheduleNo());
        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setTestTime(testDate);
        roundResult.setUpdateState(0);
        roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());
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
            }
        }
        LogUtils.operation("足球拦截添加新成绩:" + roundResult.toString());

        DBManager.getInstance().insertRoundResult(roundResult);
        //获取所有成绩设置为非最好成绩
        List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(mStudentItem);
        TestCache.getInstance().getResults().put(student, results);
        showStuInfoResult();
//        if (mStudentItem.getExamType() == 2) {
//            prepareForCheckIn();
//        }
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
                    , testNo, null, RoundResultBean.beanCope(roundResultList)));
            Logger.i("自动上传成绩:" + uploadResults.toString());
            ServerMessage.uploadResult(uploadResults);
        }
    }

    private void showBeginLed() {
        try {
            byte[] buffer = new byte[16];
            byte[] nameByte = pairs.get(0).getStudent().getLEDStuName().getBytes("GB2312");
            System.arraycopy(nameByte, 0, buffer, 0, nameByte.length);
            nameByte = ("第" + roundNo + "次").getBytes("GB2312");
            System.arraycopy(nameByte, 0, buffer, 10, nameByte.length);
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), buffer, 0, 0, true, true);
            ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "准备", 2, false, true, LEDManager.MIDDLE);
//                ledManager.showString(SettingHelper.getSystemSetting().getHostId(), "", 2, false, true, LEDManager.LEFT, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * 是否需要进行下一次测试
     *
     * @param result
     * @return
     */
    private boolean shouldContinue(int result) {
        TestCache testCache = TestCache.getInstance();
        Student student = testCache.getAllStudents().get(0);
        boolean hasRemain = isExistTestPlace();// 测试次数未完成
        LogUtils.operation("足球进入下一次测试:成绩=" + result + ",测试次数未完成=" + hasRemain + ",准考证号=" + student.getStudentCode());
        return hasRemain;
    }

    private boolean isFullSkip(int result, int resultState) {
        Student student = pairs.get(0).getStudent();
        if (setting.isFullSkip() && resultState == RoundResult.RESULT_STATE_NORMAL) {
//            int result = testResult.getSelectMachineResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            if (student != null) {
                if (student.getSex() == Student.MALE) {
                    return result <= setting.getMaleFullScore() * 1000;
                } else {
                    return result <= setting.getFemaleFullScore() * 1000;
                }
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
                if (setting.getUseLedType() == 0) {
                    ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                } else {
                    ballManager.sendSetStopStatusTo(SettingHelper.getSystemSetting().getHostId(), 0, setting.getAccuracy());
                }
                sleep();
                ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 2);
                if (setting.getTestType() == 1) {
                    facade.awaitState();
                }
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

    private void initAFR() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(setAFRFrameLayoutResID(), afrFragment);
        transaction.commitAllowingStateLoss();// 提交更改
    }

    public int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    public void compareStu(final Student student) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (student != null) {
                    individualCheckFragment.checkQulification(student.getStudentCode(), IndividualCheckFragment.STUDENT_CODE);

                    afrFrameLayout.setVisibility(View.GONE);
                } else {
                    InteractUtils.toastSpeak(FootballIndividualActivity.this, "该考生不存在");
                }
//                if (student == null) {
//                    InteractUtils.toastSpeak(FootballIndividualActivity.this, "该考生不存在");
//                    return;
//                } else {
//                    afrFrameLayout.setVisibility(View.GONE);
//                }
//                StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
//                if (studentItem == null) {
//                    InteractUtils.toastSpeak(FootballIndividualActivity.this, "无此项目");
//                    return;
//                }
//                List<RoundResult> results = DBManager.getInstance().queryResultsByStuItem(studentItem);
//                if (results != null && results.size() >= TestConfigs.getMaxTestCount(FootballIndividualActivity.this)) {
//                    InteractUtils.toastSpeak(FootballIndividualActivity.this, "该考生已测试");
//                    return;
//                }
//                // 可以直接检录
//                onIndividualCheckIn(student, studentItem, results);
            }
        });
    }

    @Override
    public void setRoundNo(Student student, int roundNo) {
        for (StuDevicePair pair : pairs) {
            Student student1 = pair.getStudent();
            if (student1 != null && student1.getStudentCode().equals(student.getStudentCode())) {
                pair.setCurrentRoundNo(roundNo);
            }
        }
    }

}
