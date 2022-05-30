package com.feipulai.exam.activity.pullup.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.feipulai.exam.activity.jump_rope.pair.JumpRopePairActivity;
import com.feipulai.exam.activity.pullup.pair.PullUpPairActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class PullUpSettingActivity extends AbstractRadioSettingActivity {

    private PullUpSetting setting;

    @BindView(R.id.btn_matching)
    Button btnMatching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setting = SharedPrefsUtil.loadFormSource(this, PullUpSetting.class);
        if (setting.isCountless()) {
            llTestTime.setVisibility(View.GONE);
            mSpDeviceNum.setSelection(0);
            mSpDeviceNum.setEnabled(false);
        }
        if (setting.isHandCheck()) {
            llTestAngle.setVisibility(View.VISIBLE);
            mSpDeviceNum.setSelection(0);
            mSpDeviceNum.setEnabled(false);
        }
        btnMatching.setVisibility(View.VISIBLE);
    }

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new PullUpSettingPresenter(this, this);
    }

    @OnClick(R.id.btn_matching)
    public void onBtnMatching() {
        startActivity(new Intent(this, PullUpPairActivity.class));
    }
}
