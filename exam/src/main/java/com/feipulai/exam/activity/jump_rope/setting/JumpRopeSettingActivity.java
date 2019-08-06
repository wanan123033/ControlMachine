package com.feipulai.exam.activity.jump_rope.setting;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.feipulai.exam.activity.jump_rope.pair.JumpRopePairActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class JumpRopeSettingActivity extends AbstractRadioSettingActivity {
    @BindView(R.id.btn_matching)
    Button btnMatching;

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new JumpRopeSettingPresenter(this, this);
    }

    @Override
    protected void initData() {
        super.initData();
        btnMatching.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_matching)
    public void onBtnMatching() {
        startActivity(new Intent(this, JumpRopePairActivity.class));
    }
}
