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
import com.feipulai.common.utils.SoundPlayUtils;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.serial.beans.RunTimerResult;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
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
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.utils.LogUtils;

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
    @BindView(R.id.tv_wait_ready)
    TextView tvWaitReady;
    @BindView(R.id.tv_get_time)
    TextView tvGetTime;
    private int currentTestTime = 0;
    private List<RunStudent> mList = new ArrayList<>();//?????????
    private RunNumberAdapter2 mAdapter;
    /**
     * ???????????????????????????????????????
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

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_group_run_timer2;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.life("RunTimerActivityGroupActivity onCreate");
        ButterKnife.bind(this);
        initView();
        playUtils = SoundPlayUtils.init(this);
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
//        if (group != null)
//            LogUtils.operation("?????????????????????????????????:"+group.toString());
        if (pairs != null)
            LogUtils.operation("?????????????????????????????????:"+pairs.toString());
        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

//                deleteDialog(position);
                showPop(position, view);
            }
        });
        // ?????? ?????? ?????? ?????? ??????
        changeState(new boolean[]{true, false, false, false, false});

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
     * ????????????
     *
     * @param position
     */
    private void deleteDialog(final int position) {
        new AlertDialog.Builder(this).setMessage("????????????????????????????")
                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mList.get(position) != null) {

                            mList.get(position).setStudent(null);
                            mAdapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("??????", null).show();
    }

    /**
     * ??????????????????????????? ?????????????????????
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
        // ????????????????????????
        addToRunWay();
        mAdapter.notifyDataSetChanged();

    }

    @OnClick({
            R.id.tv_wait_start, R.id.tv_force_start, R.id.tv_fault_back, R.id.tv_mark_confirm,R.id.tv_wait_ready
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
//            case R.id.txt_group_name:
//                groupPop.showPopOrDismiss(view);
//                break;
//            case R.id.btn_start:
//                if (mList.get(0).getStudent() == null) {
//                    ToastUtils.showShort("??????????????????");
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
                LogUtils.operation("?????????????????????????????????");
                startActivity(new Intent(this, LEDSettingActivity.class));
                break;
            case R.id.tv_wait_start://????????????
                LogUtils.operation("?????????????????????????????????");
                waitStart();
                startTime = System.currentTimeMillis()+"";
                if (currentTestTime >= setTestCount()) {
                    isOverTimes = true;
                } else {
                    showReady(mList, true);
                }
                for (RunStudent runStudent : mList) {
                    runStudent.setMark("");
                    runStudent.getResultList().clear();
                }
                mAdapter.notifyDataSetChanged();
                playUtils.play(13);//??????????????????
                break;
            case R.id.tv_force_start://????????????
                LogUtils.operation("?????????????????????????????????");
                playUtils.play(15);//????????????
                forceStart();
                break;
            case R.id.tv_wait_ready://??????
                LogUtils.operation("???????????????????????????");
                playUtils.play(14);
                changeState(new boolean[]{false, true, false, false,false});
                break;
            case R.id.tv_fault_back://????????????
                LogUtils.operation("?????????????????????????????????");
                faultBack();
                break;
            case R.id.tv_mark_confirm://????????????
                LogUtils.operation("?????????????????????????????????");
                markConfirm();
                group.setIsTestComplete(2);
                confirmResult();
                if (runTimerSetting.isTestModel()) {//????????????
                    continueRun();

                } else {//????????????
                    for (int i = 0; i < runNum; i++) {
                        if (mList.size()<i+1)
                            return;
                        mList.get(i).setMark("");
                        mList.get(i).setStudent(null);
                    }
                    cycleRun();
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.tv_get_time:
                getTime();
                LogUtils.operation("?????????????????????????????????");
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

    public int setTestCount() {
//        SystemSetting setting = SettingHelper.getSystemSetting();
//        StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),pairs.get(select).getStudent().getStudentCode());
//        if (setting.isResit() || studentItem.getMakeUpType() == 1){
//            return pairs.get(select).getTestNo();
//        }
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            return TestConfigs.sCurrentItem.getTestNum();
        } else {
            return TestConfigs.getMaxTestCount();
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
     * ?????????
     */
    private void continueRun() {
        currentTestTime++;
        for (int i = 0; i < runNum; i++) {
            if (mList.size()< i+1)
                return;
            mList.get(i).setMark("");
        }

        if (currentTestTime < setTestCount()) {//?????????????????????????????????
//            for (int i = 0; i < runNum; i++) {
//                mList.get(i).setMark("");
//            }
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
     * ?????????
     */
    private void cycleRun() {
        boolean isAdd = true;
        if (tempGroup.size() <= 0) {
            currentTestTime++;
            if (currentTestTime < setTestCount()) {
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
     * ?????????????????????
     */
    private void addToRunWay() {
        for (int i = 0; i < runNum; i++) {
            if (mList.size()< i+1)
                return;
            if (tempGroup.size() > i) {
                int size = DBManager.getInstance().
                        queryGroupRound(tempGroup.get(i).getStudent().getStudentCode(), group.getId() + "").size();
                SystemSetting setting = SettingHelper.getSystemSetting();
                StudentItem studentItem = DBManager.getInstance().queryStudentItemByCode(TestConfigs.getCurrentItemCode(),pairs.get(select).getStudent().getStudentCode());
                //???????????????????????????????????????????????????????????????,???????????????????????????
                if (studentItem != null && (setting.isResit() || studentItem.getMakeUpType() == 1) && !pairs.get(select).isResit()){
                    size = 0;
                }
                if (size < setTestCount()) {
                    mList.get(i).setStudent(tempGroup.get(i).getStudent());
                    LogUtils.operation("?????????????????????????????????:"+tempGroup.get(i).getStudent().toString());
                } else {
                    List<RoundResult> roundResults = DBManager.getInstance().
                            queryGroupRound(tempGroup.get(i).getStudent().getStudentCode(), group.getId() + "");
                    List<String> list = new ArrayList<>();
                    for (RoundResult result : roundResults) {
                        list.add(getFormatTime(result.getResult()));
                    }
                    disposeManager.printResult(tempGroup.get(i).getStudent(), list, setTestCount(), setTestCount(), group.getGroupNo());
                    list.clear();
                    mList.get(i).setStudent(null);
                }
            } else {
                mList.get(i).setStudent(null);
            }
        }

        //???????????????????????????
        for (int i = 0; i < runNum; i++) {
            if (tempGroup.size() > 0) {
                tempGroup.remove(0);
            } else {
                break;
            }
        }

    }

    /**
     * ????????????
     */
    private void confirmResult() {
        List<BaseStuPair> stuPairs = (List<BaseStuPair>) TestConfigs.baseGroupMap.get("basePairStu");
        for (RunStudent runStudent : mList) {
            if (runStudent.getStudent() != null && !TextUtils.isEmpty(runStudent.getMark())) {
                if (runStudent.getRoundNo() != 0){
                    disposeManager.saveGroupResult(runStudent.getStudent(), runStudent.getOriginalMark(), runStudent.getRoundNo(), group,startTime);
                    runStudent.setRoundNo(0);
                    if (stuPairs != null){
                        for (BaseStuPair pp : stuPairs){
                            if (pp.getStudent().getStudentCode().equals(runStudent.getStudent().getStudentCode()))
                                pp.setRoundNo(0);
                        }
                    }
                }else {
                    disposeManager.saveGroupResult(runStudent.getStudent(), runStudent.getOriginalMark(), currentTestTime + 1, group,startTime);
                }
                List<RoundResult> resultList = DBManager.getInstance().queryGroupRound(runStudent.getStudent().getStudentCode(), group.getId() + "");
                List<String> list = new ArrayList<>();
                for (RoundResult result : resultList) {
                    list.add(getFormatTime(result.getResult()));
                }
                disposeManager.printResult(runStudent.getStudent(), list, currentTestTime + 1, setTestCount(), group.getGroupNo());
                list.clear();
//                disposeManager.broadResult(runStudent.getStudent(), getFormatTime(runStudent.getOriginalMark()));
            }
        }
        disposeManager.setShowLed(mList);
    }

    /**
     * LED?????????????????????
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
        if (studentItem!=null){
            List<RoundResult> roundResult = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());
            if (roundResult != null) {
                return;
            }
        }
        addStudent(student);


    }

    private void addStudent(Student student) {
        if (testState != 2 && testState != 3 && testState != 4) {
            currentTestTime = 0;
            if (groupRunList.size() == 0) {
                ToastUtils.showShort("??????????????????");

                //????????????????????????????????????
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
                if (!flag) {//??????????????????
                    toastSpeak(student.getStudentName() + "????????????????????????????????????????????????");
                } else {
                    //TODO ????????????
                }
            }

            mAdapter.notifyItemChanged(0);
        } else {
            ToastUtils.showShort("?????????????????????");
        }
    }

//    private void selectTestDialog(final Student student) {
//        new AlertDialog.Builder(this).setMessage(student.getStudentName() + "??????????????????????????????????????????????????????")
//                .setPositiveButton("??????", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        //????????????????????????
//                        DBManager.getInstance().deleteStuResult(student.getStudentCode());
//                        addStudent(student);
//                        dialog.dismiss();
//                    }
//                }).setNegativeButton("??????", null).show();
//    }


    @Override
    public void updateText(final String time) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTimer.setText(time);
            }
        });

    }

    @Override
    public void updateTableUI(RunTimerResult result) {
        if (result.getTrackNum() == 0)
            return;
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
            Logger.i("runTimer:" + mList.get(result.getTrackNum() - 1).getStudent().getStudentName() + "????????????:" + currentTestTime);
        }

    }

    @Override
    public void updateConnect(HashMap<String, Integer> map) {

        for (int i = 0; i < runNum; i++) {
            if (mList.size() <i+1)
                return;
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

                tvWaitReady.setEnabled(state[4]);
                tvWaitReady.setSelected(state[4]);
                tvRunState.setText(state[0] ? "??????" : state[1] ? "??????" : "??????");

                tvGetTime.setEnabled(state[3]);//????????????
                tvGetTime.setSelected(state[3]);

            }
        });

    }

//    @Override
//    public void setVisible() {
//
//    }

    /**
     * ?????????????????????
     */
    private void allTestComplete() {
        //????????????????????????
        toastSpeak("???????????????????????????????????????????????????");
        group.setIsTestComplete(1);
        DBManager.getInstance().updateGroup(group);
        mList.clear();
        tempGroup.clear();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title;
        if (TextUtils.isEmpty(SettingHelper.getSystemSetting().getTestName())) {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????";
        } else {
            title = TestConfigs.machineNameMap.get(machineCode) + SettingHelper.getSystemSetting().getHostId() + "??????-" + SettingHelper.getSystemSetting().getTestName();
        }

        return builder.setTitle(title) ;
//                .addRightText("????????????", new View.OnClickListener() {
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
    @Override
    public void setRoundNo(Student student, int roundNo) {
        for (BaseStuPair pair : pairs){
            Student student1 = pair.getStudent();
            if (student1 != null && student1.getStudentCode().equals(student.getStudentCode())){
                pair.setRoundNo(roundNo);
            }
        }
    }
}
