package com.feipulai.exam.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.EditText;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.bean.UserBean;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.netUtils.OnResultListener;
import com.feipulai.exam.netUtils.netapi.HttpSubscriber;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 登录
 * Created by zzs on  2018/12/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class LoginActivity extends BaseTitleActivity {


    @BindView(R.id.edit_account)
    EditText editAccount;
    @BindView(R.id.edit_pass)
    EditText editPass;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        editAccount.setText(SettingHelper.getSystemSetting().getUserName());
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("登录");
    }

    @OnClick(R.id.btn_login)
    public void onViewClicked() {
        if (isCheckData()) {
            login();
        }
    }

    private boolean isCheckData() {
        if (TextUtils.isEmpty(editAccount.getText().toString())) {
            ToastUtils.showShort("请输入用户名");
            return false;
        }
        if (TextUtils.isEmpty(editPass.getText().toString())) {
            ToastUtils.showShort("请输入密码");
            return false;
        }
        return true;
    }

    /**
     * 用户登录
     */
    private void login() {
        new HttpSubscriber().login(this, editAccount.getText().toString(), editPass.getText().toString(), new OnResultListener<UserBean>() {
            @Override
            public void onResponseTime(String responseTime) {

            }

            @Override
            public void onSuccess(UserBean userBean) {
                MyApplication.TOKEN = userBean.getToken();
                SharedPrefsUtil.putValue(MyApplication.getInstance(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.TOKEN, userBean.getToken());
                ToastUtils.showShort("登录成功");

                if (!TextUtils.isEmpty(userBean.getExamName())) {
                    SettingHelper.getSystemSetting().setTestName(userBean.getExamName());
                }
                SettingHelper.getSystemSetting().setUserName(editAccount.getText().toString());
                SettingHelper.getSystemSetting().setSitCode(userBean.getSiteId());
                SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
                finish();
            }

            @Override
            public void onFault(int code, String errorMsg) {
                ToastUtils.showShort(errorMsg);
            }
        });
    }
}
