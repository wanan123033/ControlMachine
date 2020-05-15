package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.feipulai.exam.R;

/**
 * created by ww on 2019/6/21.
 */
public class DialogUtil {

    private Context context;
    private AlertDialog.Builder builder;

    public DialogUtil(Context context) {
        this.context = context;
    }

    private AlertDialog dialog;

    public interface DialogListener {
        void onPositiveClick();

        void onNegativeClick();
    }

    public void showCommonDialog(String notice, int bitmapId, final DialogListener dialogListener) {
        builder = new AlertDialog.Builder(context).setTitle("提示").setIcon(bitmapId==0?android.R.drawable.ic_dialog_info:bitmapId)
                .setMessage(notice).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogListener.onPositiveClick();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogListener.onNegativeClick();
                        dialogInterface.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
