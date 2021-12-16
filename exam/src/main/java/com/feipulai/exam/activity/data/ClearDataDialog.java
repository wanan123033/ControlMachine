package com.feipulai.exam.activity.data;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;

import com.feipulai.common.db.ClearDataProcess;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.db.DBManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClearDataDialog{
    @BindView(R.id.cb_topic)
    CheckBox cb_topic;
    @BindView(R.id.cb_face)
    CheckBox cb_face;
    @BindView(R.id.cb_basic)
    CheckBox cb_basic;
    private Dialog dialog;

    public ClearDataDialog(Context context){
        init(context);
    }
    private void init(Context context){
        dialog = new Dialog(context, R.style.dialog_style);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        View view = LayoutInflater.from(context).inflate(R.layout.view_cleardata_dialog,null);
        Window window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
        ButterKnife.bind(this,view);
    }
    @OnClick({R.id.btn_cancel,R.id.btn_confirm})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_cancel:
                dismissDialog();
                break;
            case R.id.btn_confirm:
                if (cb_basic.isChecked()){
                    DBManager.getInstance().clear();
                }
                if (cb_face.isChecked()){
                    DBManager.getInstance().clearFace();
                }
                if (cb_topic.isChecked()){
                    FileUtil.deleteDirectory(MyApplication.PATH_FACE);
                    FileUtil.deleteDirectory(MyApplication.PATH_IMAGE);
                }
                break;
        }
    }
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    public void showDialog(){
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }
}
