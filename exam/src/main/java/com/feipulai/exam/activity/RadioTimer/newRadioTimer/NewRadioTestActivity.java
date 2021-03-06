package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

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
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.sport_timer.SportContract;
import com.feipulai.exam.activity.sport_timer.SportPresent;
import com.feipulai.exam.activity.sport_timer.SportTimerActivity;
import com.feipulai.exam.activity.sport_timer.TestState;
import com.feipulai.exam.adapter.PopAdapter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.utils.ResultDisplayUtils;
import com.feipulai.exam.view.CommonPopupWindow;
import com.feipulai.exam.view.ResultPopWindow;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_RESULT;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_START;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_STOP;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_ADD_TIME;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_DEVICE;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_TEXT;
import static com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair.RadioConstant.RUN_UPDATE_VIEW_VISIBLE;

public class NewRadioTestActivity extends BaseTitleActivity implements SportContract.SportView, TimerTask2.TimeUpdateListener {

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
    private int baseTimer;//??????????????? ??????????????????????????????????????????
    private int maxTestTimes;
    private int currentTestTime;
    private SoundPlayUtils playUtils;
    private SportPresent sportPresent;
    private TestState testState;

    private ResultPopWindow resultPopWindow;
    private List<String> marks = new ArrayList<>();
    private String startTime;
    //?????????????????????
    private int select;
    private int runNum;
    private int[] independent;
    private SparseArray<Integer> array;//???????????????????????????????????? 0?????? 1?????? 2??????
    private TimerTask2 timerTask;
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
        maxTestTimes = runTimerSetting.getTestTimes();
        playUtils = SoundPlayUtils.init(this);


        testState = TestState.UN_STARTED;
        setView(new boolean[]{true, false, false, false, false, true});

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

        adapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, int pos) {

                alertConfirm(pos);
                return true;
            }
        });

        myRunnable = new MyRunnable();
        new Thread(myRunnable).start();
    }

    private MyRunnable myRunnable;

    private class MyRunnable implements Runnable {

        @Override
        public void run() {
            //???????????? = (????????????+1)*2 ?????? (????????????+1)
            if (runTimerSetting.getInterceptPoint() == 3) {
                sportPresent = new SportPresent(NewRadioTestActivity.this, runNum * 2);
            } else {
                sportPresent = new SportPresent(NewRadioTestActivity.this, runNum);
            }
            sportPresent.rollConnect();
            sportPresent.setContinueRoll(true);
            sportPresent.showReadyLed(mList);
            timerTask = new TimerTask2(NewRadioTestActivity.this, 100);
            timerTask.keepTime();
        }
    }

    private void alertConfirm(final int pos) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText(getString(R.string.clear_dialog_title))
                .setContentText(pos == -1 ? "???????????????????" : "??????????????????????")
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
        if (sportPresent != null) {
            sportPresent.setContinueRoll(true);
        }
    }

    /**
     * ?????????????????????0
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


    /**
     * ???????????? ??????0?????????1???????????????2?????????3???????????????4???????????????5???
     *
     * @param enable
     */
    private void setView(boolean[] enable) {
        tvWaitStart.setEnabled(enable[0]);
        tvWaitReady.setEnabled(enable[1]);
        tvFaultBack.setEnabled(enable[2]);
        tvForceStart.setEnabled(enable[3]);
        tvMarkConfirm.setEnabled(enable[4]);
        tvDetail.setEnabled(enable[5]);
    }

    @Override
    public void updateDeviceState(int deviceId, int state) {
        if (runTimerSetting.getInterceptPoint() != 3) {
            if (deviceId > runNum)
                return;
            if (mList.size() == 0)
                return;
            if (mList.get(deviceId - 1).getConnectState() != state) {
//                if (state == 2) {//????????????
//                    mList.get(deviceId - 1).setConnectState(2);
//                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                }else if (testState == TestState.UN_STARTED){
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
//                if ( state == 2) {//????????????
//                    mList.get(deviceId - 1).setConnectState(2);
//                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                }else if (testState == TestState.UN_STARTED){
//                    mList.get(deviceId - 1).setConnectState(state);
//                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                }
                mList.get(deviceId - 1).setConnectState(state);
                mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
            } else {
                if (array.get(deviceId - runNum - 1) != state) {
//                    if ( state == 2) {//????????????
//                        array.put(deviceId - runNum - 1, 2);
//                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                    }else if (testState == TestState.UN_STARTED) {
//                        array.put(deviceId - runNum - 1, state);
//                        mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
//                    }
                    array.put(deviceId - runNum - 1, state);
                    mHandler.sendEmptyMessage(RUN_UPDATE_DEVICE);
                }
            }
        }
        //???????????????????????????
        if (isDeviceReady() && !sportPresent.isPause() && testState == TestState.DATA_DEALING) {
            sportPresent.stopRun();

        }


    }

    /**
     * ???????????????????????????????????????
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

    /**
     * ??????????????????
     */
    @Override
    public void getDeviceStart() {
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        sportPresent.setRunState(1);
//        mHandler.sendEmptyMessage(RUN_UPDATE_VIEW_VISIBLE);
    }

    private void setBeginTime() {
        sportPresent.setForceStart();
        sportPresent.setShowReady(false);
        sportPresent.setRunState(1);
        baseTimer = sportPresent.getTime();
//        if (sportPresent.getSynKeep()>0){
//            baseTimer = sportPresent.getSynKeep();
//        }else {
//            baseTimer = sportPresent.getTime();
//            sportPresent.setSynKeep(baseTimer);
//        }
        LogUtils.all("????????????????????????baseTimer???" + baseTimer);
        testState = TestState.WAIT_RESULT;
        mHandler.sendEmptyMessage(RUN_START);
        currentTestTime++;
        startTime = System.currentTimeMillis() + "";
        sportPresent.clearLed(1);
        timerTask.setStart();
    }

    @Override
    public void receiveResult(SportResult result) {
        //??????????????? ????????????
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

            if (result.getDeviceId() <= runNum && independent[result.getDeviceId() - 1] == 0) {//??????????????????????????????
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
            //??????????????????????????????
            if (testState == TestState.DATA_DEALING) {
//                setBeginTime();
                return;
            }
            //??????????????????????????????????????????????????????
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
                LogUtils.all("????????????:" + result.getLongTime() + "????????????:" + baseTimer);
                int realTime = (result.getLongTime() - baseTimer);
                setRunWayTime(temp, realTime);
            }
        }


    }

    /**
     * ????????????????????????
     *
     * @param temp     ??????
     * @param realTime ??????
     */
    private void setRunWayTime(int temp, int realTime) {
        List<RunStudent.WaitResult> list = mList.get(temp).getResultList();
        if (realTime <= mList.get(temp).getOriginalMark()) {
            return;
        }
        mList.get(temp).setMark(getFormatTime(realTime));
        mList.get(temp).setOriginalMark(realTime);
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
     * ????????????????????????????????????
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
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title);
    }

    @OnClick({R.id.tv_wait_start, R.id.tv_wait_ready, R.id.tv_fault_back, R.id.tv_force_start, R.id.tv_mark_confirm, R.id.tv_device_detail})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_wait_start:
                timerTask.stopKeepTime();
                sportPresent.setRunLed(false);
                testing = true;
                LogUtils.operation("?????????????????????????????????");
                boolean flag = false;//?????????????????????????????????
                for (RunStudent runStudent : mList) {
                    if (null != runStudent.getStudent()) {
                        List<RoundResult> resultList = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                        flag = false;
                        if (resultList.size() >= maxTestTimes) {//??????
                            Logger.i(runStudent.getStudent().getId() + "???????????????????????????");
                            runStudent.setStudent(null);
                            flag = true;
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
                if (flag) {
                    ToastUtils.showShort("???????????????????????????");
                    return;
                }
                if (currentTestTime >= maxTestTimes) {
                    ToastUtils.showShort("?????????????????????");
                    return;
                }
                for (RunStudent runStudent : mList) {
                    if (null == runStudent.getStudent()) {
                        continue;
                    }
                    runStudent.setMark("");
                    runStudent.setOriginalMark(0);
                    runStudent.getResultList().clear();
                }
                adapter.notifyDataSetChanged();
                setView(new boolean[]{false, true, true, true, false, false});
                if (tvWaitStart.getVisibility() == View.VISIBLE) {
                    playUtils.play(13);//??????????????????
                }
                testState = TestState.DATA_DEALING;
                tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(0, false));
                tvRunState.setText("??????");
//                if (runTimerSetting.getInterceptWay() == 0 && runTimerSetting.getInterceptPoint() != 2) {//????????????&&???????????????????????????
//                    testState = TestState.DATA_DEALING;
//                    sportPresent.waitStart();
//                }
                sportPresent.waitLed();
//                sportPresent.showReadyLed(mList);
                sportPresent.waitStart();

                break;
            case R.id.tv_wait_ready:
                LogUtils.operation("???????????????????????????");
                playUtils.play(14);
                sportPresent.readyLed();
                setView(new boolean[]{false, false, true, true, false, false});
                break;
            case R.id.tv_fault_back:
                testing = false;
                timerTask.stopKeepTime();
                setView(new boolean[]{true, false, false, false, false, true});
                for (RunStudent runStudent : mList) {
                    if (runStudent == null) {
                        continue;
                    }
                    runStudent.setMark("");
                    runStudent.setOriginalMark(0);
                    runStudent.getResultList().clear();
                    runStudent.setConnectState(0);
                }
                adapter.notifyDataSetChanged();
                testState = TestState.UN_STARTED;
                currentTestTime--;
                setIndependent();
                sportPresent.showReadyLed(mList);
                sportPresent.setDeviceStateStop();
                break;
            case R.id.tv_force_start:
                if (!isDeviceReady()) {
                    sportPresent.stopRun();
                    alertConfirm();
                    return;
                }
                if (testState == TestState.UN_STARTED || testState == TestState.DATA_DEALING) {
                    LogUtils.operation("???????????????????????????");
                    testState = TestState.FORCE_START;
                    playUtils.play(15);
                    setBeginTime();

                }
                break;
            case R.id.tv_mark_confirm:
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
            LogUtils.operation("?????????????????????????????????");
            testState = TestState.UN_STARTED;
            timerTask.stopKeepTime();
            sportPresent.setDeviceStateStop();
            sportPresent.setShowLed(mList);
            for (RunStudent runStudent : mList) {
                if (runStudent.getStudent() != null) {
                    List<RoundResult> results = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                    sportPresent.saveResultRadio(runStudent.getStudent(), runStudent.getOriginalMark(), results.size() + 1, 1, startTime);
                    //????????????????????????list.add(getFormatTime(runStudent.getOriginalMark())) ,??????????????????????????????
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
            toastSpeak("??????????????????????????????????????????");
            EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_COUNT));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sportPresent.setShowReady(false);
                    sportPresent.setRunLed(false);
                    finish();
                }
            }, 3000);

        }
    }

    private void alertConfirm() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("???????????????????????????")
                .setContentText("?????????????")
                .setConfirmText(getString(com.feipulai.common.R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                testState = TestState.FORCE_START;
                playUtils.play(15);

                setBeginTime();

            }
        }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismissWithAnimation();
                sportPresent.setPause(true);
            }
        }).show();
    }


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case RUN_START:
                    setView(new boolean[]{false, false, true, false, true, false});
                    tvRunState.setText("??????");
                    break;
                case RUN_STOP:
                    setView(new boolean[]{true, false, false, false, false, true});
                    tvRunState.setText("??????");
                    break;
                case RUN_RESULT:
                case RUN_UPDATE_DEVICE:
                    adapter.notifyDataSetChanged();
                    break;
                case RUN_UPDATE_ADD_TIME:
                    addTime();
                    break;
                case RUN_UPDATE_TEXT:
                    tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(msg.arg1, false));
                    break;
                case RUN_UPDATE_VIEW_VISIBLE:
                    break;
            }
            return false;
        }
    });

    //??????????????????
    private void addTime() {
        if (testState == TestState.WAIT_RESULT) {
            mHandler.sendEmptyMessageDelayed(RUN_UPDATE_ADD_TIME, 100);
            for (int i = 0; i < mList.size(); i++) {
                RunStudent runStudent = mList.get(i);
                if (null != runStudent && independent[i] > 0) {
                    runStudent.setIndependentTime(sportPresent.getTime()-runStudent.getIndependentTime());
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
    }

    @Override
    public void finish() {
        if (testing) {
            toastSpeak("?????????,???????????????????????????");
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
        tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay(time, false));
//        EventBus.getDefault().post(new BaseEvent(time,RUN_UPDATE_TEXT));
//        onTimeIOTaskUpdate(time);
    }

    @Override
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
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == RUN_UPDATE_TEXT) {
            tvTimer.setText(ResultDisplayUtils.getStrResultForDisplay((Integer) baseEvent.getData(), false));
        }

    }
}
