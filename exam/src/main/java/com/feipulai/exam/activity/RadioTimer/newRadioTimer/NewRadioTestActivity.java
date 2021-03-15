package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.SportContract;
import com.feipulai.exam.activity.sport_timer.SportPresent;
import com.feipulai.exam.activity.sport_timer.TestState;
import com.feipulai.exam.adapter.PopAdapter;
import com.feipulai.exam.adapter.RunNumberAdapter2;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.CommonPopupWindow;
import com.feipulai.exam.view.ResultPopWindow;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_RESULT;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_START;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_STOP;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_DEVICE;

public class NewRadioTestActivity extends BaseTitleActivity implements SportContract.SportView, TimerKeeper.TimeUpdateListener {

    @BindView(R.id.tv_device_state)
    TextView tvDeviceState;
    @BindView(R.id.tv_num)
    TextView tvNum;
    @BindView(R.id.tv_stuCode)
    TextView tvStuCode;
    @BindView(R.id.tv_stuName)
    TextView tvStuName;
    @BindView(R.id.tv_stuSex)
    TextView tvStuSex;
    @BindView(R.id.tv_stuItem)
    TextView tvStuItem;
    @BindView(R.id.tv_stuMark)
    TextView tvStuMark;
    @BindView(R.id.rv_timer2)
    RecyclerView rvTimer2;
    @BindView(R.id.tv_timer)
    TextView tvTimer;
    @BindView(R.id.tv_run_state)
    TextView tvRunState;
    @BindView(R.id.rl_state)
    RelativeLayout rlState;
    @BindView(R.id.tv_wait_start)
    TextView tvWaitStart;
    @BindView(R.id.tv_wait_ready)
    TextView tvWaitReady;
    @BindView(R.id.tv_fault_back)
    TextView tvFaultBack;
    @BindView(R.id.tv_force_start)
    TextView tvForceStart;
    @BindView(R.id.tv_mark_confirm)
    TextView tvMarkConfirm;
    @BindView(R.id.rl_control)
    RelativeLayout rlControl;
    @BindView(R.id.rl_second)
    RelativeLayout rlSecond;
    private List<RunStudent> mList;
    private RunTimerSetting runTimerSetting;
    private RunNumberAdapter2 mAdapter2;
    private int baseTimer;//初始化时间 用于记录计时器开始计数的时间
    private TimerKeeper timerKeeper;
    private int maxTestTimes;
    private int currentTestTime;
    private SoundPlayUtils playUtils;
    private SportPresent sportPresent;
    private TestState testState;

    private ResultPopWindow resultPopWindow;
    private List<String> marks = new ArrayList<>();
    private String startTime;
    //更换成绩的序号
    private int select;
    private int runNum;
    private int [] independent;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_radio_test;
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        mList = (List<RunStudent>) intent.getSerializableExtra("runStudent");
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        mAdapter2 = new RunNumberAdapter2(mList);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        rvTimer2.setLayoutManager(layoutManager2);
        rvTimer2.setAdapter(mAdapter2);
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        runNum = Integer.parseInt(runTimerSetting.getRunNum());
        timerKeeper = new TimerKeeper(this);
        timerKeeper.keepTime();
        maxTestTimes = runTimerSetting.getTestTimes();
        playUtils = SoundPlayUtils.init(this);

        //机器个数 = (跑到数量+1)*2 或者 (跑到数量+1)
        if (runTimerSetting.getInterceptPoint() == 3){
            sportPresent = new SportPresent(this,runNum *2);
        }else {
            sportPresent = new SportPresent(this, runNum);
        }
        sportPresent.rollConnect();
        sportPresent.setContinueRoll(true);
        testState = TestState.UN_STARTED;
        setView(false);

        PopAdapter popAdapter = new PopAdapter(marks);
        resultPopWindow = new ResultPopWindow(this, popAdapter);
        resultPopWindow.setOnPopItemClickListener(new CommonPopupWindow.OnPopItemClickListener() {
            @Override
            public void itemClick(int position) {
                String result = marks.get(position);
                mList.get(select).setMark(result);
                mList.get(select).setOriginalMark(mList.get(select).getResultList().get(position).getOriResult());
                mAdapter2.notifyDataSetChanged();
            }
        });

