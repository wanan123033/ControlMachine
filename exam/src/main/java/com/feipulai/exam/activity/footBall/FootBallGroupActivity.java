package com.feipulai.exam.activity.footBall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.NetWorkUtils;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.BallManager;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.device.udp.UdpLEDUtil;
import com.feipulai.device.udp.result.BasketballResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.BasketBallListener;
import com.feipulai.exam.activity.basketball.BasketBallRadioFacade;
import com.feipulai.exam.activity.basketball.TimeUtil;
import com.feipulai.exam.activity.basketball.bean.BallDeviceState;
import com.feipulai.exam.activity.basketball.result.BasketBallTestResult;
import com.feipulai.exam.activity.basketball.util.TimerUtil;
import com.feipulai.exam.activity.footBall.adapter.FootBallResultAdapter;
import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;
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
import com.feipulai.exam.utils.PrintResultUtil;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.EditResultDialog;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class FootBallGroupActivity extends BaseTitleActivity implements TimerUtil.TimerAccepListener, BasketBallListener.BasketBallResponseListener, BaseQuickAdapter.OnItemClickListener {
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
    @BindView(R.id.cb_near)
    CheckBox cbNear;
    @BindView(R.id.cb_far)
    CheckBox cbFar;
    @BindView(R.id.cb_led)
    CheckBox cbLed;
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
    private FootBallResultAdapter resultAdapter;

    private FootBallSetting setting;
    private TimerUtil timerUtil;
    private String testDate;
    private int roundNo;
    private static final String TAG = "FootBallGroupActivity";
    private int useMode;

    private BallManager ballManager;
    private BasketBallRadioFacade facade;
    private long timerDate;
    private String startTime; //开始时间
    private boolean startTest = true;
    private EditResultDialog editResultDialog;
    private List<BaseStuPair> stuPairs;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_basketball;
    }

    @Override
    protected void initData() {
        //获取项目设置
        setting = SharedPrefsUtil.loadFormSource(this, FootBallSetting.class);
        if (setting == null)
            setting = new FootBallSetting();
        LogUtils.operation("项目设置" + setting.toString());
        useMode = setting.getUseMode();
//        //初始化UDP
//        UdpClient.getInstance().init(1527);
//        UdpClient.getInstance().setHostIpPostLocatListener(setting.getHostIp(), setting.getPost(), new BasketBallListener(this));
//        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
//        sleep();
//        //设置精度
//        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_PRECISION(TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1));
//        sleep();
        facade = new BasketBallRadioFacade(setting.getTestType(), this);
        facade.setDeviceVersion(setting.getDeviceVersion());
        ballManager = new BallManager.Builder((setting.getTestType())).setHostIp(setting.getHostIp()).setInetPost(1527).setPost(setting.getPost())
                .setRadioListener(facade).setUdpListerner(new BasketBallListener(this)).build();

        if (setting.getTestType() == 1) {
            facade.resume();
            facade.setInterceptSecond(setting.getInterceptSecond());
        }
        //设置精度
        ballManager.sendSetPrecision(SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(),
                setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() - 1);
        ballManager.sendSetDelicacy(SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(),
                setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1);
        ballManager.sendSetBlockertime(SettingHelper.getSystemSetting().getHostId(), setting.getSensitivity(),
                setting.getInterceptSecond(), TestConfigs.sCurrentItem.getDigital() == 1 ? 0 : 1);


        timerUtil = new TimerUtil(this);
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
        CheckUtils.groupCheck(pairs);

        rvTestingPairs.setLayoutManager(new LinearLayoutManager(this));
        stuPairAdapter = new VolleyBallGroupStuAdapter(pairs);
        rvTestingPairs.setAdapter(stuPairAdapter);
        stuPairAdapter.setOnItemClickListener(this);

        resultAdapter = new FootBallResultAdapter(resultList, setting);
        rvTestResult.setLayoutManager(new LinearLayoutManager(this));
        rvTestResult.setAdapter(resultAdapter);
        resultAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (!isConfigurableNow()) {
                    // 测试信息
                    resultAdapter.setSelectPosition(position);
                    resultAdapter.notifyDataSetChanged();
                }
            }
        });
        if (setting.getTestType() == 0) {
            ballManager.setRadioStopTime(SettingHelper.getSystemSetting().getHostId());
            cbNear.setVisibility(View.GONE);
            cbFar.setVisibility(View.GONE);
            cbLed.setVisibility(View.GONE);
        } else {
            ballManager.setRadioFreeStates(SettingHelper.getSystemSetting().getHostId());
            cbNear.setVisibility(View.VISIBLE);
            cbFar.setVisibility(View.VISIBLE);
            cbLed.setVisibility(View.VISIBLE);
        }
        fristCheckTest();

        editResultDialog = new EditResultDialog(this);
        editResultDialog.setListener(new EditResultDialog.OnInputResultListener() {

            @Override
            public void inputResult(String result, int state) {
                BasketballResult deviceResult = (BasketballResult) pairs.get(position()).getDeviceResult();
                deviceResult.setSecond(Integer.valueOf(result));
                String displayResult = ResultDisplayUtils.getStrResultForDisplay(pairs.get(position()).getDeviceResult().getResult());
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

    private void sleep() {

        try {
            //两个指令相间隔100MS
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    public void finish() {
        if (isConfigurableNow()) {
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
        //清屏
        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, "", Paint.Align.RIGHT);
        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.RIGHT);
        timerUtil.stop();
        facade.finish();
        if (setting.getTestType() == 0) {
            ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
        } else {
            ballManager.setRadioFreeStates(SettingHelper.getSystemSetting().getHostId());
        }

        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        boolean isTestNameEmpty = TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName());
        title = TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId() + "号机"
                + (isTestNameEmpty ? "" : ("-" + SettingHelper.getSystemSetting().getTestName()));
        return builder.setTitle(title);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.BALL_STATE_UPDATE) {
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
    public void getDeviceStatus(int status) {
        LogUtils.operation("足球获取到设备状态值:" + status);
        switch (status) {
            case 1:
                txtDeviceStatus.setText("空闲");
                if (isExistTestPlace()) {
                    state = WAIT_CHECK_IN;
                } else {
                    state = WAIT_FREE;
                }
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
        LogUtils.operation("足球开始计时:useMode=" + useMode + ",result=" + result.toString());
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
//                    UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STATUS(2));
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 2);
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
        timerUtil.startTime(1);
        state = TESTING;
        txtDeviceStatus.setText("计时");
        setOperationUI();
    }

    @Override
    public void getResult(BasketballResult result) {
        LogUtils.operation("足球获取到结果数据:useMode=" + useMode + ",state=" + state + ",result=" + result);
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
//        String time = DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital());
        String time = ResultDisplayUtils.getStrResultForDisplay(result.getResult());
        if (time.charAt(0) == '0' && time.charAt(1) == '0') {
            time = time.substring(3, time.toCharArray().length);
        } else if (time.charAt(0) == '0') {
            time = time.substring(1, time.toCharArray().length);
        }
        tvResult.setText(time);
        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, Paint.Align.RIGHT);
    }

    private void doGetResult(BasketballResult result) {
        pairs.get(position()).setDeviceResult(result);

        state = WAIT_CONFIRM;
        txtDeviceStatus.setText("中断");
        Student student = pairs.get(position()).getStudent();
        List<MachineResult> machineResultList = DBManager.getInstance().getItemGroupFRoundMachineResult(student.getStudentCode()
                , group.getId(),
                roundNo);
        List<RoundResult> resultList1 = DBManager.getInstance().queryResultsByStudentCode(TestConfigs.getCurrentItemCode(),student.getStudentCode(),roundNo);
        MachineResult machineResult = new MachineResult();
        machineResult.setItemCode(TestConfigs.getCurrentItemCode());
        machineResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        machineResult.setTestNo(1);
        machineResult.setRoundNo(roundNo);
        machineResult.setStudentCode(student.getStudentCode());
        machineResult.setResult(result.getResult());
        machineResult.setGroupId(group.getId());
        //第一次拦截保存成绩，其他拦截只保存
        if (machineResultList.size() == 0 || machineResultList == null || resultList1 == null ||resultList1.size() == 0) {
            machineResultList.add(machineResult);
            addRoundResult(result);
            resultList.get(resultAdapter.getSelectPosition()).setMachineResultList(machineResultList);
            resultList.get(resultAdapter.getSelectPosition()).setSelectMachineResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setResult(machineResult.getResult());
            resultList.get(resultAdapter.getSelectPosition()).setPenalizeNum(0);
            resultList.get(resultAdapter.getSelectPosition()).setResultState(RoundResult.RESULT_STATE_NORMAL);
        } else {
            //更新页面轮次数据
            machineResultList.add(machineResult);
            BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
            int pResult = result.getResult() + (testResult.getPenalizeNum() * setting.getPenaltySecond() * 1000);
            testResult.setSelectMachineResult(machineResult.getResult());
            testResult.setResult(pResult);
            if (testResult.getMachineResultList() == null) {
                testResult.setMachineResultList(machineResultList);
            } else {
                testResult.getMachineResultList().clear();
                testResult.getMachineResultList().addAll(machineResultList);
            }
            //更新本次轮次数据库数据
            RoundResult testRoundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId() + "", roundNo);
            testRoundResult.setResult(testResult.getResult());
            testRoundResult.setMachineResult(testResult.getSelectMachineResult());
            testRoundResult.setPenaltyNum(testResult.getPenalizeNum());
            LogUtils.operation("足球更新本次轮次成绩:result = " + testRoundResult.getResult() + "---" + testRoundResult.toString());
            DBManager.getInstance().updateRoundResult(testRoundResult);

            //获取所有成绩设置为非最好成绩
            List<RoundResult> results = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
            //获取最小成绩设置为最好成绩
            RoundResult dbAscResult = DBManager.getInstance().queryGroupOrderAscScore(student.getStudentCode(), group.getId());
            for (RoundResult roundResult : results) {
                if (roundResult.getResult() == dbAscResult.getResult()) {
                    roundResult.setIsLastResult(1);
                } else {
                    roundResult.setIsLastResult(0);
                }
                DBManager.getInstance().updateRoundResult(roundResult);
            }
            if (results != null) {
                TestCache.getInstance().getResults().put(student, results);
            }

        }

        resultAdapter.notifyDataSetChanged();
        DBManager.getInstance().insterMachineResult(machineResult);
        setOperationUI();
    }

    @Override
    public void getStatusStop(BasketballResult result) {
        LogUtils.operation("足球停止计时:state = " + state + ",result = " + result);
        //非测试不做处理
        if (state == WAIT_FREE || state == WAIT_CHECK_IN || state == WAIT_CONFIRM) {
            return;
        }
        timerUtil.stop();
        txtDeviceStatus.setText("停止");

        if (state == WAIT_BEGIN) { //未触发拦截
            state = WAIT_CHECK_IN;
            setOperationUI();
            tvResult.setText(DateUtil.caculateFormatTime(0, TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital()));
//            UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_DIS_LED(2,
//                    UdpLEDUtil.getLedByte("", Paint.Align.RIGHT)));
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.RIGHT);
        } else {
            pairs.get(position()).setDeviceResult(result);
            state = WAIT_STOP;
            setOperationUI();
            tvResult.setText(DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital()));

            String time = DateUtil.caculateFormatTime(result.getResult(), TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital());
            if (time.charAt(0) == '0' && time.charAt(1) == '0') {
                time = time.substring(3, time.toCharArray().length);
            } else if (time.charAt(0) == '0') {
                time = time.substring(1, time.toCharArray().length);
            }
            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, Paint.Align.RIGHT);

        }


    }

    @Override
    public void timer(Long time) {
        timerDate = time;
        if (state == TESTING) {
            tvResult.setText(DateUtil.caculateTime(time, TestConfigs.sCurrentItem.getDigital() == 0 ? 2 : TestConfigs.sCurrentItem.getDigital(), 0));
        }

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

    @OnClick({R.id.tv_punish_add, R.id.tv_punish_subtract, R.id.tv_foul, R.id.tv_inBack, R.id.tv_abandon, R.id.tv_normal, R.id.tv_print, R.id.tv_confirm
            , R.id.txt_waiting, R.id.txt_illegal_return, R.id.txt_continue_run, R.id.txt_stop_timing, R.id.txt_finish_test, R.id.tv_result})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_waiting://等待发令
                LogUtils.operation("足球点击了等待发令");
                if ((state == WAIT_CHECK_IN || state == WAIT_CONFIRM || state == WAIT_STOP)) {
                    if (isExistTestPlace()) {
                        if ((setting.getTestType() == 1 && facade.isDeviceNormal()) || setting.getTestType() == 0) {
                            ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, pairs.get(position()).getStudent().getLEDStuName(), Paint.Align.LEFT);
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

                    } else {
                        toastSpeak("该考生已全部测试完成");
                    }

                }
                break;
            case R.id.txt_illegal_return://违例返回
                LogUtils.operation("足球点击了违例返回");
                showIllegalReturnDialog();
                break;
            case R.id.txt_continue_run://继续运行
                LogUtils.operation("足球点击了继续运行");
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
                        ballManager.setRadioLedStartTime(SettingHelper.getSystemSetting().getHostId(), result);
                        state = TESTING;
                        setOperationUI();
                    }
                }
                break;
            case R.id.txt_stop_timing://停止计时
                LogUtils.operation("足球点击了停止计时");
                ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());

                break;
            case R.id.tv_punish_add: //违例+
                LogUtils.operation("足球点击了违例+");
                setPunish(1);
                break;
            case R.id.tv_punish_subtract://违例-
                LogUtils.operation("足球点击了违例-");
                setPunish(-1);
                break;
            case R.id.tv_foul://犯规
                LogUtils.operation("足球点击了犯规");
                setResultState(RoundResult.RESULT_STATE_FOUL);
                break;
            case R.id.tv_inBack://中退
                LogUtils.operation("足球点击了中退");
                setResultState(RoundResult.RESULT_STATE_BACK);
                break;
            case R.id.tv_abandon://放弃 ;
                LogUtils.operation("足球点击了放弃");
                setResultState(RoundResult.RESULT_STATE_WAIVE);
                break;
            case R.id.tv_normal://正常
                LogUtils.operation("足球点击了正常");
                setResultState(RoundResult.RESULT_STATE_NORMAL);
                break;
            case R.id.tv_print://打印
                LogUtils.operation("足球点击了打印");
                showPrintDialog();

                break;
            case R.id.tv_confirm://确定
                LogUtils.operation("足球点击了确定");
                if (SettingHelper.getSystemSetting().isInputTest()) {
                    onResultConfirmed();
                    return;
                }
                timerUtil.stop();
                if (state == WAIT_CONFIRM || state == WAIT_BEGIN) {
                    ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                    ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 1);
                    startTest = false;
                }
                if (state != TESTING) {
                    tvResult.setText("");
                    if (group.getIsTestComplete() == Group.FINISHED) {
                        toastSpeak("分组考生全部测试完成，请选择下一组");
                    } else {
                        if (TextUtils.isEmpty(testDate)) {
                            testDate = DateUtil.getCurrentTime() + "";
                        }
                        onResultConfirmed();
                    }

                }
                break;
            case R.id.txt_finish_test:
                LogUtils.operation("足球点击了跳过");
                if (state == TESTING) {
                    toastSpeak("测试中,不允许跳过本次测试");
                } else {
                    if (group.getIsTestComplete() == Group.FINISHED) {
                        toastSpeak("分组考生全部测试完成，请选择下一组");
                    } else {
                        timerUtil.stop();
                        if (setting.getTestType() == 0) {
//                        UdpClient.getInstance().send(UDPBasketBallConfig.BASKETBALL_CMD_SET_STOP_STATUS());
                            ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
                        } else {
                            ballManager.sendSetStatus(SettingHelper.getSystemSetting().getHostId(), 1);
                        }
                        prepareForFinish();
                    }
                }

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
        }
    }

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
                                        TestConfigs.getMaxTestCount(FootBallGroupActivity.this), testCache.getTrackNoMap());
                                break;
                        }
                    }
                }).create().show();
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
     * 拦截添加新成绩
     *
     * @param basketballResult
     */
    private void addRoundResult(BasketballResult basketballResult) {
        Student student = pairs.get(position()).getStudent();
        RoundResult roundResult = new RoundResult();
        roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
        roundResult.setStudentCode(student.getStudentCode());
        roundResult.setItemCode(TestConfigs.getCurrentItemCode());
        roundResult.setResult(basketballResult.getResult());
        roundResult.setMachineResult(basketballResult.getResult());
        if (pairs.get(position()).getCurrentRoundNo() != 0){
            roundResult.setRoundNo(pairs.get(position()).getCurrentRoundNo());
            roundResult.setResultTestState(1);
            pairs.get(position()).setCurrentRoundNo(0);
        }else {
            roundResult.setRoundNo(roundNo);
            roundResult.setResultTestState(0);
        }
        roundResult.setTestNo(1);
//        roundResult.setExamType(group.getExamType());
        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),student.getStudentCode());
        if (studentItem != null){
            roundResult.setExamType(studentItem.getExamType());
        }
        roundResult.setScheduleNo(group.getScheduleNo());
        roundResult.setResultState(RoundResult.RESULT_STATE_NORMAL);
        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
        roundResult.setTestTime(testDate);
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
                Logger.i("更新成绩:" + bestResult.toString());
            }
        }
        LogUtils.operation("足球确认保存成绩:result = " + roundResult.getResult() + "---" + roundResult.toString());
        DBManager.getInstance().insertRoundResult(roundResult);
        //获取所有成绩设置为非最好成绩
        List<RoundResult> results = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
        TestCache.getInstance().getResults().put(student, results);
        if (studentItem.getExamType() == 2){
            continuousTestNext();
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
        if (state == TESTING) {
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
        resultAdapter.setSelectPosition(-1);
        Student student = TestCache.getInstance().getAllStudents().get(position());
        List<RoundResult> roundResults = TestCache.getInstance().getResults().get(student);
        roundNo = (roundResults == null ? 1 : roundResults.size() + 1);
        int testNo = setTestCount();
        for (int i = 0; i < testNo; i++) {
            RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId() + "", i + 1);
            if (roundResult == null) {
                resultList.add(new BasketBallTestResult(i + 1, null, 0, -999, 0, -999));
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

    /**
     * 是否存在未测试位置
     */
    private boolean isExistTestPlace() {
        if (resultAdapter.getSelectPosition() == -1)
            return false;
        if (resultList.get(resultAdapter.getSelectPosition()).getResultState() != -999) {
            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getResultState() == -999) {
                    resultAdapter.setSelectPosition(i);
                    if (startTest) {
                        roundNo = i + 1;
                        startTest = false;
                    }

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
                return result <= setting.getMaleFullScore() * 1000;
            } else {
                return result <= setting.getFemaleFullScore() * 1000;
            }
        }
        return false;
    }

    /**
     * 成绩确定
     */
    private void onResultConfirmed() {
        if (pairs.get(position()) == null)
            return;
        Student student = pairs.get(position()).getStudent();
        List<RoundResult> updateResult = new ArrayList<>();
        for (int i = 0; i < resultList.size(); i++) {
            BasketBallTestResult testResult = resultList.get(i);
            if (testResult.getResult() >= 0) {
                RoundResult roundResult = DBManager.getInstance().queryGroupRoundNoResult(student.getStudentCode(), group.getId() + "", resultList.get(i).getRoundNo());
                if (roundResult != null) {
                    //是否有进行更改，有更改成绩为未上传
                    if (roundResult.getResult() != testResult.getResult() || roundResult.getPenaltyNum() != testResult.getPenalizeNum()
                            || roundResult.getResultState() != testResult.getResultState()) {
                        roundResult.setUpdateState(0);
                        roundResult.setResult(testResult.getResult());
                        roundResult.setPenaltyNum(testResult.getPenalizeNum());
                        roundResult.setEndTime(DateUtil.getCurrentTime() + "");
                        roundResult.setResultState(testResult.getResultState());
                        updateResult.add(roundResult);
                    }
                }
            } else {
                if (testResult.getResultState() != -999 && testResult.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                    RoundResult roundResult = new RoundResult();
                    roundResult.setMachineCode(TestConfigs.sCurrentItem.getMachineCode());
                    roundResult.setStudentCode(student.getStudentCode());
                    roundResult.setItemCode(TestConfigs.getCurrentItemCode());
                    roundResult.setResult(0);
                    roundResult.setMachineResult(0);
                    roundResult.setResultState(testResult.getResultState());
                    roundResult.setTestTime(startTime);
                    roundResult.setEndTime(System.currentTimeMillis() + "");
                    if (pairs.get(position()).getCurrentRoundNo() != 0){
                        roundResult.setRoundNo(pairs.get(position()).getCurrentRoundNo());
                        pairs.get(position()).setCurrentRoundNo(0);
                        List<BaseStuPair> stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
                        if (stuPairs != null){
                            for (BaseStuPair pp : stuPairs){
                                if (pp.getStudent().getStudentCode().equals(pairs.get(position()).getStudent().getStudentCode()))
                                    pp.setRoundNo(0);
                            }
                        }
                    }else {
                        roundResult.setRoundNo(resultList.get(i).getRoundNo());
                    }
                    roundResult.setTestNo(1);
                    roundResult.setExamType(group.getExamType());
                    roundResult.setScheduleNo(group.getScheduleNo());
                    roundResult.setUpdateState(0);
                    roundResult.setGroupId(group.getId());
                    roundResult.setMtEquipment(SettingHelper.getSystemSetting().getBindDeviceName());

                    DBManager.getInstance().insertRoundResult(roundResult);
                    SystemSetting setting = SettingHelper.getSystemSetting();
                    StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),stuPairs.get(stuPairAdapter.getTestPosition()).getStudent().getStudentCode());
                    //判断是否开启补考需要加上是否已完成本次补考,并将学生改为已补考
                    if ((setting.isResit() || studentItem.getMakeUpType() == 1) && !stuPairs.get(stuPairAdapter.getTestPosition()).isResit()){
                        stuPairs.get(stuPairAdapter.getTestPosition()).setResit(true);
                    }
                    resultList.get(i).setResult(0);
                    resultAdapter.notifyDataSetChanged();
                }

            }

        }
        showLedConfirmedResult();
        if (updateResult.size() > 0) {
            DBManager.getInstance().updateRoundResult(updateResult);
        }

        List<RoundResult> dbRoundResult = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
        //获取最小成绩设置为最好成绩
        RoundResult dbAscResult = DBManager.getInstance().queryGroupOrderAscScore(student.getStudentCode(), group.getId());


        if (dbAscResult != null) {
            //获取所有成绩设置为非最好成绩
            for (RoundResult roundResult : dbRoundResult) {
                if (roundResult.getResult() == dbAscResult.getResult()) {
                    dbAscResult.setIsLastResult(1);
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
            TestCache.getInstance().getResults().put(student, dbRoundResult);
        }


        uploadResults();

        nextTest();

    }

    private void showLedConfirmedResult() {
        if (resultAdapter.getSelectPosition() == -1)
            return;
        BasketBallTestResult testResult = resultList.get(resultAdapter.getSelectPosition());
        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, pairs.get(position()).getStudent().getLEDStuName(), testResult.getPenalizeNum() + "", Paint.Align.LEFT);
        switch (testResult.getResultState()) {
            case RoundResult.RESULT_STATE_NORMAL:
                String time = ResultDisplayUtils.getStrResultForDisplay(testResult.getResult());
                if (time.charAt(0) == '0' && time.charAt(1) == '0') {
                    time = time.substring(3, time.toCharArray().length);
                } else if (time.charAt(0) == '0') {
                    time = time.substring(1, time.toCharArray().length);
                }
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, testResult.getPenalizeNum() + "", Paint.Align.CENTER);

                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, time, Paint.Align.RIGHT);
                break;
            case RoundResult.RESULT_STATE_FOUL:
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "犯规", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "犯规", Paint.Align.RIGHT);
                break;
            case RoundResult.RESULT_STATE_BACK:
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "中退", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "中退", Paint.Align.RIGHT);
                break;
            case RoundResult.RESULT_STATE_WAIVE:
