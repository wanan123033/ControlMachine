package com.feipulai.host.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.host.R;

/**
 * Created by pengjf on 2018/8/10.
 */

public class NumPickerView extends RelativeLayout implements View.OnClickListener, NumPickDialog.NumPickListener {
    private TextView tvTitle;
    private TextView tvTestTimes;
    LinearLayout pickLayout ;
    private TerminalNumListener numListener ;
    private final NumPickDialog pickDialog;

    public void setNumListener(TerminalNumListener numListener) {
        this.numListener = numListener;
    }
    public NumPickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_picker_num, this, true);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTestTimes = (TextView) findViewById(R.id.tv_test_times);
        pickLayout = (LinearLayout) findViewById(R.id.ll_picker);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.select_num);

        if (attributes != null){
           String title =  attributes.getString(R.styleable.select_num_title);
           if (!TextUtils.isEmpty(title)){
               tvTitle.setText(title);
           }
        }

        pickLayout.setOnClickListener(this);
        pickDialog = new NumPickDialog(context ,this);
    }

    /**
     * 设置标题
     *
     * @param title
     */
    public void setText(String title) {
        tvTitle.setText(title);
    }

    /**
     * 设置不能点击
     */
    public void setDisable() {
        pickLayout.setEnabled(false);
    }

    public void setEnable(){
        pickLayout.setEnabled(true);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_picker:
                showDialog();
                break;
        }
    }

    private void showDialog() {
        if (pickDialog != null && pickDialog.isShowing()){
            pickDialog.dismiss();
        }

        pickDialog.show();
    }

    @Override
    public void onNumPicked(int val) {
        if (numListener != null){
            numListener.onNUmChangerListener(val);
            tvTestTimes.setText(val+"");
        }
    }

    public interface TerminalNumListener{
       void onNUmChangerListener(int num) ;
    }
}
