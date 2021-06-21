package com.feipulai.exam.activity.jump_rope.setting;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingActivity;
import com.feipulai.exam.activity.jump_rope.base.setting.AbstractRadioSettingPresenter;
import com.feipulai.exam.activity.jump_rope.pair.JumpRopePairActivity;
import com.feipulai.exam.activity.jump_rope.test.JumpRopeTestActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class JumpRopeSettingActivity extends AbstractRadioSettingActivity {
    @BindView(R.id.btn_matching)
    Button btnMatching;
    @BindView(R.id.ll_jump_rope)
    LinearLayout llJumpRope;
    @BindView(R.id.cb_show_stumble)
    CheckBox cbShowStumble;
    @BindView(R.id.btn_connect)
    Button btnConnect;
    private JumpRopeSettingPresenter jPresenter;

    @Override
    protected AbstractRadioSettingPresenter getPresenter() {
        return new JumpRopeSettingPresenter(this, this);
    }

    @Override
    protected void initData() {
        super.initData();
        jPresenter = (JumpRopeSettingPresenter) presenter;
        btnMatching.setVisibility(View.VISIBLE);
        llJumpRope.setVisibility(View.VISIBLE);
        btnConnect.setVisibility(View.VISIBLE);
        cbShowStumble.setChecked(jPresenter.setting.isShowStumbleCount());

    }

    @OnClick(R.id.btn_matching)
    public void onBtnMatching() {
        startActivity(new Intent(this, JumpRopePairActivity.class));
    }

    @OnClick(R.id.cb_show_stumble)
    public void onCbShowStumble() {
        jPresenter.setShowStumbleCount(cbShowStumble.isChecked());
    }

    @OnClick(R.id.btn_connect)
    public void onBtnConnect(){
        startActivity(new Intent(this, JumpRopeCorrespondTestActivity.class));
    }
}