//                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "弃权", testResult.getPenalizeNum() + "", Paint.Align.CENTER);
                ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "弃权", Paint.Align.RIGHT);
                break;

        }
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

    /**
     * 等待
     */
    private void prepareForBegin() {
        LogUtils.operation("足球考生:" + pairs.get(position()).getStudent().getSpeakStuName() + "进行第" + roundNo + "轮测试");
        Student student = pairs.get(position()).getStudent();
        tvResult.setText(student.getStudentName());
        state = WAIT_CHECK_IN;
        setOperationUI();
        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 1, student.getLEDStuName(), Paint.Align.LEFT);
        ballManager.sendDisLed(SettingHelper.getSystemSetting().getHostId(), 2, "", Paint.Align.CENTER);
    }


    /**
     * 结束 下一位
     */
    private void prepareForFinish() {
//        Student student = pairs.get(position()).getStudent();
//        List<RoundResult> dbRoundResult = DBManager.getInstance().queryGroupRound(student.getStudentCode(), group.getId() + "");
//        if (dbRoundResult != null) {
//            TestCache.getInstance().getResults().put(student, dbRoundResult);
//        }
//        uploadResults();
        state = WAIT_FREE;
        setOperationUI();
//        nextTest();
        if (setting.getTestPattern() == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
            continuousTestNext();
        } else {
            //循环
            loopTestNext();
        }
    }

    private void nextTest() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
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
        if (roundNo <= setTestCount()) {
            //是否存在可以测试位置
            if (isExistTestPlace()) {
                prepareForBegin();
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
            } else {
                continuousTestNext();
            }
        } else {
            //是否测试到最后一位
            if (position() == pairs.size() - 1) {
                fristCheckTest();
            } else {
                continuousTestNext();
            }


        }
    }

    private int setTestCount() {
//        SystemSetting setting = SettingHelper.getSystemSetting();
//        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),stuPairs.get(position()).getStudent().getStudentCode());
//        if (setting.isResit() || studentItem.getMakeUpType()==1){
//            return stuPairs.get(position()).getTestNo() == -1 ? TestConfigs.getMaxTestCount() : stuPairs.get(position()).getTestNo();
//        }
        return TestConfigs.getMaxTestCount();
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
            presetResult();
            prepareForBegin();
            //最后一次测试的成绩
            toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                    String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));


            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);
            stuPairAdapter.notifyDataSetChanged();
            return;
        }
        //全部次数测试完，
        fristCheckTest();

    }