        mAdapter2.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                showPop(position, view);
            }
        });
        currentTestTime = 0;
        independent = new int[runNum];
        setIndependent();
    }

    /**
     * 设置分组时间为0
     */
    private void setIndependent(){
        for (int i = 0; i < runNum; i++) {
           independent[i] = 0;
        }
    }

    private void showPop(int pos, View view) {
        marks.clear();
        RunStudent runStudent = mList.get(pos);
        if (runStudent.getStudent() != null) {
            List<RunStudent.WaitResult> hashMap = runStudent.getResultList();
            for (RunStudent.WaitResult entry : hashMap) {
//                Log.i("key= "+entry.getKey()," and value= "+entry.getValue());
                marks.add(entry.getWaitResult());
            }

        }
        resultPopWindow.notifyPop();
        select = pos;
        resultPopWindow.showPopOrDismiss(view);
    }

    private void setView(boolean enable) {
        tvWaitStart.setSelected(!enable);
        tvWaitReady.setSelected(enable);
        tvFaultBack.setSelected(enable);
        tvForceStart.setSelected(enable);
        tvMarkConfirm.setSelected(enable);
    }

    @Override
    public void updateDeviceState(int deviceId, int state) {
        if (runTimerSetting.getInterceptPoint() != 3){
            if (deviceId  > runNum)
                return;
            if (mList.get(deviceId-1).getConnectState() != state){
                mList.get(deviceId-1).setConnectState(state);
                mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
            }

        }else {
            if (deviceId/2  > runNum)
                return;
            if (deviceId <= runNum){
                if (mList.get(deviceId-1).getConnectState() != state){
                    mList.get(deviceId-1).setConnectState(state);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                }
            }else {
                if (mList.get(deviceId/2-1).getConnectState() != state){
                    mList.get(deviceId/2-1).setConnectState(state);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                }
            }
        }

    }

    /**
     * 设置开始启动
     */
    @Override
    public void getDeviceStart() {
        if (testState!= TestState.DATA_DEALING){
            setBeginTime();
        }else {
            sportPresent.setRunState(1);
        }

    }

    private void setBeginTime() {
        sportPresent.setRunState(1);
        baseTimer = sportPresent.getTime();
        testState = TestState.WAIT_RESULT;
        mHandler.sendEmptyMessage(RUN_START);
        timerKeeper.setStartInit();
        currentTestTime++;
        startTime = System.currentTimeMillis()+"";
    }

    @Override
    public void receiveResult(SportResult result) {
        //起终点拦截 独立计时
        if (runTimerSetting.getInterceptPoint() == 3 && runTimerSetting.isTimer_select()){
            if (testState == TestState.DATA_DEALING){
                testState = TestState.WAIT_RESULT;
                mHandler.sendEmptyMessage(RUN_START);
                startTime = System.currentTimeMillis()+"";
                currentTestTime++;
                independent[result.getDeviceId()-1] = sportPresent.getTime();
                return;
            }

            if (result.getDeviceId()< runNum && independent[result.getDeviceId()-1] == 0){//判断是不是第一次启动
                independent[result.getDeviceId()-1] = sportPresent.getTime();
            }else {
                int temp ;
                if (result.getDeviceId()/2 > runNum)
                    return;
                if (result.getDeviceId()%2 == 1){
                    temp = result.getDeviceId()-1;
                }else {
                    temp = result.getDeviceId()/2-1;
                }

                if (null == mList.get(temp).getStudent())
                    return;
                int realTime =  (result.getLongTime() - independent[temp]);
                setRunWayTime(temp, realTime);
            }
        }else {
            //红外拦截并且有起终点
            if (testState == TestState.DATA_DEALING){
                setBeginTime();
                return;
            }
            //假使都是认为发射指令，起点终点不相关
            if (testState == TestState.WAIT_RESULT){
                int temp ;
                if (runTimerSetting.getInterceptPoint() != 3){
                    if (result.getDeviceId()> runNum)
                        return;
                    temp = result.getDeviceId()-1;
                }else {
                    if (result.getDeviceId()/2 > runNum)
                        return;
                    if (result.getDeviceId()%2 == 1){
                        temp = result.getDeviceId()-1;
                    }else {
                        temp = result.getDeviceId()/2-1;
                    }
                }
                if (null == mList.get(temp).getStudent())
                    return;
                int realTime =  (result.getLongTime() - baseTimer);
                setRunWayTime(temp, realTime);
            }
        }


    }

    /**
     * 设定跑道上的时间
     * @param temp 道次
     * @param realTime 时间
     */
    private void setRunWayTime(int temp, int realTime) {
        mList.get(temp).setMark(getFormatTime(realTime));
        mList.get(temp).setOriginalMark(realTime);
        List<RunStudent.WaitResult> list = mList.get(temp).getResultList();
        RunStudent.WaitResult waitResult = new RunStudent.WaitResult();
        waitResult.setOriResult(realTime);
        waitResult.setWaitResult(getFormatTime(realTime));
        list.add(waitResult);
        mHandler.sendEmptyMessage(RUN_RESULT);
    }

    private String getFormatTime(int time) {
        return ResultDisplayUtils.getStrResultForDisplay(time, false);
    }

    /**
     * 指的是机器工作状态结束）
     * @param
     */
    @Override
    public void getDeviceStop() {
        timerKeeper.stopKeepTime();
        mHandler.sendEmptyMessage(RUN_STOP);
        sportPresent.setRunState(0);
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title);
    }

    @Override
    public void onTimeUpdate(int time) {
        tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(time, false));
    }


    @OnClick({R.id.tv_wait_start, R.id.tv_wait_ready, R.id.tv_fault_back, R.id.tv_force_start, R.id.tv_mark_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_wait_start:
                LogUtils.operation("红外计时点击了等待发令");
                boolean flag = false;//标记学生是否全部测试完
                for (RunStudent runStudent : mList) {
                    List<RoundResult> resultList = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                    flag = false;
                    if (resultList.size()>= maxTestTimes){//说明
                        Logger.i(runStudent.getStudent().getId()+"已完成所有测试次数");
                        runStudent.setStudent(null);
                        flag = true;
                        mAdapter2.notifyDataSetChanged();
                    }

                }
                if (flag){
                    ToastUtils.showShort("已完成所有测试次数");
                }
                if (currentTestTime >= maxTestTimes){
                    ToastUtils.showShort("已完成所有测试");
                    return;
                }
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.getResultList().clear();
                }
                mAdapter2.notifyDataSetChanged();
                playUtils.play(13);
                setView(true);
                tvMarkConfirm.setSelected(false);
                testState = TestState.UN_STARTED;
                tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(0, false));
                if (runTimerSetting.getInterceptWay() == 0 && runTimerSetting.getInterceptPoint() == 3){
                    testState = TestState.DATA_DEALING;
                    sportPresent.waitStart();
                }
                break;
            case R.id.tv_wait_ready:
                LogUtils.operation("红外计时点击了预备");
                playUtils.play(14);
                tvWaitReady.setSelected(false);
                break;
            case R.id.tv_fault_back:
                sportPresent.setDeviceStateStop();
                setView(false);
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.getResultList().clear();
                }
                mAdapter2.notifyDataSetChanged();
                testState = TestState.UN_STARTED;
                currentTestTime--;
                setIndependent();
                break;
            case R.id.tv_force_start:
                if (testState == TestState.UN_STARTED){
                    LogUtils.operation("红外计时点击了开始");
                    sportPresent.waitStart();
                    playUtils.play(15);
                }
                if (testState == TestState.DATA_DEALING){
                    playUtils.play(15);
                }
                break;
            case R.id.tv_mark_confirm:
                if (testState == TestState.WAIT_RESULT){
                    LogUtils.operation("红外计时点击了成绩确认");
                    sportPresent.setDeviceStateStop();
                    testState = TestState.RESULT_CONFIRM;
                    sportPresent.setShowLed(mList);
                    for (RunStudent runStudent : mList) {
                        if (runStudent.getStudent() != null) {
                            List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                            sportPresent.saveResultRadio(runStudent.getStudent(), runStudent.getOriginalMark(), results.size()+1, 1,startTime);
                            //是否可以直接增加list.add(getFormatTime(runStudent.getOriginalMark())) ,而不需查询两次数据库
                            List<RoundResult> resultList = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                            List<String> list = new ArrayList<>();
                            for (RoundResult result : resultList) {
                                list.add(getFormatTime(result.getResult()));
                            }
                            sportPresent.printResult(runStudent.getStudent(), list, resultList.size(), maxTestTimes, -1);
                            list.clear();
                        }
                    }
                    setIndependent();
                }

                break;
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case RUN_START:
                    tvWaitStart.setSelected(false);
                    tvWaitReady.setSelected(false);
                    tvForceStart.setSelected(false);
                    tvMarkConfirm.setSelected(true);
                    tvRunState.setText("计时");
                    break;
                case RUN_STOP:
                    setView(false);
                    tvRunState.setText("空闲");
                    break;
                case RUN_RESULT:
                case RUN_UPDATE_DEVICE:
                    mAdapter2.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sportPresent.presentRelease();
        timerKeeper.release();
    }
}
