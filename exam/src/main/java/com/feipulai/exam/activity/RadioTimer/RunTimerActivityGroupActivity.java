package com.feipulai.exam.activity.RadioTimer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.PopAdapter;
import com.feipulai.exam.adapter.RunNumberAdapter2;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Group;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.RunStudent;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.CommonPopupWindow;
import com.feipulai.exam.view.ResultPopWindow;
import com.feipulai.exam.view.baseToolbar.BaseToolbar;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RunTimerActivityGroupActivity extends BaseRunTimerActivity {
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
    @BindView(R.id.tv_run_state)
    TextView tvRunState;
    private int currentTestTime = 0;
    private List<RunStudent> mList = new ArrayList<>();//测试的
    private RunNumberAdapter2 mAdapter;
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

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_run_timer2;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        mAdapter = new RunNumberAdapter2(mList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTimer.setLayoutManager(layoutManager);
        rvTimer.setAdapter(mAdapter);
        group = (Group) TestConfigs.baseGroupMap.get("group");
        pairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

//                deleteDialog(position);
                showPop(position, view);
            }
        });
        changeState(new boolean[]{true, false, false, false});

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


    }

    @Override
    protected void onResume() {
        super.onResume();
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
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
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

    @OnClick({
            R.id.tv_wait_start, R.id.tv_force_start, R.id.tv_fault_back, R.id.tv_mark_confirm
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
//            case R.id.txt_group_name:
//                groupPop.showPopOrDismiss(view);
//                break;
//            case R.id.btn_start:
//                if (mList.get(0).getStudent() == null) {
//                    ToastUtils.showShort("请先添加学生");
//                    return;
//                }
//                rlControl.setVisibility(View.VISIBLE);
//                rlBottom.setVisibility(View.GONE);
//                isVisible = false;
//                break;
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
                } else {
                    showReady(mList, true);
                }
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.getResultList().clear();
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.tv_force_start://强制启动
                forceStart();
                break;
            case R.id.tv_fault_back://违规返回
                faultBack();
                break;
            case R.id.tv_mark_confirm://成绩确认
                markConfirm();
                group.setIsTestComplete(2);
                confirmResult();
                if (runTimerSetting.isTestModel()) {//连续测试
                    continueRun();

                } else {//循环测试
                    for (int i = 0; i < runNum; i++) {
                        mList.get(i).setMark("");
                        mList.get(i).setStudent(null);
                    }
                    cycleRun();
                }
                mAdapter.notifyDataSetChanged();
                break;
//            case R.id.img_last:
//                if (groupAdapter.getTestPosition() > 0) {
//                    selectGroup(groupAdapter.getTestPosition() - 1);
//                }
//
//                break;
//            case R.id.img_next:
//                if (groupList.size() > 0 && groupAdapter.getTestPosition() != groupList.size() - 1) {
//                    selectGroup(groupAdapter.getTestPosition() + 1);
//                }
        }

    }

    @Override
    public void illegalBack() {
        for (RunStudent runStudent : mList) {
            runStudent.setMark("");
            runStudent.getResultList().clear();
        }
        showReady(mList, false);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 连续跑
     */
    private void continueRun() {
        currentTestTime++;
        if (currentTestTime < maxTestTimes) {//当前跑道学生是否测试完
            for (int i = 0; i < runNum; i++) {
                mList.get(i).setMark("");
            }
        } else {
            currentTestTime = 0;
            if (tempGroup.size() <= 0) {
                allTestComplete();
            } else {
                addToRunWay();
            }
        }
    }

    /**
     * 循环跑
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
     * 将学生加入跑道
     */
    private void addToRunWay() {
        for (int i = 0; i < runNum; i++) {
            if (tempGroup.size() > i) {
                int size = DBManager.getInstance().
                        queryGroupRound(tempGroup.get(i).getStudent().getStudentCode(), group.getId() + "").size();
                if (size < maxTestTimes) {
                    mList.get(i).setStudent(tempGroup.get(i).getStudent());
                } else {
                    List<RoundResult> roundResults = DBManager.getInstance().
                            queryGroupRound(tempGroup.get(i).getStudent().getStudentCode(), group.getId() + "");
                    List<String> list = new ArrayList<>();
                    for (RoundResult result : roundResults) {
                        list.add(getFormatTime(result.getResult()));
                    }
                    disposeManager.printResult(tempGroup.get(i).getStudent(), list, maxTestTimes, maxTestTimes, group.getGroupNo());
                    list.clear();
                    mList.get(i).setStudent(null);
                }
            } else {
                mList.get(i).setStudent(null);
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

    /**
     * 成绩确认
     */
    private void confirmResult() {
        for (RunStudent runStudent : mList) {
            if (runStudent.getStudent() != null && !TextUtils.isEmpty(runStudent.getMark())) {
                disposeManager.saveGroupResult(runStudent.getStudent(), runStudent.getOriginalMark(), currentTestTime + 1, group);
                List<RoundResult> resultList = DBManager.getInstance().queryGroupRound(runStudent.getStudent().getStudentCode(), group.getId() + "");
                List<String> list = new ArrayList<>();
                for (RoundResult result : resultList) {
                    list.add(getFormatTime(result.getResult()));
                }
                disposeManager.printResult(runStudent.getStudent(), list, currentTestTime + 1, maxTestTimes, group.getGroupNo());
                list.clear();
//                disposeManager.broadResult(runStudent.getStudent(), getFormatTime(runStudent.getOriginalMark()));
            }
        }
        disposeManager.setShowLed(mList);
    }

    /**
     * LED屏显示准备开始
     */
    private void showReady(List<RunStudent> runs, boolean ready) {
        if (runs.size() < 0)
            return;
        disposeManager.showReady(runs, ready);
    }

    @Override
    public void onCheckIn(Student student) {
//        RoundResult roundResult = DBManager.getInstance().queryFinallyRountScore(student.getStudentCode());
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(student.getStudentCode());
        List<RoundResult> roundResult = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
        if (roundResult != null) {
//            selectTestDialog(student);
            return;
        } else {
            addStudent(student);
        }

    }

    private void addStudent(Student student) {
        if (testState != 2 && testState != 3 && testState != 4) {
            currentTestTime = 0;
            if (groupRunList.size() == 0) {
                ToastUtils.showShort("请先选择分组");

                //判断所选的分组是否有考生
            } else {
                boolean flag = true;
                for (int i = 0; i < groupRunList.size(); i++) {
                    if (groupRunList.get(i).getStudent().getStudentCode().equals(student.getStudentCode())) {
                        flag = true;
                        break;
                    } else {
                        flag = false;
                    }
                }
                if (!flag) {//不在分组群中
                    toastSpeak(student.getStudentName() + "考生没有在选择的分组内，无法测试");
                } else {
                    //TODO 检验身份
                }
            }

            mAdapter.notifyItemChanged(0);
        } else {
            ToastUtils.showShort("设备正在使用中");
        }
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
//        Log.i(result.getOrder() + "key" + mList.get(result.getTrackNum() - 1).getStudent().getStudentName(), getFormatTime(result.getResult()));
        mAdapter.notifyDataSetChanged();
//        List<String> list = new ArrayList<>();
//        list.add(getFormatTime(result.getResult()));
        if ((mList.get(result.getTrackNum() - 1).getStudent() != null)) {
//            disposeManager.printResult(mList.get(result.getTrackNum() - 1).getStudent(), list, currentTestTime, maxTestTimes);
//            disposeManager.broadResult(mList.get(result.getTrackNum() - 1).getStudent(), getFormatTime(result.getResult()));
//            disposeManager.saveGroupResult(mList.get(result.getTrackNum() - 1).getStudent(), result.getResult(), currentTestTime, group);
//            disposeManager.setShowLed(mList);
            Logger.i("runTimer:" + mList.get(result.getTrackNum() - 1).getStudent().getStudentName() + "测试次数:" + currentTestTime);
        }

    }

    @Override
    public void updateConnect(HashMap<String, Integer> map) {

        for (int i = 0; i < runNum; i++) {
            if (mList.get(i) != null) {
                mList.get(i).setConnectState(map.get(("runNum" + i)));
            }
        }
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

                tvRunState.setText(state[0] ? "空闲" : state[1] ? "等待" : "计时");

            }
        });

    }

//    @Override
//    public void setVisible() {
//
//    }

    /**
     * 全部次数测试完
     */
    private void allTestComplete() {
        //全部次数测试完，
        toastSpeak("分组考生全部测试完成，请选择下一组");
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
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
                finish();
            }
        });
//                .addRightText("项目设置", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        gotoItemSetting();
//                    }
//                }).addRightImage(R.mipmap.icon_setting, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        gotoItemSetting();
//                    }
//                });
    }

    private void gotoItemSetting() {
        startActivity(new Intent(this, RunTimerSettingActivity.class));
    }
}
