package com.feipulai.host.activity.radio_timer.newRadioTimer;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.LogUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.radio_timer.RunTimerSetting;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.sporttime.SportContract;
import com.feipulai.host.activity.sporttime.SportPresent;
import com.feipulai.host.activity.sporttime.TestState;
import com.feipulai.host.adapter.PopAdapter;
import com.feipulai.host.bean.RunStudent;
import com.feipulai.host.config.BaseEvent;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.utils.ResultDisplayUtils;
import com.feipulai.host.view.CommonPopupWindow;
import com.feipulai.host.view.ResultPopWindow;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.host.activity.radio_timer.newRadioTimer.pair.RadioConstant.RUN_RESULT;
import static com.feipulai.host.activity.radio_timer.newRadioTimer.pair.RadioConstant.RUN_START;
import static com.feipulai.host.activity.radio_timer.newRadioTimer.pair.RadioConstant.RUN_STOP;
import static com.feipulai.host.activity.radio_timer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_ADD_TIME;
import static com.feipulai.host.activity.radio_timer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_DEVICE;
import static com.feipulai.host.activity.radio_timer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_TEXT;

public class NewRadioTestActivity extends BaseTitleActivity implements SportContract.SportView, TimerTask.TimeUpdateListener {

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
    @BindView(R.id.tv_device_detail)
    TextView tvDetail;
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
    private NewRunAdapter adapter;
    private int baseTimer;//初始化时间 用于记录计时器开始计数的时间
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
    private int[] independent;
    private SparseArray<Integer> array;//有起终点时各道次终点状态 0异常 1正常 2计时
    private TimerTask timerTask;
    private boolean testing;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_radio_test;
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        mList = (List<RunStudent>) intent.getSerializableExtra("runStudent");
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        if (runTimerSetting.getInterceptPoint() == 3 && runTimerSetting.isTimer_select()) {
            adapter = new NewRunAdapter(mList, 1);
            rlState.setVisibility(View.GONE);
        } else {
            adapter = new NewRunAdapter(mList, 0);
        }
        if (runTimerSetting.getInterceptPoint() == 3) {
            array = new SparseArray();
            for (int i = 0; i < mList.size(); i++) {
                array.put(i, 0);
            }
            adapter.setIntercept(1, array);
        }
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        rvTimer2.setLayoutManager(layoutManager2);
        rvTimer2.setAdapter(adapter);
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        runNum = Integer.parseInt(runTimerSetting.getRunNum());
        timerTask = new TimerTask(this, 100);
        timerTask.keepTime();
        maxTestTimes = runTimerSetting.getTestTimes();
        playUtils = SoundPlayUtils.init(this);

