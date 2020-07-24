package com.feipulai.host.activity.data;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.feipulai.host.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zzs on  2020/7/23
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DownLoadProgressDialog {


    @BindView(R.id.txt_title)
    TextView txtTitle;
    @BindView(R.id.progress_storage)
    ProgressBar progressStorage;
    @BindView(R.id.txt_page)
    TextView txtPage;
    @BindView(R.id.txt_total)
    TextView txtTotal;
    @BindView(R.id.txt_cancel)
    TextView txtCancel;
    private Dialog dialog;
    private Context context;
    private Window window = null;

    public DownLoadProgressDialog(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(context).inflate(R.layout.view_down_progress, null);
        ButterKnife.bind(this, view);

        dialog = new Dialog(context, R.style.dialog_style);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        window = dialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画
        dialog.setContentView(view);
    }

    public void setCancelClickListener(View.OnClickListener listener) {
        txtCancel.setOnClickListener(listener);
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 显示对话框
     */
    public void showDialog() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void setMaxProgress(int maxProgress) {
        progressStorage.setMax(maxProgress);
        txtTotal.setText(maxProgress + "");
    }

    public void setProgress(int progress) {
        progressStorage.setProgress(progress);
        txtPage.setText(progress + "");
    }

    private String downFileName;

    public String getDownFileName() {
        return downFileName;
    }

    public void setDownFileName(String downFileName) {
        this.downFileName = downFileName;
    }
}
