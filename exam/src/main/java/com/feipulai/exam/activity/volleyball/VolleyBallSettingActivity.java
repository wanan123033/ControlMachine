package com.feipulai.exam.activity.volleyball;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class VolleyBallSettingActivity
        extends BaseTitleActivity
        implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, TextWatcher {

    @BindView(R.id.sp_test_no)
    Spinner spTestNo;
    @BindView(R.id.cb_full_skip)
    CheckBox cbFullSkip;
    @BindView(R.id.edit_male_full)
    EditText editMaleFull;
    @BindView(R.id.edit_female_full)
    EditText editFemaleFull;
    @BindView(R.id.ll_full_skip)
    LinearLayout llFullSkip;
    @BindView(R.id.rg_group_mode)
    RadioGroup rgGroupMode;
    @BindView(R.id.et_test_time)
    EditText etTestTime;

    private Integer[] testRound = new Integer[]{1, 2, 3};

    private VolleyBallSetting setting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_volleyball_setting;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, VolleyBallSetting.class);

        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestNo.setAdapter(spTestRoundAdapter);
        spTestNo.setSelection(TestConfigs.getMaxTestCount(this) - 1);
        // 数据库中已经指定了测试次数,就不能再设置了
        spTestNo.setEnabled(TestConfigs.sCurrentItem.getTestNum() == 0);

        boolean isGroupMode = SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN;
        rgGroupMode.setVisibility(isGroupMode ? View.VISIBLE : View.GONE);
        rgGroupMode.setOnCheckedChangeListener(this);
        rgGroupMode.check(setting.getGroupMode() == TestConfigs.GROUP_PATTERN_SUCCESIVE ? R.id.rb_successive : R.id.rb_loop);

        editMaleFull.setText(setting.getMaleFullScore() + "");
        editFemaleFull.setText(setting.getFemaleFullScore() + "");

        cbFullSkip.setChecked(setting.isFullSkip());
        llFullSkip.setVisibility(setting.isFullSkip() ? View.VISIBLE : View.GONE);
        cbFullSkip.setOnCheckedChangeListener(this);

        etTestTime.setText(setting.getTestTime() + "");
        editMaleFull.addTextChangedListener(this);
        editFemaleFull.addTextChangedListener(this);
        etTestTime.addTextChangedListener(this);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        SharedPrefsUtil.save(this, setting);
        Logger.i("保存排球设置:" + setting.toString());
        super.finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_loop:
                setting.setGroupMode(TestConfigs.GROUP_PATTERN_LOOP);
                break;

            case R.id.rb_successive:
                setting.setGroupMode(TestConfigs.GROUP_PATTERN_SUCCESIVE);
                break;
        }
    }

    @OnItemSelected({R.id.sp_test_no})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {

            case R.id.sp_test_no:
                setting.setTestNo(position + 1);
                break;

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {

            case R.id.cb_full_skip:
                setting.setFullSkip(isChecked);
                llFullSkip.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;

        }
    }

    @OnClick({R.id.tv_judgement})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_judgement:
                ToastUtils.showShort("功能开发中,敬请期待");
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = editMaleFull.getText().toString();
        int value;
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("满分最大值不能超过1000");
            } else {
                setting.setMaleFullScore(value);
            }
        }

        text = editFemaleFull.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("满分最大值不能超过1000");
            } else {
                setting.setFemaleFullScore(value);
            }
        }

        text = etTestTime.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("满分最大值不能超过1000");
            } else {
                setting.setTestTime(value);
            }
        }
    }

}
