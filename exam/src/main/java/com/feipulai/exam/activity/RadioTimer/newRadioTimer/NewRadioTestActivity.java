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

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.SportContract;
import com.feipulai.exam.activity.sport_timer.SportPresent;
import com.feipulai.exam.activity.sport_timer.TestState;
import com.feipulai.exam.adapter.RunNumberAdapter2;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

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
    @BindView(R.id.tv_get_time)
    TextView tvGetTime;
    @BindView(R.id.tv_mark_confirm)
    TextView tvMarkConfirm;
    @BindView(R.id.rl_control)
    RelativeLayout rlControl;
    @BindView(R.id.rl_second)
    RelativeLayout rlSecond;
    private List<RunStudent> mList;
    private RunTimerSetting runTimerSetting;
    private RunNumberAdapter2 mAdapter2;
    private int initTime;//初始化时间 用于记录计时器开始计数的时间
    private int startMode;
    private TimerKeeper timerKeeper;
    private int maxTestTimes;
    private int currentTestTime;
    private SoundPlayUtils playUtils;
    private SportPresent sportPresent;
    private TestState testState;
    private final int RUN_START = 0X01;
    private final int RUN_STOP = 0X02;
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
        startMode = runTimerSetting.getStartPoint();
        timerKeeper = new TimerKeeper(this);
        timerKeeper.keepTime();
        maxTestTimes = runTimerSetting.getTestTimes();
        playUtils = SoundPlayUtils.init(this);
        sportPresent = new SportPresent(this,(Integer.parseInt(runTimerSetting.getRunNum())+1)*2);//机器个数 = (跑到数量+1)*2
        sportPresent.rollConnect();
        sportPresent.setContinueRoll(true);
        startMode = runTimerSetting.getInterceptPoint();
        testState = TestState.UN_STARTED;
        tvGetTime.setVisibility(View.GONE);
        setView(false);
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
//        if (deviceId - 1 >= mList.size())
//            return;
//        if (state != mList.get(deviceId - 1).getConnectState()) {//更新设备连接状态
//            mList.get(deviceId - 1).setConnectState(state);
//            mAdapter2.notifyDataSetChanged();
//        }

    }

    /**
     * 设置开始启动
     */
    @Override
    public void getDeviceStart() {
        initTime = 0;
        testState = TestState.WAIT_RESULT;
        mHandler.sendEmptyMessage(RUN_START);
        timerKeeper.setStartInit();
    }

    @Override
    public void receiveResult(SportResult sportResult) {
        if (testState == TestState.WAIT_RESULT){

        }
    }

    /**
     * 指的是机器工作状态结束）
     * @param
     */
    @Override
    public void getDeviceStop() {
        timerKeeper.stopKeepTime();
        mHandler.sendEmptyMessage(RUN_STOP);
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
                if (currentTestTime >= maxTestTimes)
                    return;
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.getResultList().clear();
                }
                mAdapter2.notifyDataSetChanged();
                playUtils.play(13);
                setView(true);
                tvMarkConfirm.setSelected(false);

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
                break;
            case R.id.tv_force_start:
                sportPresent.waitStart();
                break;
            case R.id.tv_mark_confirm:
                sportPresent.setDeviceStateStop();
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
                    break;
                case RUN_STOP:
                    setView(false);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sportPresent.presentStop();
        timerKeeper.stop();
    }
}
