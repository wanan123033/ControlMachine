package com.feipulai.exam.activity.setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 高级设置密码输入界面
 * Created by zzs on 2018/11/22
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AdvancedPwdActivity extends BaseActivity {

    @BindView(R.id.edit_pwd)
    EditText editPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_pwd);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.btn_confirm)
    public void onViewClicked() {
        if (TextUtils.equals(editPwd.getText().toString(), MyApplication.ADVANCED_PWD)) {
            startActivity(new Intent(this, AdvancedSettingActivity.class));
            finish();
        }
    }
}
