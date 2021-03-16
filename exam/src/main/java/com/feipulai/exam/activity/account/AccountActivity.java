package com.feipulai.exam.activity.account;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.MainActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Account;
import com.orhanobut.logger.utils.LogUtils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zzs on  2021/1/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AccountActivity extends BaseTitleActivity {


    @BindView(R.id.edit_account)
    EditText editAccount;
    @BindView(R.id.edit_pass)
    EditText editPass;
    @BindView(R.id.btn_login)
    Button btnLogin;
    private int errorCount = 0;
    private long errorTime = 0;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备管理");
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_account;
    }

    @Override
    protected void initData() {

    }


    @OnClick(R.id.btn_login)
    public void onViewClicked() {
        if (TextUtils.isEmpty(editAccount.getText().toString())) {
            ToastUtils.showShort("请输入用户名");
            return;
        }
        if (TextUtils.isEmpty(editPass.getText().toString())) {
            ToastUtils.showShort("请输入密码");
            return;
        }

        if (errorCount == 5) {
            long time = DateUtil.getCurrentTime() - errorTime;
            if (time >= 10 * 60 * 1000) {
                errorTime = 0;
                errorCount = 0;
                btnLogin.setEnabled(true);
                btnLogin.setText("登录");
            } else {
                long surplusTime = (10 * 60 * 1000 - time) / 1000;
                ToastUtils.showShort("当前锁定剩余时间为" + surplusTime + "秒");
                return;
            }
        }

        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {
                Account account = DBManager.getInstance().queryAccount(editAccount.getText().toString(), editPass.getText().toString());
                if (account == null) {
                    return new DataBaseRespon(false, "登录失败，请输入正确的用户名与密码", null);
                } else {
                    return new DataBaseRespon(true, "", account);
                }

            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                MyApplication.account = (Account) respon.getObject();
                LogUtils.operation("当前设备登录账号：" + MyApplication.account.toString());
                IntentUtil.gotoActivity(AccountActivity.this, MainActivity.class);
                finish();
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {
                ++errorCount;
                if (errorCount == 5) {
                    btnLogin.setEnabled(false);
                    toastSpeak("登录失败，由于尝试次数过多，帐号锁定请10分钟后重新登录");
                    errorTime = DateUtil.getCurrentTime();
                    countdown();
                } else {
                    toastSpeak("登录失败，当前还剩于" + (5 - errorCount) + "次尝试登录");
                }
            }
        });
    }

    private Disposable disposable;

    public void countdown() {

        disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        long time = DateUtil.getCurrentTime() - errorTime;
                        if (time >= 10 * 60 * 1000) {
                            errorTime = 0;
                            errorCount = 0;
                            btnLogin.setEnabled(true);
                            btnLogin.setText("登录");
                            disposable.dispose();
                        } else {
                            long surplusTime = (10 * 60 * 1000 - time) / 1000;
                            btnLogin.setText(surplusTime + "秒");

                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
