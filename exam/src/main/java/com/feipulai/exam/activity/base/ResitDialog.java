package com.feipulai.exam.activity.base;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class ResitDialog extends DialogFragment{
    private Student student;
    private List<RoundResult> results;
    private StudentItem studentItem;
    @BindView(R.id.tv_stu_info)
    TextView tv_stu_info;
    @BindView(R.id.rv_score)
    RecyclerView rv_score;
    private onClickQuitListener listener;
    private ScoreAdapter adapter;
    private int selectPos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_resit,container);
        ButterKnife.bind(this,view);
        return view;
    }

    public void setArguments(Student student, List<RoundResult> results, StudentItem studentItem) {
        this.student = student;
        this.results = results;
        this.studentItem = studentItem;
    }
    public void setOnIndividualCheckInListener(onClickQuitListener listener){
        this.listener = listener;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tv_stu_info.setText(student.getStudentCode()+"      "+student.getStudentName());

        rv_score.setLayoutManager(new GridLayoutManager(getContext(),3));
        adapter = new ScoreAdapter(results);
        if (results != null)
            rv_score.setAdapter(adapter);


    }

    @OnClick({R.id.tv_cancel,R.id.tv_commit})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.tv_cancel:
                listener.onCancel();
                dismiss();
                break;
            case R.id.tv_commit:
                listener.onCommit(student, studentItem, results);
                dismiss();
                break;
        }
    }



    public interface onClickQuitListener{
        void onCancel();
        void onCommit(Student student,StudentItem studentItem,List<RoundResult> results);
    }
}
