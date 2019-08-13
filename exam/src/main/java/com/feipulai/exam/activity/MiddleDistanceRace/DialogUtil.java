package com.feipulai.exam.activity.MiddleDistanceRace;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * created by ww on 2019/6/21.
 */
public class DialogUtil {

    private static AlertDialog dialog;

    public interface DialogListener {
        void onPositiveClick();

        void onNegativeClick();
    }


    public static void showCommonDialog(Context context, String notice, final DialogListener dialogListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("提示")
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
        dialog.show();
    }

    public static void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
