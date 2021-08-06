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
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.fragment.IndividualCheckFragment;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.ScoreAdapter;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.utils.Toast;

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
    @BindView(R.id.et_password)
    EditText et_password;
    @BindView(R.id.tv_message)
    TextView tv_message;
    private onClickQuitListener listener;
    private ScoreAdapter adapter;
    private int selectPos;
    private SystemSetting systemSetting;

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
        systemSetting = SettingHelper.getSystemSetting();
        rv_score.setLayoutManager(new GridLayoutManager(getContext(),3));
        adapter = new ScoreAdapter(results);
        if (results != null)
            rv_score.setAdapter(adapter);
        if (systemSetting.getResitPassBool()){
            et_password.setVisibility(View.VISIBLE);
            tv_message.setText("该考生已有成绩,请输入验证密码进行补考?");
        }else {
            et_password.setVisibility(View.GONE);
            tv_message.setText("该考生已有成绩,是否进行补考?");
        }

    }

    @OnClick({R.id.tv_cancel,R.id.tv_commit})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.tv_cancel:
                listener.onCancel();
                dismiss();
                break;
            case R.id.tv_commit:
                if (systemSetting.getResitPassBool()){
                    String pass = et_password.getText().toString().trim();
                    if (pass.equals(systemSetting.getResitPass())){
                        studentItem.setExamType(2);
                        DBManager.getInstance().updateStudentItem(studentItem);
                        listener.onCommit(student, studentItem, results,0);
                        dismiss();
                    }else {
                        Toast.makeText(getContext(),"密码错误",Toast.LENGTH_LONG).show();
                    }
                }else {
                    studentItem.setExamType(2);
                    DBManager.getInstance().updateStudentItem(studentItem);
                    listener.onCommit(student, studentItem, results,0);
                    dismiss();
                }

                break;
        }
    }



    public interface onClickQuitListener{
        void onCancel();
        void onCommit(Student student,StudentItem studentItem,List<RoundResult> results,int roundNo);
    }
}
