package com.feipulai.exam.activity.ranger;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.TextChangeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

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

    RangerSetting setting;

    public ThrowSettingDialog(@NonNull Context context) {
        super(context);
        setting = SharedPrefsUtil.loadFormSource(context,RangerSetting.class);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_throws_setting,null,false);
        ButterKnife.bind(this,view);
        setView(view);
        setTitle("投掷设置");
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(false);

        setPositiveButton("确定",null);
        setNegativeButton("取消",null);

        et_stand.addTextChangedListener(new TextChangeListener(R.id.et_stand) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_range.addTextChangedListener(new TextChangeListener(R.id.et_range) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_d.addTextChangedListener(new TextChangeListener(R.id.et_d) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_f.addTextChangedListener(new TextChangeListener(R.id.et_f) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
        et_m.addTextChangedListener(new TextChangeListener(R.id.et_m) {
            @Override
            protected void afterTextChanged(Editable s, int id) {
                afterTextChanged1(s,id);
            }
        });
    }

    @OnClick({R.id.tv_stand,R.id.tv_CC})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_stand:
                break;
            case R.id.tv_CC:
                break;
        }
    }

    @OnItemSelected({R.id.sp_item})
    public void spinnerItemSelected(Spinner spinner, int position) {
        setting.setItemType(position);
    }

    public void afterTextChanged1(Editable s,int id) {
        switch (id){
            case R.id.et_stand:
                setting.setRadius(Integer.parseInt(et_stand.getText().toString()));
                break;
            case R.id.et_range:
                setting.setQd_hor(Integer.parseInt(et_range.getText().toString()));
                break;
            case R.id.et_d:
                setting.setDu(Integer.parseInt(et_d.getText().toString()));
                break;
            case R.id.et_f:
                setting.setFen(Integer.parseInt(et_f.getText().toString()));
                break;
            case R.id.et_m:
                setting.setMiao(Integer.parseInt(et_m.getText().toString()));
                break;

        }
    }
}
