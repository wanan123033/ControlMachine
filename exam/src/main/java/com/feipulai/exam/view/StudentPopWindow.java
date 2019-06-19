package com.feipulai.exam.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.feipulai.exam.R;

/**
 * Created by pengjf on 2019/4/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class StudentPopWindow extends PopupWindow {
    private Context context;
    private final PopupWindow popupWindow;
    private final ListView lvResults;

    /**
     * @param context
     */
    public StudentPopWindow(Context context) {
        this.context = context;
        View contentView = LayoutInflater.from(context).inflate(
                R.layout.pop_student_layout, null);
        popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        lvResults = contentView.findViewById(R.id.lv_results);
    }

    public ListView getLvResults() {
        return lvResults;
    }

    public void showPop(View view) {
        popupWindow.showAsDropDown(view);
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }


}
