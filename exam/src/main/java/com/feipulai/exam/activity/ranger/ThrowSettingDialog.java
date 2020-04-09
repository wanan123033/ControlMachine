package com.feipulai.exam.activity.ranger;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.exam.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThrowSettingDialog extends AlertDialog.Builder {
    @BindView(R.id.sp_item)
    Spinner sp_item;
    @BindView(R.id.et_stand)
    EditText et_stand;
    @BindView(R.id.et_range)
    EditText et_range;
    @BindView(R.id.et_d)
    EditText et_d;
    @BindView(R.id.et_f)
    EditText et_f;
    @BindView(R.id.et_m)
    EditText et_m;

    public ThrowSettingDialog(@NonNull Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_throws_setting,null,false);
        ButterKnife.bind(this,view);
        setView(view);
        setTitle("投掷设置");
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(false);

        setPositiveButton("确定",null);
        setNegativeButton("取消",null);
    }

    @OnClick({R.id.tv_stand,R.id.tv_CC})
    public void onClick(View view){

    }
}
