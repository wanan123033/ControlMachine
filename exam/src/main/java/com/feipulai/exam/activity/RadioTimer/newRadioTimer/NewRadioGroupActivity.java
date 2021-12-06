package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.SportResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.RadioTimer.RunTimerSetting;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.SportContract;
import com.feipulai.exam.activity.sport_timer.SportPresent;
import com.feipulai.exam.activity.sport_timer.TestState;
import com.feipulai.exam.adapter.PopAdapter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.utils.FileUtils;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.CommonPopupWindow;
import com.feipulai.exam.view.ResultPopWindow;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_RESULT;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_START;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_STOP;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_ADD_TIME;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_COMPLETE;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_DEVICE;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_TEXT;

public class NewRadioGroupActivity extends BaseTitleActivity implements SportContract.SportView, TimerTask.TimeUpdateListener {
    @BindView(R.id.rl_control)
    RelativeLayout rlControl;
    @BindView(R.id.rv_timer)
    RecyclerView rvTimer;
    @BindView(R.id.tv_wait_start)
    TextView tvWaitStart;
    @BindView(R.id.tv_force_start)
    TextView tvForceStart;
    @BindView(R.id.tv_fault_back)
    TextView tvFaultBack;
    @BindView(R.id.tv_mark_confirm)
    TextView tvMarkConfirm;
    @BindView(R.id.tv_timer)
    TextView tvTimer;
    @BindView(R.id.tv_device_detail)
    TextView tvDetail;
    @BindView(R.id.tv_run_state)
    TextView tvRunState;
    @BindView(R.id.tv_wait_ready)
    TextView tvWaitReady;
    @BindView(R.id.rl_state)
    RelativeLayout rlState;
    private int[] independent;
    /**
     * 从分组信息中选择的学生信息
     */
    private List<RunStudent> groupRunList = new ArrayList<>();
    private Group group;
    private List<BaseStuPair> pairs;
    private ResultPopWindow resultPopWindow;
    private List<String> marks = new ArrayList<>();
    private int select;
    private List<RunStudent> tempGroup = new ArrayList<>();
    private SoundPlayUtils playUtils;
    private String startTime;
    private int currentTestTime = 0;
    private List<RunStudent> mList = new ArrayList<>();//测试的
    private NewRunAdapter mAdapter;
    private int runNum = 1;
    private int maxTestTimes = 1;
    private SportPresent sportPresent;
    private RunTimerSetting runTimerSetting;
    private TestState testState;
    private int baseTimer;//初始化时间 用于记录计时器开始计数的时间
    private TimerTask timerKeeper;
    private SparseArray<Integer> array;//有起终点时各道次终点状态
    private static final String TAG = "NewRadioGroupActivity";
    ExecutorService service = Executors.newFixedThreadPool(2);
    private boolean testing;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_radio_group;
    }

    @Override
    protected void initData() {
        runTimerSetting = SharedPrefsUtil.loadFormSource(this, RunTimerSetting.class);
        runNum = Integer.parseInt(runTimerSetting.getRunNum());
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            maxTestTimes = TestConfigs.sCurrentItem.getTestNum();
        } else {
            maxTestTimes = runTimerSetting.getTestTimes();
        }
        playUtils = SoundPlayUtils.init(this);
        mList.clear();
        for (int i = 0; i < runNum; i++) {
            RunStudent runStudent = new RunStudent();
            runStudent.setResultList(new ArrayList<RunStudent.WaitResult>());
            mList.add(runStudent);
        }
        if (runTimerSetting.getInterceptPoint() == 3 && runTimerSetting.isTimer_select()) {
            mAdapter = new NewRunAdapter(mList, 1);
            rlState.setVisibility(View.GONE);
        } else {
            mAdapter = new NewRunAdapter(mList, 0);
        }
        if (runTimerSetting.getInterceptPoint() == 3) {
            array = new SparseArray();
            for (int i = 0; i < mList.size(); i++) {
                array.put(i, 0);
            }
            mAdapter.setIntercept(1, array);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTimer.setLayoutManager(layoutManager);
        rvTimer.setAdapter(mAdapter);
        group = (Group) TestConfigs.baseGroupMap.get("group");
        pairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
//        if (group != null)
//            LogUtils.operation("红外计时获取到分组信息:" + group.toString());
        if (pairs != null)
            LogUtils.operation("红外计时获取到分组信息:" + pairs.toString());
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

//                deleteDialog(position);
                showPop(position, view);
            }
        });

        getTestStudent();
        PopAdapter popAdapter = new PopAdapter(marks);
        resultPopWindow = new ResultPopWindow(this, popAdapter);
        resultPopWindow.setOnPopItemClickListener(new CommonPopupWindow.OnPopItemClickListener() {
            @Override
            public void itemClick(int position) {
                String result = marks.get(position);
                mList.get(select).setMark(result);
                mList.get(select).setOriginalMark(mList.get(select).getResultList().get(position).getOriResult());
                mAdapter.notifyDataSetChanged();
            }
        });
        setView(false);
        if (runTimerSetting.getInterceptPoint() == 3) {
            sportPresent = new SportPresent(this, (runNum * 2));
        } else {
            sportPresent = new SportPresent(this, runNum);
        }
        sportPresent.setContinueRoll(true);
        sportPresent.rollConnect();
        testState = TestState.UN_STARTED;
        timerKeeper = new TimerTask(this, 100);
        timerKeeper.keepTime();
        independent = new int[runNum];
        setIndependent();
        sportPresent.showReadyLed(mList);

        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {

                alertConfirm(pos);
                return true;
            }
        });
    }

    private void alertConfirm(final int pos) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.clear_dialog_title))
                .setContentText(pos == -1 ? "是否保存成绩?" : "是否获取新成绩?")
                .setConfirmText(getString(com.feipulai.common.R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                if (pos == -1) {
                    saveResult();
                } else {
                    if (mList.get(pos).getResultList() != null || mList.get(pos).getResultList().size() > 0) {
                        sportPresent.getDeviceCacheResult(pos + 1, mList.get(pos).getResultList().size() + 1);
                    } else {
                        sportPresent.getDeviceCacheResult(pos + 1, 1);
                    }
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

    private void setView(boolean enable) {
        tvWaitStart.setSelected(!enable);
        tvWaitReady.setSelected(enable);
        tvFaultBack.setSelected(enable);
        tvForceStart.setSelected(enable);
        tvMarkConfirm.setSelected(enable);
        tvDetail.setSelected(!enable);
    }

    /**
     * 获取需要测试的考生 将考生放入跑道
     */
    private void getTestStudent() {
        currentTestTime = 0;
        groupRunList.clear();
        tempGroup.clear();
        //long groupId = group.getId();
        for (BaseStuPair pair : pairs) {
            RunStudent runStudent = new RunStudent();
            runStudent.setStudent(pair.getStudent());
            runStudent.setTrackNo(pair.getTrackNo());

            groupRunList.add(runStudent);
        }
        tempGroup.addAll(groupRunList);

        if (tempGroup.get(0) != null) {
            int size = DBManager.getInstance().
                    queryGroupRound(tempGroup.get(0).getStudent().getStudentCode(), group.getId() + "").size();
            currentTestTime = size;
        }
        // 将考生放入跑道中
        addToRunWay();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 将学生加入跑道
     */
    private void addToRunWay() {
        for (int i = 0; i < runNum; i++) {
            if (mList.size() < i + 1)
                return;
            if (tempGroup.size() > i) {
                int size = DBManager.getInstance().
                        queryGroupRound(tempGroup.get(i).getStudent().getStudentCode(), group.getId() + "").size();
                if (size < maxTestTimes) {
                    mList.get(i).setStudent(tempGroup.get(i).getStudent());
                    mList.get(i).setOriginalMark(0);
                    mList.get(i).setMark("");
                    mList.get(i).setIndependentTime(0);
                    LogUtils.operation("红外计时已在准备的学生:" + tempGroup.get(i).getStudent().toString());
                } else {
                    List<RoundResult> roundResults = DBManager.getInstance().
                            queryGroupRound(tempGroup.get(i).getStudent().getStudentCode(), group.getId() + "");
                    List<String> list = new ArrayList<>();
                    for (RoundResult result : roundResults) {
                        list.add(getFormatTime(result.getResult()));
                    }
//                    disposeManager.printResult(tempGroup.get(i).getStudent(), list, maxTestTimes, maxTestTimes, group.getGroupNo());
                    list.clear();
                    mList.get(i).setStudent(null);
                    mList.get(i).setOriginalMark(0);
                    mList.get(i).setMark("");
                    mList.get(i).setIndependentTime(0);
                }
            } else {
                mList.get(i).setStudent(null);
                mList.get(i).setOriginalMark(0);
                mList.get(i).setMark("");
                mList.get(i).setIndependentTime(0);
            }
        }

        //将临时的学生组移除
        for (int i = 0; i < runNum; i++) {
            if (tempGroup.size() > 0) {
                tempGroup.remove(0);
            } else {
                break;
            }
        }

    }

    private String getFormatTime(int time) {
        return ResultDisplayUtils.getStrResultForDisplay(time, false);
    }

    private void showPop(int pos, View view) {
        marks.clear();
        RunStudent runStudent = mList.get(pos);
        if (runStudent.getStudent() != null) {
            List<RunStudent.WaitResult> hashMap = runStudent.getResultList();
            for (RunStudent.WaitResult entry : hashMap) {
                Log.i("key= " + entry, " and value= " + entry);
                marks.add(entry.getWaitResult());
            }

        }
        resultPopWindow.notifyPop();
        select = pos;
        resultPopWindow.showPopOrDismiss(view);
    }

    @OnClick({R.id.tv_wait_start, R.id.tv_force_start, R.id.tv_fault_back, R.id.tv_mark_confirm, R.id.tv_wait_ready, R.id.tv_device_detail})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_wait_start://等待发令
                timerKeeper.stopKeepTime();
                testing = true;
                LogUtils.operation("红外计时点击了等待发令");
                boolean flag = false;//标记学生是否全部测试完
                for (RunStudent runStudent : mList) {
                    if (null == runStudent || null == runStudent.getStudent())
                        continue;
                    List<RoundResult> resultList = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                    flag = false;
                    if (resultList.size() >= maxTestTimes) {//说明
                        Logger.i(runStudent.getStudent().getId() + "已完成所有测试次数");
                        runStudent.setStudent(null);
                        flag = true;
                        mAdapter.notifyDataSetChanged();
                    }

                }
                if (flag) {
                    ToastUtils.showShort("已完成所有测试次数");
                }
                if (currentTestTime >= maxTestTimes) {
                    ToastUtils.showShort("已完成所有测试");
                    return;
                }
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.setOriginalMark(0);
                    runStudent.getResultList().clear();
                    runStudent.setIndependentTime(0);
                }
                mAdapter.notifyDataSetChanged();
                setIndependent();
                setView(true);
                tvMarkConfirm.setSelected(false);
                if (tvWaitStart.getVisibility() == View.VISIBLE) {
                    playUtils.play(13);//播放各就各位
                }
                testState = TestState.UN_STARTED;
                tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(0, false));
                tvRunState.setText("等待");
//                if (runTimerSetting.getInterceptWay() == 0 && runTimerSetting.getInterceptPoint() != 2) {//红外拦截&&触发方式必须有起点
                testState = TestState.DATA_DEALING;//预处理
                sportPresent.waitStart();
                sportPresent.showReadyLed(mList);
//                }
                break;
            case R.id.tv_force_start://强制启动
                if (!isDeviceReady()) {
                    alertConfirm();
                    return;
                }
                if (testState == TestState.UN_STARTED || testState == TestState.DATA_DEALING) {
                    LogUtils.operation("红外计时点击了开始");
                    testState = TestState.FORCE_START;
                    setBeginTime();
                    playUtils.play(15);
                }

                break;
            case R.id.tv_wait_ready://预备
                LogUtils.operation("红外计时点击了预备");
                playUtils.play(14);
                tvWaitReady.setSelected(false);
                break;
            case R.id.tv_fault_back://违规返回
                testing = false;
                LogUtils.operation("红外计时点击了违规返回");
                sportPresent.setDeviceStateStop();
                setView(false);
                timerKeeper.stopKeepTime();
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.setOriginalMark(0);
                    runStudent.getResultList().clear();
                    runStudent.setIndependentTime(0);
                }
                mAdapter.notifyDataSetChanged();
                testState = TestState.UN_STARTED;
                currentTestTime--;
                setIndependent();
                break;
            case R.id.tv_mark_confirm://成绩确认
                boolean b = true;
                for (RunStudent runStudent : mList) {
                    if (TextUtils.isEmpty(runStudent.getMark())) {
                        b = false;
                        break;
                    }
                }
                if (!b) {
                    alertConfirm(-1);
                    return;
                }
                saveResult();
                break;
            case R.id.tv_device_detail:
                IntentUtil.gotoActivity(this, RadioDeviceDetailActivity.class);
                break;
        }
    }

    private void saveResult() {
        if (testState == TestState.WAIT_RESULT) {
            testing = false;
            timerKeeper.stopKeepTime();
            sportPresent.setShowLed(mList);
            LogUtils.operation("红外计时点击了成绩确认");
            sportPresent.setDeviceStateStop();
            testState = TestState.UN_STARTED;
            updateComplete();
        }
    }

    private void updateComplete() {
        MyRunnable r = new MyRunnable();
        service.submit(r);
    }

    private class MyRunnable implements Runnable {

        @Override
        public void run() {
            group.setIsTestComplete(2);
            confirmResult();
            for (int i = 0; i < runNum; i++) {
                if (mList.size() < i + 1)
                    break;
                mList.get(i).setMark("");
                mList.get(i).setStudent(null);
            }
            cycleRun();
            mHandler.sendEmptyMessage(RUN_UPDATE_COMPLETE);
            setIndependent();
        }
    }

    /**
     * 循环测试
     */
    private void cycleRun() {
        boolean isAdd = true;
        if (tempGroup.size() <= 0) {
            currentTestTime++;
            if (currentTestTime < maxTestTimes) {
                tempGroup.addAll(groupRunList);
            } else {
                isAdd = false;
                allTestComplete();
            }
        }
        if (isAdd) {
            addToRunWay();
        }
    }

    /**
     * 全部次数测试完
     */
    private void allTestComplete() {
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
//        mList.clear();
//        tempGroup.clear();
//        mAdapter.notifyDataSetChanged();
        finish();
    }

    /**
     * 成绩确认保存上传
     */
    private void confirmResult() {
        for (RunStudent runStudent : mList) {
            if (runStudent.getStudent() != null && !TextUtils.isEmpty(runStudent.getMark())) {
                List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                sportPresent.saveGroupResult(runStudent.getStudent(), runStudent.getOriginalMark(), RoundResult.RESULT_STATE_NORMAL, results.size() + 1, group, startTime, false);
                List<RoundResult> resultList = DBManager.getInstance().queryGroupRound(runStudent.getStudent().getStudentCode(), group.getId() + "");
                List<String> list = new ArrayList<>();
                for (RoundResult result : resultList) {
                    list.add(getFormatTime(result.getResult()));
                }
                sportPresent.printResult(runStudent.getStudent(), list, results.size() + 1, maxTestTimes, group.getGroupNo());
                list.clear();
            }
        }
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
    public void updateDeviceState(int deviceId, int state) {
        if (mList.size() == 0)
            return;
        if (runTimerSetting.getInterceptPoint() != 3) {
            if (deviceId > runNum)
                return;
            if (mList.get(deviceId - 1).getConnectState() != state) {
//                if ( state == 2) {//不处于计时状态
//                    mList.get(deviceId - 1).setConnectState(2);
//                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                } else if (testState == TestState.UN_STARTED) {
//                    mList.get(deviceId - 1).setConnectState(state);
//                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                }
                mList.get(deviceId - 1).setConnectState(state);
                mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
            }

        } else {
            if (deviceId / 2 > runNum)
                return;
            if (deviceId <= runNum) {
                if (mList.get(deviceId - 1).getConnectState() != state) {
//                    if ( state == 2) {//计时状态
//                        mList.get(deviceId - 1).setConnectState(2);
//                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                    } else if (testState == TestState.UN_STARTED) {
//                        mList.get(deviceId - 1).setConnectState(state);
//                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                    }
                    mList.get(deviceId - 1).setConnectState(state);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                }
            } else {
                if (array.get(deviceId - runNum - 1) != state) {
//                    if ( state == 2) {//即将计时
//                        array.put(deviceId - runNum - 1, 2);
//                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                    } else if (testState == TestState.UN_STARTED) {
//                        array.put(deviceId - runNum - 1, state);
//                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                    }
                    array.put(deviceId - runNum - 1, state);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                }
            }
        }

    }

    @Override
    public void getDeviceStart() {
//        if (sportPresent.getRunState() == 1){
//            return;
//        }
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        sportPresent.setRunState(1);
    }

    private void setBeginTime() {
        sportPresent.setRunState(1);
        if (sportPresent.getSynKeep() > 0) {
            baseTimer = sportPresent.getSynKeep();
        } else {
            baseTimer = sportPresent.getTime();
            sportPresent.setSynKeep(baseTimer);
        }
        FileUtils.log("红外计时开始时间：" + baseTimer);
        testState = TestState.WAIT_RESULT;
        mHandler.sendEmptyMessage(RUN_START);
        timerKeeper.setStart();
        startTime = System.currentTimeMillis() + "";
        sportPresent.clearLed(1);
    }

    @Override
    public void receiveResult(SportResult result) {
        if (testState != TestState.WAIT_RESULT) {
            return;
        }
        //起终点拦截 独立计时
        if (runTimerSetting.getInterceptPoint() == 3 && runTimerSetting.isTimer_select()) {
            if (testState == TestState.DATA_DEALING) {
                if (result.getDeviceId() > runNum)
                    return;
                testState = TestState.WAIT_RESULT;
                mHandler.sendEmptyMessage(RUN_START);
                startTime = System.currentTimeMillis() + "";
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
        }
        //红外拦截并且有起终点
        else {
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
                LogUtils.operation("time:" + result.getLongTime() + "base:" + baseTimer);
                int realTime = (result.getLongTime() - baseTimer);
                if (realTime < 0)
                    return;
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
        if (mList.size() == 0) {
            return;
        }
        List<RunStudent.WaitResult> list = mList.get(temp).getResultList();

        if (realTime <= mList.get(temp).getOriginalMark()) {
            return;
        }
        for (RunStudent runStudent : groupRunList) {
            if (TextUtils.equals(runStudent.getStudent().getStudentCode(), mList.get(temp).getStudent().getStudentCode())) {
                runStudent.setIndependentTime(realTime);
            }
        }
        mList.get(temp).setMark(getFormatTime(realTime));
        mList.get(temp).setOriginalMark(realTime);

        RunStudent.WaitResult waitResult = new RunStudent.WaitResult();
        waitResult.setOriResult(realTime);
        waitResult.setWaitResult(getFormatTime(realTime));
        list.add(waitResult);
        mHandler.sendEmptyMessage(RUN_RESULT);
    }

    @Override
    public void getDeviceStop() {
        if (sportPresent.getRunState() == 0)
            return;
        timerKeeper.stopKeepTime();
        mHandler.sendEmptyMessage(RUN_STOP);
        sportPresent.setRunState(0);
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
                    mAdapter.notifyDataSetChanged();
                    break;
                case RUN_UPDATE_ADD_TIME:
                    addTime();
                    break;
                case RUN_UPDATE_COMPLETE:
                    mAdapter.notifyDataSetChanged();
                    break;
                case RUN_UPDATE_TEXT:
                    tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(msg.arg1, false));
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
                    mAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    private void alertConfirm() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("存在设备非计时状态")
                .setContentText("开始计时?")
                .setConfirmText(getString(com.feipulai.common.R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                testState = TestState.FORCE_START;
                setBeginTime();
                playUtils.play(15);
            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
            }
        }).show();
    }

    /**
     * 判断计时器是否进入计时状态
     */
    private boolean isDeviceReady() {
        boolean flag = true;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).getConnectState() != 2) {
                flag = false;
                break;
            }
        }

        if (runTimerSetting.getInterceptPoint() == 3) {
            for (int i = 0; i < array.size(); i++) {
                Integer index = array.valueAt(i);
                if (index != 2) {
                    flag = false;
                    break;
                }
            }
        }

        return flag;
    }

    @Override
    public void onTimeTaskUpdate(int time) {
//        Message msg = Message.obtain();
//        msg.what = RUN_UPDATE_TEXT;
//        msg.arg1 = time;
//        mHandler.sendMessage(msg);
//        if (testState == TestState.WAIT_RESULT){
//            String formatTime ;
//            if (time<60*60*1000){
//                formatTime = DateUtil.formatTime1(time, "mm:ss.S");
//            }else {
//                formatTime = DateUtil.formatTime1(time, "HH:mm:ss");
//            }
//            sportPresent.showLedString(formatTime);
//        }
        EventBus.getDefault().post(new BaseEvent(time, RUN_UPDATE_TEXT));
//        tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(time, false));
        onTimeIOTaskUpdate(time);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == RUN_UPDATE_TEXT) {
            tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay((Integer) baseEvent.getData(), false));
        }

    }


    public void onTimeIOTaskUpdate(int time) {
        if (testState == TestState.WAIT_RESULT) {
            String formatTime;
            if (time < 60 * 60 * 1000) {
                formatTime = DateUtil.formatTime1(time, "mm:ss.S");
            } else {
                formatTime = DateUtil.formatTime1(time, "HH:mm:ss");
            }
            sportPresent.showLedString(formatTime);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sportPresent.presentRelease();
        timerKeeper.release();
        service.shutdown();
        EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_RESULT));
    }

    @Override
    public void finish() {
        if (testing) {
            toastSpeak("测试中,不允许退出当前界面");
            return;
        }
        super.finish();
    }
}
