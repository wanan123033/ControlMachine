package com.feipulai.exam.activity.account;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.feipulai.common.utils.DateUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Account;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zzs on  2021/2/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AccountDialog {

    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.edit_account)
    EditText editAccount;
    @BindView(R.id.edit_old_pwd)
    EditText editOldPwd;
    @BindView(R.id.ll_old_pwd)
    LinearLayout llOldPwd;
    @BindView(R.id.edit_pwd)
    EditText editPwd;
    @BindView(R.id.edit_account_two)
    EditText editAccountTwo;
    /**
     * 提示框对象
     */
    private Dialog dialog;
    private Window window = null;
    private Account account;

    public AccountDialog(Context context) {
        init(context);
    }


    /**
     * 初始话对话框
     * <p>
     * <p>
     *
     * @param context
     */
    protected void init(Context context) {
        dialog = new Dialog(context, R.style.dialog_style);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.view_add_account, null);
        ButterKnife.bind(this, view);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);

    }


    @OnClick({R.id.view_txt_cancel, R.id.view_txt_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.view_txt_cancel:
                dismissDialog();
                break;
            case R.id.view_txt_confirm:
                if (account == null) {
                    if (isNullCheck()) {
                        DBManager.getInstance().insterAccount(editAccount.getText().toString(), editPwd.getText().toString(), 1);
                        dismissDialog();
                    }
                } else {
                    if (isCheckUpdate() && isNullCheck()) {
                        account.setPassword(editPwd.getText().toString());
                        account.setUpdateTime(DateUtil.getCurrentTime());
                        DBManager.getInstance().updateAccount(account);
                        dismissDialog();
                    }
                }
                break;
        }
    }

    private boolean isNullCheck() {
        if (TextUtils.isEmpty(editAccount.getText().toString())) {
            ToastUtils.showShort("请输入用户名");
            return false;
        }
        if (TextUtils.isEmpty(editPwd.getText().toString())) {
            ToastUtils.showShort("请输入密码");
            return false;
        }
        if (TextUtils.isEmpty(editAccountTwo.getText().toString())) {
            ToastUtils.showShort("请输入确认密码");
            return false;
        }
        if (!TextUtils.equals(editPwd.getText().toString(), editAccountTwo.getText().toString())) {
            ToastUtils.showShort("请输入两次密码不一致");
            return false;
        }
        return true;
    }

    private boolean isCheckUpdate() {
        if (TextUtils.isEmpty(editOldPwd.getText().toString())) {
            ToastUtils.showShort("请输入帐号旧密码");
            return false;
        }
        if (!TextUtils.equals(account.getPassword(), editOldPwd.getText().toString())) {
            ToastUtils.showShort("旧密码输入错误");
            return false;
        }
        return true;
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void showDialog() {
        this.account = null;
        llOldPwd.setVisibility(View.GONE);
    }

    public void showDialog(Account account) {
        this.account = account;
        llOldPwd.setVisibility(View.VISIBLE);
        txtTitle.setText("修改密码");
    }

    /**
     * 判断进度条是否显示
     * <p>
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/12/2 16:43
     * <br/> @updateTime 2015/12/2 16:43
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     *
     * @return
     */
    public boolean isShow() {
        return dialog != null && dialog.isShowing();
    }


    /**
     * 设置对话框取消监听
     * <p>
     * <p>
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/11/23 15:32
     * <br/> @updateTime 2015/11/23 15:32
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     *
     * @param listener
     */
    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        if (null != dialog) {
            dialog.setOnDismissListener(listener);
        }
    }


}
