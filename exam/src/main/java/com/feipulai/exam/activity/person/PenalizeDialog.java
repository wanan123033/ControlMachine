package com.feipulai.exam.activity.person;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.exam.R;
import com.feipulai.exam.entity.Student;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzs on  2020/12/31
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PenalizeDialog {

    @BindView(R.id.tip)
    TextView tip;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.rv_popup_window)
    RecyclerView rvPopupWindow;
    @BindView(R.id.view_txt_cancel)
    TextView viewTxtCancel;
    @BindView(R.id.view_txt_confirm)
    TextView viewTxtConfirm;
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.turn_left)
    TextView turnLeft;
    @BindView(R.id.turn_right)
    TextView turnRight;
    private Context context;
    private String[] title = {"犯规", "中退", "放弃", "正常"};
    private Dialog dialog;
    private Window window = null;
    /**
     * @param context
     */
    public PenalizeDialog(Context context) {
        this.context = context;
        init();
    }


    protected void init() {
        dialog = new Dialog(context, R.style.dialog_style);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_penalize, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

    }


    @OnClick({R.id.turn_left, R.id.turn_right, R.id.view_txt_cancel, R.id.view_txt_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.turn_left:
                break;
            case R.id.turn_right:
                break;
            case R.id.view_txt_cancel:
                dialog.dismiss();
                break;
            case R.id.view_txt_confirm:
                dialog.dismiss();
                break;
        }
    }

    public void showDialog(int tip) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
        tvTitle.setText(title[tip]);
    }
}
