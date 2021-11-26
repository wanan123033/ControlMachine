package com.feipulai.exam.activity.situp.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.manager.ShoulderManger;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.feipulai.exam.activity.setting.CorrespondTestActivity;
import com.feipulai.exam.activity.setting.SettingHelper;

public class SitUpSettingActivity extends AbstractRadioSettingActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SitUpSettingPresenter sitUpSettingPresenter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llTestMin.setVisibility(View.VISIBLE);
        llTestMax.setVisibility(View.VISIBLE);
        llTestLed.setVisibility(View.VISIBLE);
        Button btnConnect = findViewById(R.id.btn_connect);
//        Button btnSyncTime = findViewById(R.id.btn_sync_time);
        btnConnect.setVisibility(View.VISIBLE);
        btnConnect.setOnClickListener(this);
        mCbShowLed.setOnCheckedChangeListener(this);
//        btnSyncTime.setOnClickListener(this);
    }

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        sitUpSettingPresenter = new SitUpSettingPresenter(this,this);
        return sitUpSettingPresenter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                startActivity(new Intent(this, CorrespondTestActivity.class));
                break;
//            case R.id.btn_sync_time:
//
//                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        sitUpSettingPresenter.setLedShow(isChecked);
    }


}
