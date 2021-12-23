package com.feipulai.host.activity.data;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import com.feipulai.common.dbutils.BackupManager;
import com.feipulai.host.R;
import com.feipulai.host.db.DBManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClearDataDialog {
    @BindView(R.id.cb_topic)
    CheckBox cb_topic;
    @BindView(R.id.cb_face)
    CheckBox cb_face;
    @BindView(R.id.cb_basic)
    CheckBox cb_basic;
    private Dialog dialog;
    public BackupManager backupManager;
    private OnProcessFinishedListener listener;

    public ClearDataDialog(Context context, OnProcessFinishedListener listener) {
        init(context);
        this.listener = listener;
    }

    private void init(Context context) {
        dialog = new Dialog(context, R.style.dialog_style);
        backupManager = new BackupManager(dialog.getContext(), DBManager.DB_NAME, BackupManager.TYPE_EXAM);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.view_cleardata_dialog, null);
        Window window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        ButterKnife.bind(this, view);
    }

    @OnClick({R.id.btn_cancel, R.id.btn_confirm})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                dismissDialog();
                break;
            case R.id.btn_confirm:
                if (listener != null) {
                    listener.onClearConfirmed(cb_topic.isChecked(), cb_face.isChecked(), cb_basic.isChecked());
                }
                dismissDialog();
                break;
        }
    }


    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void showDialog() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public interface OnProcessFinishedListener {
        /**
         * 确认恢复数据库
         */
        void onClearConfirmed(boolean isDeletePhoto, boolean isDeleteAFR, boolean isDeleteBase);
    }

}