        //机器个数 = (跑到数量+1)*2 或者 (跑到数量+1)
        if (runTimerSetting.getInterceptPoint() == 3) {
            sportPresent = new SportPresent(this, runNum * 2);
        } else {
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
                adapter.notifyDataSetChanged();
            }
        });

        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                showPop(position, view);
            }
        });
        currentTestTime = 0;
        independent = new int[runNum];
        setIndependent();
        sportPresent.showReadyLed(mList);
        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {

                alertConfirm(pos);
                return true;
            }
        });
    }

    private void alertConfirm(final int pos){
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.clear_dialog_title))
                .setContentText("是否获取新成绩?")
                .setConfirmText(getString(com.feipulai.common.R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                if (mList.get(pos).getResultList()!= null|| mList.get(pos).getResultList().size()> 0){
                    sportPresent.getDeviceCacheResult(pos+1,mList.get(pos).getResultList().size()+1);
                }else {
                    sportPresent.getDeviceCacheResult(pos+1,1);
                }
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        sportPresent.setContinueRoll(true);
    }

    /**
     * 设置分组时间为0
     */
    private void setIndependent() {
        for (int i = 0; i < runNum; i++) {
            independent[i] = 0;
        }
    }

    private void showPop(int pos, View view) {
        marks.clear();
        RunStudent runStudent = mList.get(pos);
        if (runStudent.getStudent() != null) {
            List<RunStudent.WaitResult> list = runStudent.getResultList();
            for (RunStudent.WaitResult entry : list) {
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
        tvDetail.setSelected(!enable);
    }

    @Override
    public void updateDeviceState(int deviceId, int state) {
        if (runTimerSetting.getInterceptPoint() != 3) {
            if (deviceId > runNum)
                return;
            if (mList.size() == 0)
                return;
            if (mList.get(deviceId - 1).getConnectState() != state) {
                if (state == 2) {//计时状态
                    mList.get(deviceId - 1).setConnectState(2);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                } else if (testState == TestState.UN_STARTED) {
                    mList.get(deviceId - 1).setConnectState(state);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                }

            }

        } else {
            if (deviceId / 2 > runNum)
                return;
            if (deviceId <= runNum) {
                if (state == 2) {//计时状态
                    mList.get(deviceId - 1).setConnectState(2);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                } else if (testState == TestState.UN_STARTED) {
                    mList.get(deviceId - 1).setConnectState(state);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                }
            } else {
                if (array.get(deviceId - runNum - 1) != state) {
                    if (state == 2) {//即将计时
                        array.put(deviceId - runNum - 1, 2);
                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                    } else if (testState == TestState.UN_STARTED) {
                        array.put(deviceId - runNum - 1, state);
                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                    }
                }
            }
        }

    }

    /**
     * 设置开始启动
     */
    @Override
    public void getDeviceStart() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setBeginTime() {
        if (!sportPresent.keepTime){
            return;
        }
        sportPresent.setRunState(1);
        baseTimer = sportPresent.getTime();
//        if (sportPresent.getSynKeep() > 0) {
//            baseTimer = sportPresent.getSynKeep();
//        } else {
//            baseTimer = sportPresent.getTime();
//            sportPresent.setSynKeep(baseTimer);
//        }
        LogUtils.operation("红外计时开始时间baseTimer：" + baseTimer);
        testState = TestState.WAIT_RESULT;
        mHandler.sendEmptyMessage(RUN_START);
        currentTestTime++;
        startTime = System.currentTimeMillis() + "";
        sportPresent.clearLed(1);
        timerTask.setStart();

    }

    @Override
    public void receiveResult(SportResult result) {
        //起终点拦截 独立计时
        if (runTimerSetting.getInterceptPoint() == 3 && runTimerSetting.isTimer_select()) {
            if (testState == TestState.DATA_DEALING) {
                if (result.getDeviceId() > runNum)
                    return;
                testState = TestState.WAIT_RESULT;
                mHandler.sendEmptyMessage(RUN_START);
                startTime = System.currentTimeMillis() + "";
                currentTestTime++;
                independent[result.getDeviceId() - 1] = sportPresent.getTime();
                mHandler.sendEmptyMessage(RUN_UPDATE_ADD_TIME);
                return;
            }

            if (result.getDeviceId() <= runNum && independent[result.getDeviceId() - 1] == 0) {//判断是不是第一次启动
                independent[result.getDeviceId() - 1] = sportPresent.getTime();
            } else {
                int temp;
                if (result.getDeviceId() / 2 > runNum)
                    return;
                if (result.getDeviceId() <= runNum) {
                    temp = result.getDeviceId() - 1;
                } else {
                    temp = (result.getDeviceId() - runNum) - 1;
                }

                if (null == mList.get(temp).getStudent())
                    return;
                int realTime = (result.getLongTime() - independent[temp]);
                setRunWayTime(temp, realTime);
            }
        } else {
            //红外拦截并且有起终点
            if (testState == TestState.DATA_DEALING) {
//                setBeginTime();
                return;
            }
            //假使都是认为发射指令，起点终点不相关
            if (testState == TestState.WAIT_RESULT) {
                int temp;
                if (runTimerSetting.getInterceptPoint() != 3) {
                    if (result.getDeviceId() > runNum)
                        return;
                    temp = result.getDeviceId() - 1;
                } else {
                    if (result.getDeviceId() / 2 > runNum)
                        return;
                    if (result.getDeviceId() <= runNum) {
                        temp = result.getDeviceId() - 1;
                    } else {
                        temp = (result.getDeviceId() - runNum) - 1;
                    }
                }
                if (null == mList.get(temp).getStudent())
                    return;
                LogUtils.operation("baseTimer:"+baseTimer+"result.getLongTime():"+result.getLongTime());
                int realTime = (result.getLongTime() - baseTimer);
                setRunWayTime(temp, realTime);
            }
        }


    }

    /**
     * 设定跑道上的时间
     *
     * @param temp     道次
     * @param realTime 时间
     */
    private void setRunWayTime(int temp, int realTime) {
        List<RunStudent.WaitResult> list = mList.get(temp).getResultList();
        if (realTime <= mList.get(temp).getOriginalMark()) {
            return;
        }
        mList.get(temp).setMark(getFormatTime(realTime));
        mList.get(temp).setOriginalMark(realTime);
        if (!TextUtils.isEmpty(mList.get(temp).getStudent().getStudentName())){
            LogUtils.operation("baseTimer:"+mList.get(temp).getStudent().getStudentName()+realTime);
        }
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
     *
     * @param
     */
    @Override
    public void getDeviceStop() {
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

    @OnClick({R.id.tv_wait_start, R.id.tv_wait_ready, R.id.tv_fault_back, R.id.tv_force_start, R.id.tv_mark_confirm, R.id.tv_device_detail})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_wait_start:
                testing = true;
                LogUtils.operation("红外计时点击了等待发令");

//                if (currentTestTime >= maxTestTimes) {
//                    ToastUtils.showShort("已完成所有测试");
//                    return;
//                }
                for (RunStudent runStudent : mList) {
                    if (null == runStudent.getStudent()) {
                        continue;
                    }
                    runStudent.setMark("");
                    runStudent.setOriginalMark(0);
                    runStudent.getResultList().clear();
                }
                adapter.notifyDataSetChanged();
                setView(true);
                tvMarkConfirm.setSelected(false);
                if (tvWaitStart.getVisibility() == View.VISIBLE) {
                    playUtils.play(13);//播放各就各位
                }
                testState = TestState.DATA_DEALING;
                tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(0, false));
                tvRunState.setText("等待");
//                if (runTimerSetting.getInterceptWay() == 0 && runTimerSetting.getInterceptPoint() != 2) {//红外拦截&&触发方式必须有起点
//                    testState = TestState.DATA_DEALING;
//                    sportPresent.waitStart();
//                }
                sportPresent.showReadyLed(mList);
                sportPresent.waitStart();

                break;
            case R.id.tv_wait_ready:
                LogUtils.operation("红外计时点击了预备");
                playUtils.play(14);
                tvWaitReady.setSelected(false);
                break;
            case R.id.tv_fault_back:
                sportPresent.keepTime = false;
                testing = false;
                timerTask.stopKeepTime();
                sportPresent.setDeviceStateStop();
                setView(false);
                for (RunStudent runStudent : mList) {
                    if (runStudent == null) {
                        continue;
                    }
                    runStudent.setMark("");
                    runStudent.setOriginalMark(0);
                    runStudent.getResultList().clear();
                }
                adapter.notifyDataSetChanged();
                testState = TestState.UN_STARTED;
                currentTestTime--;
                setIndependent();
                break;
            case R.id.tv_force_start:
                if (testState == TestState.UN_STARTED || testState == TestState.DATA_DEALING) {
                    LogUtils.operation("红外计时点击了开始");
                    testState = TestState.FORCE_START;
                    sportPresent.keepTime = true;
                    setBeginTime();
                    playUtils.play(15);
                }
                break;
            case R.id.tv_mark_confirm:
                if (testState == TestState.WAIT_RESULT) {
                    testing = false;
                    LogUtils.operation("红外计时点击了成绩确认+");
                    testState = TestState.UN_STARTED;
                    timerTask.stopKeepTime();
                    sportPresent.setDeviceStateStop();
                    sportPresent.setShowLed(mList);
                    for (RunStudent runStudent : mList) {
                        if (runStudent.getStudent() != null) {
                            List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                            sportPresent.saveResultRadio(runStudent.getStudent(), runStudent.getOriginalMark(), results.size() + 1, 1, startTime);
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
                    sportPresent.keepTime = false;
                    toastSpeak("成绩保存成功，返回中，请等待");
                    EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_COUNT));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 5000);

                }

                break;
            case R.id.tv_device_detail:
                IntentUtil.gotoActivity(this, RadioDeviceDetailActivity.class);
                break;
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
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
                    adapter.notifyDataSetChanged();
                    break;
                case RUN_UPDATE_ADD_TIME:
                    addTime();
                    break;
                case RUN_UPDATE_TEXT:
//                    tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(msg.arg1, false));
                    break;
            }
            return false;
        }
    });

    private void addTime() {
        if (testState == TestState.WAIT_RESULT) {
            mHandler.sendEmptyMessageDelayed(RUN_UPDATE_ADD_TIME, 100);
            for (int i = 0; i < mList.size(); i++) {
                RunStudent runStudent = mList.get(i);
                if (null != runStudent && independent[i] > 0) {
                    runStudent.setIndependentTime(runStudent.getIndependentTime() + 100);
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sportPresent.presentRelease();
        timerTask.release();
        sportPresent = null;
    }

    @Override
    public void finish() {
        if (testing) {
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
    }

    @Override
    public void onTimeTaskUpdate(int time) {
//        Message msg = Message.obtain();
//        msg.what = RUN_UPDATE_TEXT;
//        msg.arg1 = time;
//        mHandler.sendMessage(msg);
        EventBus.getDefault().post(new BaseEvent(time,EventConfigs.UPDATE_TIME));
//        if (testState == TestState.WAIT_RESULT){
//            String formatTime ;
//            if (time<60*60*1000){
//                formatTime = DateUtil.formatTime1(time, "mm:ss.S");
//            }else {
//                formatTime = DateUtil.formatTime1(time, "HH:mm:ss");
//            }
//            sportPresent.showLedString(formatTime);
//        }

    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.UPDATE_TIME){
            tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay((Integer) baseEvent.getData(), false));
        }
    }
}
