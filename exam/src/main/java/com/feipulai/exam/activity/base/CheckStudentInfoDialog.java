package com.feipulai.exam.activity.base;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.entity.GroupItem;
import com.feipulai.exam.entity.Student;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckStudentInfoDialog {
    private Dialog dialog;
    private Window window = null;
    private Student student;

    @BindView(R.id.tv_studentCode)
    TextView tv_studentCode;
    @BindView(R.id.tv_studentName)
    TextView tv_studentName;
    @BindView(R.id.tv_studentTrank)
    TextView tv_studentTrank;
    @BindView(R.id.iv_portrait)
    ImageView iv_portrait;
    private GroupItem groupItem;

    public void setOnCheckInListener(CheckStudentInfoDialog.onCheckInListener onCheckInListener) {
        this.onCheckInListener = onCheckInListener;
    }

    private onCheckInListener onCheckInListener;

    public CheckStudentInfoDialog(Context context){
        init(context);
    }
    protected void init(Context context) {
        dialog = new Dialog(context, R.style.dialog_style);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_check_student_info, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);

        WindowManager.LayoutParams params = window.getAttributes();
        params.height = 350;
        window.setAttributes(params);
    }

    public void show() {
        tv_studentCode.setText(groupItem.getStudentCode());
        tv_studentName.setText(student.getStudentName());
        tv_studentTrank.setText(groupItem.getTrackNo()+"");
        Glide.with(iv_portrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(iv_portrait);
        dialog.show();
    }
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    public void setGroupItem(GroupItem groupItem) {
        this.groupItem = groupItem;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @OnClick(R.id.tv_pair)
    public void onClick(View v){
        if (onCheckInListener != null)
            onCheckInListener.onCheck(student);
    }

    interface onCheckInListener{
        void onCheck(Student student);
    }
}
