package com.feipulai.exam.activity.RadioTimer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.PopAdapter;
import com.feipulai.exam.adapter.RunNumberAdapter;
import com.feipulai.exam.adapter.RunNumberAdapter2;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.CommonPopupWindow;
import com.feipulai.exam.view.ResultPopWindow;
import com.feipulai.exam.view.StuSearchEditText;
import com.feipulai.exam.view.StuSearchEditText2;
import com.feipulai.exam.view.StudentPopWindow;
import com.feipulai.exam.view.baseToolbar.BaseToolbar;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RunTimerActivityTestActivity extends BaseRunTimerActivity {

    @BindView(R.id.et_input_text)
    StuSearchEditText etInputText;
    @BindView(R.id.rv_timer2)
    RecyclerView rvTimer2;
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
    @BindView(R.id.btn_start)
    TextView btnStart;
    @BindView(R.id.btn_led)
    TextView btnLed;
    @BindView(R.id.ll_first)
    LinearLayout llFirst;
    @BindView(R.id.rl_second)
    RelativeLayout rlSecond;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.tv_run_state)
    TextView tvRunState;
    private int testNo;
    private List<RunStudent> mList = new ArrayList<>();
    private RunNumberAdapter2 mAdapter2;
    private RunNumberAdapter mAdapter;
    private ResultPopWindow resultPopWindow ;
//    private ListView lvResults;
    @BindView(R.id.lv_results)
    ListView lvResults;
//    private StudentPopWindow studentPopWindow ;
    private List<String> marks = new ArrayList<>();
    //更换成绩的序号
    private int select ;
    //当前测试次数
    private int currentTestTime = 0;
    private boolean isSetting = true;
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
    }

    private void initView() {
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

        mAdapter = new RunNumberAdapter(mList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTimer.setLayoutManager(layoutManager);
        rvTimer.setAdapter(mAdapter);


        changeState(new boolean[]{true, false, false, false});
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

                deleteDialog(position);

            }
        });

        mAdapter2.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                showPop(position,view);
            }
        });
        btnStart.setSelected(true);
        btnLed.setSelected(true);
        PopAdapter popAdapter = new PopAdapter(marks);
        resultPopWindow = new ResultPopWindow(this,popAdapter);
        resultPopWindow.setOnPopItemClickListener(new CommonPopupWindow.OnPopItemClickListener() {
            @Override
            public void itemClick(int position) {
                String result = marks.get(position);
                mList.get(select).setMark(result);
                mList.get(select).setOriginalMark(mList.get(select).getResultList().get(position).getOriResult());
                mAdapter2.notifyDataSetChanged();
            }
        });

//        studentPopWindow = new StudentPopWindow(this);
//        lvResults = studentPopWindow.getLvResults();
        etInputText.setData(lvResults, this);
