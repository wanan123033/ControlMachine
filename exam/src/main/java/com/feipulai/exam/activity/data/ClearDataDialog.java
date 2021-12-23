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

import com.bumptech.glide.Glide;
import com.feipulai.common.db.ClearDataProcess;
import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.dbutils.BackupManager;
import com.feipulai.common.utils.FileUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.orhanobut.logger.Logger;
import com.ww.fpl.libarcface.faceserver.FaceServer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClearDataDialog implements ClearDataProcess.OnProcessFinishedListener {
    @BindView(R.id.cb_topic)
    CheckBox cb_topic;
    @BindView(R.id.cb_face)
    CheckBox cb_face;
    @BindView(R.id.cb_basic)
    CheckBox cb_basic;
    private Dialog dialog;
    public BackupManager backupManager;

    public ClearDataDialog(Context context){
        init(context);
    }
    private void init(Context context){
        dialog = new Dialog(context, R.style.dialog_style);
        backupManager = new BackupManager(dialog.getContext(), DBManager.DB_NAME, BackupManager.TYPE_EXAM);
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
        int clearType = 0;
        if (cb_basic.isChecked()){
            clearType = ClearDataProcess.CLEAR_DATABASE;
        }
        if (cb_face.isChecked()){
            clearType = ClearDataProcess.CLEAR_DATABASE_FACE;
        }
        if (cb_basic.isChecked() && cb_face.isChecked()){
            clearType = ClearDataProcess.CLEAR_DATABASE & ClearDataProcess.CLEAR_DATABASE_FACE;
        }
        new DBDataCleaner(dialog.getContext(), clearType, this).process();

        if (cb_topic.isChecked()){
            FileUtil.deleteDirectory(MyApplication.PATH_IMAGE);
        }
        ToastUtils.showShort("数据清空完成");
        dismissDialog();
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

    @Override
    public void onRestoreConfirmed() {

    }

    @Override
    public void onClearDBConfirmed() {
        DataBaseExecutor.addTask(new DataBaseTask(dialog.getContext(), "数据清除中，请稍后。。。.", false) {
            @Override
            public DataBaseRespon executeOper() {
                boolean autoBackup = backupManager.autoBackup();
                Logger.i(autoBackup ? "自动备份成功" : "自动备份失败");
                DBManager.getInstance().clear();
                SharedPrefsUtil.putValue(dialog.getContext(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, null);
                SharedPrefsUtil.putValue(dialog.getContext(), SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.LAST_DOWNLOAD_TIME, null);
                SharedPrefsUtil.remove(dialog.getContext(), DownLoadPhotoHeaders.class);
                DBManager.getInstance().initDB();
                TestConfigs.init(dialog.getContext(), TestConfigs.sCurrentItem.getMachineCode(), TestConfigs.sCurrentItem.getItemCode(), null);
                FileUtil.delete(MyApplication.PATH_IMAGE);//清理图片
                FileUtil.mkdirs(MyApplication.PATH_IMAGE);
                FileUtil.delete(MyApplication.PATH_PDF_IMAGE);//清理成绩PDF与图片
                FileUtil.mkdirs(MyApplication.PATH_PDF_IMAGE);
                FileUtil.delete(MyApplication.BACKUP_DIR);
                FileUtil.mkdirs2(MyApplication.BACKUP_DIR);
                FileUtil.delete(FaceServer.ROOT_PATH);
                FileUtil.mkdirs2(FaceServer.ROOT_PATH);

                Glide.get(dialog.getContext()).clearDiskCache();
                FaceServer.getInstance().unInit();
                FaceServer.getInstance().init(dialog.getContext());
                Logger.i("进行数据清空");

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

    @Override
    public void onClearFaceDBConfirmed() {
        DBManager.getInstance().clearFace();
    }
}
