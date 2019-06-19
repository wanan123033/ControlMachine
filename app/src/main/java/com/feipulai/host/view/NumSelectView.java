package com.feipulai.host.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.feipulai.host.R;

/**
 * Created by pengjf on 2018/8/10.
 * @deprecated
 */

public class NumSelectView extends RelativeLayout implements View.OnClickListener {
    private TextView tvTitle;
    private TextView tvTestTimes;
    private RelativeLayout rlUp;
    private RelativeLayout rlDown;
    private RelativeLayout rlSwitch;
    private int num = 1;
    private TerminalNumListener numListener ;
    public void setNumListener(TerminalNumListener numListener) {
        this.numListener = numListener;
    }

    private static final String TAG = "NumSelectView";
    public NumSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_select_num, this, true);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTestTimes = (TextView) findViewById(R.id.tv_test_times);
        rlUp = (RelativeLayout) findViewById(R.id.rl_up);
        rlDown = (RelativeLayout) findViewById(R.id.rl_down);
        rlSwitch = (RelativeLayout) findViewById(R.id.rl_switch);

        rlUp.setOnClickListener(this);
        rlDown.setOnClickListener(this);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.select_num);

        if (attributes != null){
           String title =  attributes.getString(R.styleable.select_num_title);
           if (!TextUtils.isEmpty(title)){
               tvTitle.setText(title);
           }
        }

        tvTestTimes.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (numListener != null){
                    numListener.onNUmChangerListener(getNum());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }




    /**
     * 设置标题
     *
     * @param title
     */
    public void setText(String title) {
        tvTitle.setText(title);
    }

    public void add() {
        num++;
        tvTestTimes.setText(num + "");
        Log.i(TAG, "add: "+num);
    }

    public void minus() {
        if (num > 1)
            num--;
        else
            num = 1;
        tvTestTimes.setText(num + "");
        Log.i(TAG, "minus: "+num);
    }

    /**
     * 设置不能点击
     */
    public void setDisable() {
        rlDown.setClickable(false);
        rlUp.setClickable(false);
    }

    /**
     * 获取数量
     *
     * @return
     */
    public int getNum() {
        return num;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_up:
                add();
                break;
            case R.id.rl_down:
                minus();
                break;
        }
    }

    public interface TerminalNumListener{
       void onNUmChangerListener(int num) ;
    }
}
