package com.feipulai.exam.activity.account;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Account;
import com.feipulai.exam.netUtils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by zzs on  2021/2/2
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class AccountSettingActivity extends BaseTitleActivity {


    @BindView(R.id.rv_account)
    RecyclerView rvAccount;
    private List<Account> accountList = new ArrayList<>();
    private AccountAdapter mAdapter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_account_setting;
    }

    @Override
    protected void initData() {
        accountList.addAll(DBManager.getInstance().getAccountAll());
        mAdapter = new AccountAdapter(accountList);
        rvAccount.setLayoutManager(new LinearLayoutManager(this));
        rvAccount.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mAdapter.setSeletePosition(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }


    @OnClick({R.id.btn_add, R.id.btn_update, R.id.btn_delete, R.id.btn_all_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
                new AccountDialog(this).showDialog();
                break;
            case R.id.btn_update:
                if (mAdapter.getSeletePosition() == -1) {
                    return;
                }
                new AccountDialog(this).showDialog(accountList.get(mAdapter.getSeletePosition()));
                break;
            case R.id.btn_delete:
                if (mAdapter.getSeletePosition() == -1) {
                    return;
                }
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("温馨提示")
                        .setContentText("是否删除<" + accountList.get(mAdapter.getSeletePosition()).getAccount() + ">帐号")
                        .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        DBManager.getInstance().deleteAccount(accountList.get(mAdapter.getSeletePosition()));
                        accountList.remove(mAdapter.getSeletePosition());
                        mAdapter.setSeletePosition(-1);
                        mAdapter.notifyDataSetChanged();
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();

                    }
                }).show();
                break;
            case R.id.btn_all_delete:
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE).setTitleText("温馨提示")
                        .setContentText("是否删除所有帐号信息")
                        .setConfirmText(getString(R.string.confirm)).setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        DBManager.getInstance().deleteAccountAll();
                        accountList.clear();
                        mAdapter.notifyDataSetChanged();
                    }
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();

                    }
                }).show();

                break;
        }
    }
}
