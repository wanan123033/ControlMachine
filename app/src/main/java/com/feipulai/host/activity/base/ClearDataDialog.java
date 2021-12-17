package com.feipulai.host.activity.base;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;

import com.feipulai.host.MyApplication;
import com.feipulai.host.R;
import com.feipulai.host.activity.data.DataManageActivity;
import com.feipulai.host.activity.data.DownLoadPhotoHeaders;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.orhanobut.logger.Logger;

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
                clearData();

                break;
        }
    }

    private void clearData() {
        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {
                if (cb_basic.isChecked()){
                    DBManager.getInstance().clear();
                    SharedPrefsUtil.putValue(dialog.getContext(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                    SharedPrefsUtil.remove(dialog.getContext(), DownLoadPhotoHeaders.class);
                    DBManager.getInstance().initDB();
                    TestConfigs.init(dialog.getContext(), TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                }
                if (cb_face.isChecked()){
                    DBManager.getInstance().clearFace();
                }
                if (cb_topic.isChecked()){
                    FileUtil.deleteDirectory(MyApplication.PATH_IMAGE);
                }
                dismissDialog();
                return new DataBaseRespon(true, "", "");
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                Logger.i("数据清空完成");
                ToastUtils.showShort("数据清空完成");
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });

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
