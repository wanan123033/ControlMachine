package com.feipulai.exam.activity.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import com.feipulai.exam.R;

public class PenalizeDialog extends AlertDialog.Builder{
    private NumberPicker numberPicker;
    private PenalizeListener penalizeListener;
    private int minValue;

    public PenalizeDialog(Context context) {
        super(context);
        numberPicker = new MyNumberPicker(context);
        numberPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS); //禁止输入
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(false);

        LinearLayout layout = new LinearLayout(context);
        layout.setGravity(Gravity.CENTER);
        layout.addView(numberPicker, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setView(layout);
        setTitle("请输入判罚值");

        setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (penalizeListener != null){
                    int num = numberPicker.getValue() + minValue;
                    penalizeListener.penalize(num);
                }
            }
        });
        setNegativeButton("返回", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (penalizeListener != null)
                    penalizeListener.dismisson(dialog);
            }
        });
    }

    public void setMinMaxValue(final int minValue1, int maxValue){
        this.minValue = minValue1;
        if (penalizeListener.getPenalize()) {
            numberPicker.setMaxValue(maxValue - minValue);
        }else {
            numberPicker.setMaxValue(maxValue);
        }
        numberPicker.setMinValue(0);
        numberPicker.setValue(0 - minValue1);
        numberPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int index) {
                Log.e("TAG=====",index+","+minValue1);
                return Integer.toString(index + minValue1);
            }
        });
    }

    public void setPenalizeListener(PenalizeListener penalizeListener) {
        this.penalizeListener = penalizeListener;
    }

    public interface PenalizeListener{
        /**
         * 确认判罚
         * @param value  有正负之分
         */
        void penalize(int value);

        /**
         * 判罚对话框的返回按钮点击事件
         * @param dialog
         */
        void dismisson(DialogInterface dialog);

        /**
         * 是否在判罚中加入减法
         * @return
         */
        boolean getPenalize();
    }
    private static class MyNumberPicker extends NumberPicker{
        public MyNumberPicker(Context context) {
            super(context);
        }

        public MyNumberPicker(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void addView(View child) {
            super.addView(child);
            updateView(child);
        }

        @Override
        public void addView(View child, ViewGroup.LayoutParams params) {
            super.addView(child, params);
            updateView(child);
        }

        @Override
        public void addView(View child, int index, ViewGroup.LayoutParams params) {
            super.addView(child, index, params);
            updateView(child);
        }

        private void updateView(View view) {
            if (view instanceof EditText) {
                //设置文字的颜色和大小
                ((EditText) view).setTextColor(getResources().getColor(R.color.black));
                ((EditText) view).setTextSize(20);
            }
        }
    }
}