//    private void loopTest() {
//        //是否测试到最后一位
//        if (stuPairAdapter.getTestPosition() == pairs.size() - 1) {
//            fristCheckTest();
//        } else {
//            loopTestNext();
//        }
//    }

    private void fristCheckTest() {
        //是否为最后一次测试，开启新的测试
        for (int i = 0; i < pairs.size(); i++) {
            if (!isStuAllTest(pairs.get(i).getStudent().getStudentCode())) {
                stuPairAdapter.setTestPosition(i);
                rvTestingPairs.scrollToPosition(i);
                presetResult();
                isExistTestPlace();
                prepareForBegin();
                toastSpeak(String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getSpeakStuName(), roundNo),
                        String.format(getString(R.string.test_speak_hint), pairs.get(position()).getStudent().getStudentName(), roundNo));
                stuPairAdapter.notifyDataSetChanged();
                return;
            }
        }
        if (SettingHelper.getSystemSetting().isAutoPrint()) {
            TestCache testCache = TestCache.getInstance();
            InteractUtils.printResults(group, testCache.getAllStudents(), testCache.getResults(),
                    TestConfigs.getMaxTestCount(FootBallGroupActivity.this), testCache.getTrackNoMap());

        }
        allTestComplete();
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

            stuPairAdapter.notifyDataSetChanged();

            group.setIsTestComplete(2);
            DBManager.getInstance().updateGroup(group);

            return;
        }
