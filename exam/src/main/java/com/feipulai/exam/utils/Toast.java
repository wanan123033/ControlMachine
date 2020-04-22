package com.feipulai.exam.utils;

import android.content.Context;

public class Toast extends android.widget.Toast {
    private static android.widget.Toast mToast;
    private Toast(Context context) {
        super(context);
    }

    public static void showToast(Context mContext, String text, int duration) {
        if (mToast != null)
            mToast.setText(text);
        else
            mToast = makeText(mContext, text, duration);
        mToast.show();
    }

    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }

}
