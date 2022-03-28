package com.feipulai.exam.activity.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.ScoreAdapter;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AgainTestDialog extends DialogFragment implements BaseQuickAdapter.OnItemChildClickListener {

    private Student student;
    private List<RoundResult> results;
    private StudentItem studentItem;
    private ResitDialog.onClickQuitListener listener;
    private ScoreAdapter adapter;

    @BindView(R.id.tv_message)
    TextView tv_message;
    @BindView(R.id.tv_stu_info)
    TextView tv_stu_info;
    @BindView(R.id.rv_score)
    RecyclerView rv_score;
    private int selectPos = -1;
    @BindView(R.id.et_password)
    EditText et_password;
    private SystemSetting systemSetting;
    private GroupItem groupItem;

    public void setArguments(Student student, List<RoundResult> results, StudentItem studentItem) {
        this.student = student;
        this.results = results;
        this.studentItem = studentItem;
    }

    public void setArguments(Student student, List<RoundResult> results, GroupItem groupItem) {
        this.student = student;
        this.results = results;
        this.groupItem = groupItem;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_resit, container);
        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        systemSetting = SettingHelper.getSystemSetting();
        tv_stu_info.setText(student.getStudentCode() + "      " + student.getStudentName());
        rv_score.setLayoutManager(new GridLayoutManager(getContext(), results.size()));
        adapter = new ScoreAdapter(results);
        rv_score.setAdapter(adapter);
        adapter.setOnItemChildClickListener(this);
        if (results.size() == 1) {
            selectPos = 0;
            adapter.setselPos(selectPos);
            adapter.notifyDataSetChanged();
        }
        if (systemSetting.getAgainPassBool()) {
            et_password.setVisibility(View.VISIBLE);
            tv_message.setText("该考生已有成绩,请输入验证密码重新测试?");
        } else {
            et_password.setVisibility(View.GONE);
            tv_message.setText("该学生已测试完成,是否要重新测试?");
        }
    }

    public void setOnIndividualCheckInListener(ResitDialog.onClickQuitListener listener) {
        this.listener = listener;
    }

    @OnClick({R.id.tv_cancel, R.id.tv_commit})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                listener.onCancel();
                dismiss();
                break;
            case R.id.tv_commit:
                if (selectPos == -1) {
                    Toast.makeText(getContext(), "请选择轮次成绩进行重测", Toast.LENGTH_LONG).show();
                    return;
                }
                if (systemSetting.getAgainPassBool()) {
                    String pass = et_password.getText().toString().trim();
                    if (pass.equals(systemSetting.getAgainPass())) {
                        RoundResult roundResult = results.get(selectPos);
                        roundResult.setIsDelete(true);
                        Log.e("TAG----", roundResult.getId() + "----" + roundResult.getIsDelete());
//                        if (roundResult.getIsLastResult() == RoundResult.LAST_RESULT){
//                            roundResult.setIsLastResult(RoundResult.NOT_LAST_RESULT);
//                        }
                        DBManager.getInstance().updateRoundResult(roundResult);
                        results.remove(selectPos);
                        setLastResults();
                        SystemSetting systemSetting = SettingHelper.getSystemSetting();
                        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
                            listener.onCommitPattern(student, studentItem, results, roundResult.getRoundNo());
                        } else {
                            listener.onCommitGroup(student, groupItem, results, roundResult.getRoundNo());
                        }
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "密码错误", Toast.LENGTH_LONG).show();
                    }
                } else {
                    RoundResult roundResult = results.get(selectPos);
                    roundResult.setIsDelete(true);
                    Log.e("TAG----", roundResult.getId() + "----" + roundResult.getIsDelete());
//                    if (roundResult.getIsLastResult() == RoundResult.LAST_RESULT){
//                        roundResult.setIsLastResult(RoundResult.NOT_LAST_RESULT);
//                    }
                    DBManager.getInstance().updateRoundResult(roundResult);
                    results.remove(selectPos);
                    setLastResults();
                    SystemSetting systemSetting = SettingHelper.getSystemSetting();
                    if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
                        listener.onCommitPattern(student, studentItem, results, roundResult.getRoundNo());
                    } else {
                        listener.onCommitGroup(student, groupItem, results, roundResult.getRoundNo());
                    }
                    dismiss();
                }
                break;
        }
    }

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        this.selectPos = position;
        RoundResult roundResult = results.get(selectPos);
        this.adapter.setselPos(selectPos);
        this.adapter.notifyDataSetChanged();
        Log.e("TAG", roundResult.getId() + "----" + roundResult.getResult() + "-----" + roundResult.getRoundNo());
    }

    private void setLastResults() {
        RoundResult lastRount = null;
        List<RoundResult> resultList = new ArrayList<>();
        if (systemSetting.getTestPattern() == SystemSetting.PERSON_PATTERN) {
            resultList = DBManager.getInstance().queryGroupRound(student.getStudentCode(),
                    results.get(selectPos).getGroupId() + "");
        } else {
            resultList = DBManager.getInstance().queryFinallyRountScoreByExamTypeList(student.getStudentCode(), studentItem.getExamType());

        }

        for (RoundResult result : resultList) {
            result.setIsLastResult(RoundResult.NOT_LAST_RESULT);
            if (!result.isDelete()) {
                if (lastRount == null) {
                    lastRount = result;
                    continue;
                }
                if (result.getResultState() == RoundResult.RESULT_STATE_NORMAL) {
                    if (lastRount.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                        lastRount = result;
                        continue;
                    } else if (result.getResultState() != RoundResult.RESULT_STATE_NORMAL) {
                        continue;
                    }
                    switch (TestConfigs.sCurrentItem.getTestType()) {
                        case DBManager.TEST_TYPE_TIME://项目类型 计时

                            if (lastRount.getResult() > result.getResult()) {
                                lastRount = result;
                            }
                            break;
                        case DBManager.TEST_TYPE_COUNT://项目类型 计数
                        case DBManager.TEST_TYPE_DISTANCE://项目类型 远度
                        case DBManager.TEST_TYPE_POWER://项目类型 力量
                            if (lastRount.getResult() < result.getResult()) {
                                lastRount = result;
                            }
                            break;
                    }
                }

            }

        }
        if (lastRount != null) {
            lastRount.setIsLastResult(RoundResult.LAST_RESULT);
            DBManager.getInstance().updateRoundResult(resultList);
            DBManager.getInstance().updateRoundResult(lastRount);
        }

    }
}
