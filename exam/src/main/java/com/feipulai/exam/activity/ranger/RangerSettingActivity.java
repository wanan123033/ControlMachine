package com.feipulai.exam.activity.ranger;

import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class RangerSettingActivity extends BaseTitleActivity {

    @BindView(R.id.sp_item)
    Spinner sp_item;
    String[] testItems = {"跳高","撑竿跳高","跳远","立定跳远","三级跳远","标枪","铅球","铁饼","链球"};


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_ranger_setting;
    }

    @Override
    protected void initData() {
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testItems);
        sp_item.setAdapter(spTestRoundAdapter);
    }

    @OnClick({R.id.tv_throw,R.id.tv_staJump,R.id.tv_bluetooth})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_throw:
                break;
            case R.id.tv_staJump:
                break;
            case R.id.tv_bluetooth:
                startActivity(new Intent(this,BluetoothSettingActivity.class));
                break;
        }
    }
}
