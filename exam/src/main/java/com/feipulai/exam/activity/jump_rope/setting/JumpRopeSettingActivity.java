package com.feipulai.exam.activity.jump_rope.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.feipulai.common.utils.ToastUtils;
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
    @BindView(R.id.ll_is_skip)
    LinearLayout llIsSkip;
    @BindView(R.id.cb_full_skip)
    CheckBox cbFullSkip;
    @BindView(R.id.ll_full_skip)
    LinearLayout llFullSkip;
    @BindView(R.id.edit_male_full)
    EditText editMaleFull;
    @BindView(R.id.edit_female_full)
    EditText editFemaleFull;

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
        llFullSkip.setVisibility(View.VISIBLE);
        llIsSkip.setVisibility(View.VISIBLE);
        cbFullSkip.setChecked(jPresenter.setting.isFullSkip());
        editMaleFull.setText(jPresenter.setting.getMaleFullScore());
        editFemaleFull.setText(jPresenter.setting.getFemaleFullScore());
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
    public void onBtnConnect() {
        startActivity(new Intent(this, JumpRopeCorrespondTestActivity.class));
    }

    @Override
    protected void onPause() {
        if (cbFullSkip.isChecked()){
            jPresenter.setting.setFullSkip(isCheckFull());
        }else{
            jPresenter.setting.setFullSkip(cbFullSkip.isChecked());
        }

        super.onPause();
    }
    private boolean isCheckFull(){
        if (TextUtils.isEmpty(editMaleFull.getText().toString())) {
            ToastUtils.showShort("请输入男子满分值");
            return false;
        }
        jPresenter.setting.setMaleFullScore(Integer.valueOf(editMaleFull.getText().toString()));
        if (TextUtils.isEmpty(editFemaleFull.getText().toString())) {
            ToastUtils.showShort("请输入女子满分值");
            return false;
        }
        jPresenter.setting.setFemaleFullScore(Integer.valueOf(editFemaleFull.getText().toString()));
        return true;
    }
}
