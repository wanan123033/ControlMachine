package com.feipulai.common.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.feipulai.common.R;
import com.feipulai.common.utils.ActivityCollector;

import java.lang.reflect.Field;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/12/27.
 */

public class DialogUtils {

    /**
     * 退出前弹窗确认
     *
     * @param activity
     */
    public static void BeSureEsc(final Activity activity) {
        new AlertDialog.Builder(activity).setTitle("确认退出吗？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCollector.getInstance().finishAllActivity();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 点击“返回”后的操作,这里不设置没有任何操作
                    }
                }).show();
    }

    /**
     * 设置dialog中isShowing属性的状态值
     */
    public static void setMShowing(DialogInterface dialog, boolean mShowing) {
        try {
            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(dialog, mShowing);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建Dialog
     *
     * @param c          上下文环境
     * @param view       Dialog视图
     * @param cancelable Dialog是否可以返回取消
     * @return Dialog。
     */
    public static Dialog create(Context c, View view, boolean cancelable) {
        Dialog dialog = new Dialog(c, R.style.theme_dialog);
        dialog.setContentView(view);
        dialog.setCancelable(cancelable);

        return dialog;
    }
}
