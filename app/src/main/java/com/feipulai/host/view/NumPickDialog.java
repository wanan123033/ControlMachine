package com.feipulai.host.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.feipulai.host.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengjf on 2018/9/3.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class NumPickDialog extends Dialog implements View.OnClickListener {
    private NumPickListener numPickListener;
    private PickerView numberPicker;
    int count = 1 ;
    public NumPickDialog(@NonNull Context context, NumPickListener numPickListener) {
        super(context, R.style.NoTitleDialog);
        this.numPickListener = numPickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_picker);
        numberPicker = (PickerView) findViewById(R.id.num_select);
        List<String> data = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            data.add(i+1+"");
        }
        numberPicker.setData(data);
        numberPicker.setSelected(0);
        numberPicker.setOnSelectListener(new PickerView.onSelectListener() {

            @Override
            public void onSelect(String text) {
                count = Integer.parseInt(text);
            }
        });

        TextView cancel = (TextView) findViewById(R.id.tv_cancel);
        TextView confirm = (TextView) findViewById(R.id.tv_confirm);
        cancel.setOnClickListener(this);
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_confirm:
                numPickListener.onNumPicked(count);
                dismiss();
                break;
        }
    }

    public interface NumPickListener {
        void onNumPicked(int val);
    }
}
