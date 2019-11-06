package com.feipulai.host.activity.radio_timer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.adapter.PopAdapter;
import com.feipulai.host.adapter.RunNumberAdapter2;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.RunStudent;
import com.feipulai.host.entity.Student;
import com.feipulai.host.view.CommonPopupWindow;
import com.feipulai.host.view.ResultPopWindow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RunTimerFreeTestActivity extends BaseRunTimerActivity {

    @BindView(R.id.rv_timer2)
    RecyclerView rvTimer2;
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
    @BindView(R.id.btn_start)
    TextView btnStart;
    @BindView(R.id.btn_led)
    TextView btnLed;
    @BindView(R.id.ll_first)
    LinearLayout llFirst;
    @BindView(R.id.rl_second)
    RelativeLayout rlSecond;
    @BindView(R.id.tv_run_state)
    TextView tvRunState;
    @BindView(R.id.tv_wait_ready)
    TextView tvWaitReady;
    private List<RunStudent> mList = new ArrayList<>();
    private RunNumberAdapter2 mAdapter2;
    private ResultPopWindow resultPopWindow;
    @BindView(R.id.lv_results)
    ListView lvResults;

    private List<String> marks = new ArrayList<>();
    //更换成绩的序号
    private int select;

    private boolean isSetting = true;
    private SoundPlayUtils playUtils;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_run_timer2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_run_timer2);
        ButterKnife.bind(this);
        initView();
        playUtils = SoundPlayUtils.init(this);
    }

    private void initView() {
        llFirst.setVisibility(View.GONE);
        rlSecond.setVisibility(View.VISIBLE);
        mList.clear();
        for (int i = 0; i < runNum; i++) {
            RunStudent runStudent = new RunStudent();
            runStudent.setResultList(new ArrayList<RunStudent.WaitResult>());
            mList.add(runStudent);
        }
        mAdapter2 = new RunNumberAdapter2(mList);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.VERTICAL);
        rvTimer2.setLayoutManager(layoutManager2);
        rvTimer2.setAdapter(mAdapter2);

        // 等待 确认 违规 强起 预备
        changeState(new boolean[]{true, false, false, false, false});

        mAdapter2.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                showPop(position, view);
            }
        });
        btnStart.setSelected(true);
        btnLed.setSelected(true);
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


    }


    @Override
    protected void onResume() {
        super.onResume();
        if (reLoad) {
            initView();
        }
    }

    private void showPop(int pos, View view) {
        marks.clear();
        RunStudent runStudent = mList.get(pos);

        List<RunStudent.WaitResult> hashMap = runStudent.getResultList();
        for (RunStudent.WaitResult entry : hashMap) {
//                Log.i("key= "+entry.getKey()," and value= "+entry.getValue());
            marks.add(entry.getWaitResult());
        }

        resultPopWindow.notifyPop();
        select = pos;
        resultPopWindow.showPopOrDismiss(view);
    }


    @OnClick({R.id.btn_led, R.id.tv_wait_start, R.id.tv_force_start,
            R.id.tv_fault_back, R.id.tv_mark_confirm, R.id.tv_wait_ready})
    //R.id.tv_project_setting,
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.btn_led:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.tv_wait_start://等待发令
                waitStart();
                showReady(mList, true);
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.getResultList().clear();
                }
                mAdapter2.notifyDataSetChanged();
                playUtils.play(13);
                break;
            case R.id.tv_force_start://强制启动
                playUtils.play(15);
                forceStart();
                break;
            case R.id.tv_fault_back://违规返回
                faultBack();

                break;
            case R.id.tv_wait_ready:
                playUtils.play(14);
                changeState(new boolean[]{false, true, false, false, false});
                break;
            case R.id.tv_mark_confirm://成绩确认
                markConfirm();
                for (RunStudent runStudent : mList) {

                    BaseStuPair baseStuPair = new BaseStuPair();
                    baseStuPair.setStudent(runStudent.getStudent());
                    baseStuPair.setResult(runStudent.getOriginalMark());
                    baseStuPair.setResultState(RoundResult.RESULT_STATE_NORMAL);
                    if (SettingHelper.getSystemSetting().isAutoPrint()) {
                        disposeManager.printResult(runStudent.getStudent(), runStudent.getOriginalMark());
                    }


                }
                disposeManager.setShowLed(mList);


                break;
        }
    }


    @Override
    public void illegalBack() {
        for (RunStudent runStudent : mList) {
            runStudent.setMark("");
            runStudent.getResultList().clear();
        }
        mAdapter2.notifyDataSetChanged();
        showReady(mList, false);
    }


    /**
     * 显示道次准备
     */
    private void showReady(List<RunStudent> runs, boolean ready) {
        if (runs.size() < 0)
            return;
        disposeManager.showReady(runs, ready);
    }

    @Override
    public void onCheckIn(Student student) {

    }

    @Override
    public void updateText(String time) {
        tvTimer.setText(time);
    }

    @Override
    public void updateTableUI(RunTimerResult result) {

        int realTime = (int) (result.getResult() - baseTimer);
        mList.get(result.getTrackNum() - 1).setMark(getFormatTime(realTime));
        mList.get(result.getTrackNum() - 1).setOriginalMark(realTime);
        List<RunStudent.WaitResult> list = mList.get(result.getTrackNum() - 1).getResultList();
        RunStudent.WaitResult waitResult = new RunStudent.WaitResult();
        waitResult.setOriResult(realTime);
        waitResult.setWaitResult(getFormatTime(realTime));
        list.add(waitResult);
        mAdapter2.notifyDataSetChanged();

    }

    @Override
    public void updateConnect(HashMap<String, Integer> map) {
        for (int i = 0; i < runNum; i++) {
            if (mList.get(i) != null) {
                mList.get(i).setConnectState(map.get(("runNum" + i)));
            }
        }
        mAdapter2.notifyDataSetChanged();
    }

    @Override
    public void changeState(final boolean[] state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvWaitStart.setEnabled(state[0]);
                tvWaitStart.setSelected(state[0]);

                tvForceStart.setEnabled(state[1]);
                tvForceStart.setSelected(state[1]);

                tvFaultBack.setEnabled(state[2]);
                tvFaultBack.setSelected(state[2]);

                tvMarkConfirm.setEnabled(state[3]);
                tvMarkConfirm.setSelected(state[3]);

                tvWaitReady.setEnabled(state[4]);
                tvWaitReady.setSelected(state[4]);

                tvRunState.setText(state[0] ? "空闲" : state[1] ? "等待" : "计时");
            }
        });

    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "号机-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title).addRightText("项目设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoItemSetting();
            }
        });
    }

    private void gotoItemSetting() {
        if (isSetting) {
            startActivity(new Intent(this, RunTimerSettingActivity.class));
        }

    }


}