//        etInputText.setShowListListener(new StuSearchEditText2.ShowListListener() {
//            @Override
//            public void onShowListener(boolean isShow) {
//                if (isShow){
//                    studentPopWindow.showPop(etInputText);
//                    etInputText.setFocusable(true);
//                    etInputText.setFocusableInTouchMode(true);
//                    etInputText.requestFocus();
//                }else {
//                    studentPopWindow.dismiss();
//                }
//
//            }
//        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (reLoad){
            initView();
        }
    }

    private void showPop(int pos, View view) {
        marks.clear();
        RunStudent runStudent = mList.get(pos);
        if (runStudent.getStudent()!= null){
            List<RunStudent.WaitResult> hashMap = runStudent.getResultList();
            for(RunStudent.WaitResult entry : hashMap){
//                Log.i("key= "+entry.getKey()," and value= "+entry.getValue());
                marks.add(entry.getWaitResult());
            }

        }
        resultPopWindow.notifyPop();
        select = pos ;
        resultPopWindow.showPopOrDismiss(view);
    }
    /**
     * 删除考生
     *
     * @param position
     */
    private void deleteDialog(final int position) {
        new AlertDialog.Builder(this).setMessage("是否确定删除此考生?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mList.get(position) != null) {
                            mList.get(position).setStudent(null);
                            mAdapter.notifyDataSetChanged();
                            mAdapter2.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
    }

    @OnClick({R.id.btn_start, R.id.btn_led, R.id.tv_wait_start, R.id.tv_force_start,
            R.id.tv_fault_back, R.id.tv_mark_confirm}) //R.id.tv_project_setting,
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                if (mList.get(0).getStudent() == null) {
                    ToastUtils.showShort("请先添加学生");
                    return;
                }
                isSetting = false ;
                llFirst.setVisibility(View.GONE);
                rlSecond.setVisibility(View.VISIBLE);
                break;
//            case R.id.tv_project_setting:
//                startActivity(new Intent(this, RunTimerSettingActivity.class));
//                break;
            case R.id.btn_led:
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.tv_wait_start://等待发令
                waitStart();
                if (currentTestTime >= maxTestTimes) {
                    isOverTimes = true;
                }else {
                    showReady(mList,true);
                }
                for (RunStudent runStudent :mList){
                    runStudent.setMark("");
                    runStudent.getResultList().clear();
                }
                mAdapter2.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.tv_force_start://强制启动
                forceStart();
                break;
            case R.id.tv_fault_back://违规返回
                faultBack();

                break;
            case R.id.tv_mark_confirm://成绩确认
                currentTestTime++;
                markConfirm();
                for (RunStudent runStudent : mList) {
                    if (runStudent.getStudent() != null){
                        disposeManager.saveResult(runStudent.getStudent(), runStudent.getOriginalMark(), currentTestTime,testNo+1);
                        List<RoundResult> resultList = DBManager.getInstance().queryResultsByStudentCode(runStudent.getStudent().getStudentCode());
                        List<String> list = new ArrayList<>();
                        for (RoundResult result : resultList) {
                            list.add(getFormatTime(result.getResult()));
                        }

                        disposeManager.printResult(runStudent.getStudent(), list, currentTestTime, maxTestTimes,-1);
                        list.clear();
                    }
                }
                disposeManager.setShowLed(mList);

                if (currentTestTime >= maxTestTimes){//回到初始界面
                    currentTestTime = 0 ;
                    isSetting = true ;
                    llFirst.setVisibility(View.VISIBLE);
                    rlSecond.setVisibility(View.GONE);
                    mList.clear();
                    for (int i = 0; i < runNum; i++) {
                        RunStudent runStudent = new RunStudent();
                        runStudent.setResultList(new ArrayList<RunStudent.WaitResult>());
                        mList.add(runStudent);
                    }
                    mAdapter2.notifyDataSetChanged();
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void illegalBack() {
        for (RunStudent runStudent :mList){
            runStudent.setMark("");
            runStudent.getResultList().clear();
        }
        mAdapter2.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();
        showReady(mList,false);
    }


    /**
     * 显示道次准备
     */
    private void showReady(List<RunStudent> runs,boolean ready) {
        if (runs.size() < 0)
            return;
        disposeManager.showReady(runs,ready);
    }

    @Override
    public void onCheckIn(Student student) {
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        List<RoundResult> roundResultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        //保存成绩，并测试轮次大于测试轮次次数
        if (roundResultList != null && roundResultList.size() >= maxTestTimes) {
            //已测试，不重测
//            roundNo = roundResult.getRoundNo();
//            selectTestDialog(student);
            toastSpeak("该考生所有轮次已全部测试完成");
            return;
        }

        //是否有成绩，没有成绩查底该项目是否有成绩，没有成绩测试次数为1，有成绩测试次数+1
        if (roundResultList != null && roundResultList.size() == 0) {
            RoundResult testRoundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
            testNo = testRoundResult == null ? 1 : testRoundResult.getTestNo() + 1;
        } else {
            testNo = roundResultList.get(0).getTestNo();
        }
        currentTestTime = roundResultList.size();
        if (isExistStudent(student)){
            toastSpeak("该考生已存在");
            return;
        }
        addStudent(student);
        Logger.i("runTimer:" + studentItem.toString());
    }

    private void addStudent(Student student) {
        if (testState != 2 && testState != 3 && testState != 4) {
            currentTestTime = 0;
            for (int i = 0; i < runNum; i++) {
                if (mList.get(i).getStudent() == null) {
                    mList.get(i).setStudent(student);
                    break;
                }
            }
            updateStuInfo(student);
            mAdapter2.notifyDataSetChanged();
            mAdapter.notifyDataSetChanged();
            showReady(mList,false  );
        } else {

            ToastUtils.showShort("设备正在使用中");
        }
    }

    /**
     * 判断 学生是否已存在
     * @param student
     * @return
     */
    private boolean isExistStudent(Student student){
        for (RunStudent runStudent :mList){
           if (runStudent.getStudent() != null && student.getStudentCode().equals(runStudent.getStudent().getStudentCode())){
                return true ;
            }
        }
        return false ;
    }

    private void updateStuInfo(Student student) {
        txtStuCode.setText(student.getStudentCode());
        txtStuName.setText(student.getStudentName());
        txtStuSex.setText(student.getSex() == 0 ?"男":"女");
    }

//    private void selectTestDialog(final Student student) {
//        new AlertDialog.Builder(this).setMessage(student.getStudentName() + "考生已测试过本项目，是否进行再次测试")
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //重测清除已有成绩
//                        DBManager.getInstance().deleteStuResult(student.getStudentCode());
//                        addStudent(student);
//                        dialog.dismiss();
//                    }
//                }).setNegativeButton("取消", null).show();
//    }

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
        mAdapter.notifyDataSetChanged();
        if ((mList.get(result.getTrackNum() - 1).getStudent() != null)) {
//            disposeManager.saveResult(mList.get(result.getTrackNum() - 1).getStudent(), result.getResult(), currentTestTime, testNo);
            Logger.i("runTimer:" + mList.get(result.getTrackNum() - 1).getStudent().getStudentName() + "测试次数:" + currentTestTime);
        }

//        disposeManager.setShowLed(mList);
    }

    @Override
    public void updateConnect(HashMap<String, Integer> map) {
        for (int i = 0; i < runNum; i++) {
            if (mList.get(i) != null) {
                mList.get(i).setConnectState(map.get(("runNum" + i)));
            }
        }
        mAdapter2.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();
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
                tvRunState.setText(state[0]? "空闲":state[1]?"等待":"计时");
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

        return builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llFirst.getVisibility() == View.VISIBLE){
                    finish();
                }else {
                    llFirst.setVisibility(View.VISIBLE);
                    rlSecond.setVisibility(View.GONE);
                    stopRun();
                }

            }
        }).addRightText("项目设置", new View.OnClickListener() {
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