//        if (position() + 1 < pairs.size()) {
//            loopTestNext();
//        }
        fristCheckTest();
    }


    /**
     * 全部次数测试完
     */
    private void allTestComplete() {
        LogUtils.operation("足球分组模式测试完成");
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        if (group.getIsTestComplete() != 1 &&
                SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN &&
                (SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_A4
                        || SettingHelper.getSystemSetting().getPrintTool() == SystemSetting.PRINT_CUSTOM_APP) &&
                SettingHelper.getSystemSetting().isAutoPrint()) {
            InteractUtils.printA4Result(this, group);
        }
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
        state = WAIT_FREE;
        setOperationUI();

        //        TestCache testCache = TestCache.getInstance();
//        if (SettingHelper.getSystemSetting().isAutoPrint() && isExistTestPlace()) {
//            InteractUtils.printResults(null, testCache.getAllStudents(), testCache.getResults(),
//                    TestConfigs.getMaxTestCount(this), testCache.getTrackNoMap());
//        }
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
        //成绩数量是否小于测试次数
        if (roundResultList.size() < TestConfigs.getMaxTestCount(this)) {
            boolean isSkip = false;
            for (RoundResult roundResult : roundResultList) {
                //成绩是否存在满分跳过
                if (isFullSkip(roundResult.getResult(), roundResult.getResultState())) {
                    LogUtils.operation("足球考生:" + studentCode + "第" + roundNo + "轮已满分,跳过测试");
                    isSkip = true;
                }
            }
            if (!isSkip) {
                return false;
            }
        }
        return true;

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
                    Student student = pairs.get(position()).getStudent();
                    int testNo = 1;
                    DBManager.getInstance().deleteStuResult(student.getStudentCode(), testNo, roundNo, group.getId());
                    DBManager.getInstance().deleteStuMachineResults(student.getStudentCode(), testNo, roundNo, group.getId());
                    testResult.setSelectMachineResult(0);
                    testResult.setResult(-999);
                    testResult.setResultState(-999);
                    testResult.setMachineResultList(null);
                    resultAdapter.notifyDataSetChanged();
                    LogUtils.operation("足球考生" + student.getStudentName() + "第" + roundNo + "轮进行违规返回");
                }
                state = WAIT_CONFIRM;
                ballManager.sendSetStopStatus(SettingHelper.getSystemSetting().getHostId());
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
//                List<MachineResult> machineResultList = resultList.get(resultAdapter.getSelectPosition()).getMachineResultList();
//                if (machineResultList != null && machineResultList.size() > 0) {
//                    txtIllegalReturn.setEnabled(false);
//                } else {
//                    txtIllegalReturn.setEnabled(true);
//                }
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
