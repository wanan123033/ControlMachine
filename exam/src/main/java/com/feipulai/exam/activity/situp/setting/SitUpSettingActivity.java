package com.feipulai.exam.activity.situp.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.feipulai.exam.activity.setting.CorrespondTestActivity;

public class SitUpSettingActivity extends AbstractRadioSettingActivity implements View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llTestMin.setVisibility(View.VISIBLE);
        llTestMax.setVisibility(View.VISIBLE);
        llTestLed.setVisibility(View.VISIBLE);
        Button btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setVisibility(View.VISIBLE);
        btnConnect.setOnClickListener(this);

    }

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new SitUpSettingPresenter(this, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_connect:
                startActivity(new Intent(this, CorrespondTestActivity.class));
                break;
        }
    }
}
