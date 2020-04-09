package com.feipulai.exam.activity.ranger;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.feipulai.exam.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JumpSettingDialog extends AlertDialog.Builder {
    @BindView(R.id.et_jump_hor1)
    EditText et_jump_hor1;
    @BindView(R.id.et_jump_d1)
    EditText et_jump_d1;
    @BindView(R.id.et_jump_f1)
    EditText et_jump_f1;
    @BindView(R.id.et_jump_m1)
    EditText et_jump_m1;
    @BindView(R.id.et_jump_hor2)
    EditText et_jump_hor2;
    @BindView(R.id.et_jump_d2)
    EditText et_jump_d2;
    @BindView(R.id.et_jump_f2)
    EditText et_jump_f2;
    @BindView(R.id.et_jump_m2)
    EditText et_jump_m2;
    @BindView(R.id.et_range)
    EditText et_range;

    public JumpSettingDialog(Context context) {
        super(context);
        setTitle("跳远设置");
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_jump_setting,null,false);
        ButterKnife.bind(this,view);
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(false);
        setView(view);

        setPositiveButton("确定",null);
        setNegativeButton("取消",null);
    }

    @OnClick({R.id.tv_cc1,R.id.tv_cc2})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_cc1:
                break;
            case R.id.tv_cc2:
                break;
        }
    }
}
