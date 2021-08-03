package com.feipulai.exam.activity.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.adapter.ScoreAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;

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
    private int selectPos;

    public void setArguments(Student student, List<RoundResult> results, StudentItem studentItem) {
        this.student = student;
        this.results = results;
        this.studentItem = studentItem;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_resit,container);
        ButterKnife.bind(this,view);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_message.setText("该学生已测试完成,是否要重新测试?");
        tv_stu_info.setText(student.getStudentCode()+"      "+student.getStudentName());
        rv_score.setLayoutManager(new GridLayoutManager(getContext(),3));
        adapter = new ScoreAdapter(results);
        rv_score.setAdapter(adapter);
        adapter.setOnItemChildClickListener(this);
    }

    public void setOnIndividualCheckInListener(ResitDialog.onClickQuitListener listener) {
        this.listener = listener;
    }
    @OnClick({R.id.tv_cancel,R.id.tv_commit})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.tv_cancel:
                listener.onCancel();
                dismiss();
                break;
            case R.id.tv_commit:
                RoundResult roundResult = results.get(selectPos);
                roundResult.setIsDelete(true);
                DBManager.getInstance().updateRoundResult(roundResult);
                listener.onCommit(student, studentItem, results);
                dismiss();
                break;
        }
    }
    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        this.selectPos = position;
    }
}
