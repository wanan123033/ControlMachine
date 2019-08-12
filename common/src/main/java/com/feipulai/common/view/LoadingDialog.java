package com.feipulai.common.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.feipulai.common.R;


/**
 * 等待提示框封装类
 */
public class LoadingDialog {

    /**
     * 等待提示框对象
     */
    private Dialog progressDialog;
    /**
     * 等待信息
     */
    private TextView txt_Progress;

    private Window window = null;

    public LoadingDialog(Context context) {
        init(context);
    }

    /**
     * 初始话对话框
     * <p>
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/11/19 18:33
     * <br/> @updateTime 2015/11/19 18:33
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     *
     * @param context
     */
    protected void init(Context context) {
        // progressDialog = new Dialog(context);
        progressDialog = new Dialog(context, R.style.dialog_style);
        // this.context = context;
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(context).inflate(R.layout.view_loading_dialog, null);

        window = progressDialog.getWindow(); // 得到对话框
        window.setWindowAnimations(R.style.dialogWindowAnim); // 设置窗口弹出动画

        txt_Progress = (TextView) view.findViewById(R.id.custom_dialog_txt_progress);

        progressDialog.setContentView(view);
    }

    /**
     * 显示等待对话框
     * <p>
     * <p>
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/11/19 18:33
     * <br/> @updateTime 2015/11/19 18:33
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     *
     * @param text                     进度条上的文本
     * @param isCanceledOnTouchOutside 收点击dialog 之外 dialog消失
     */
    public void showDialog(String text, Boolean isCanceledOnTouchOutside) {
        if (!TextUtils.isEmpty(text)) {
            txt_Progress.setText(text);
        }
        progressDialog.setCancelable(isCanceledOnTouchOutside);
        progressDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);


        //        dismissDialog();
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    /**
     * 关闭等待对话框
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/11/19 18:33
     * <br/> @updateTime 2015/11/19 18:33
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     */
    public void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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
        if (progressDialog != null && progressDialog.isShowing()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 显示等待对话框
     * <p>
     * <p>
     * <br/> @version 1.0
     * <br/> @createTime 2015/11/19 18:33
     * <br/> @updateTime 2015/11/19 18:33
     * <br/> @createAuthor yeqing
     * <br/> @updateAuthor yeqing
     * <br/> @updateInfo (此处输入修改内容,若无修改可不写.)
     *
     * @param text 进度条上的文本
     */
    public void setText(String text) {
        if (!TextUtils.isEmpty(text)) {
            txt_Progress.setText(text);
        }
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
    public void setOnDismissListener(android.content.DialogInterface.OnDismissListener listener) {
        if (null != progressDialog) {
            progressDialog.setOnDismissListener(listener);
        }
    }
}
