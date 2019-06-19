package com.feipulai.exam.activity.jump_rope.base.setting;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseActivity;
import com.feipulai.exam.config.TestConfigs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class AbstractRadioSettingActivity
        extends BaseActivity
        implements RadioSettingContract.View<AbstractRadioSettingPresenter>,
        RadioGroup.OnCheckedChangeListener,
        TextWatcher,
        AdapterView.OnItemSelectedListener {

    @BindView(R.id.sp_device_num)
    protected Spinner mSpDeviceNum;
    @BindView(R.id.sp_test_vez)
    Spinner mSpTestVez;
    @BindView(R.id.rg_model)
    RadioGroup mRgModel;
    @BindView(R.id.ll_group_pattern)
    LinearLayout mLlGroupPattern;
    @BindView(R.id.et_test_time)
    EditText mNpSecond;
    @BindView(R.id.ll_test_time)
    protected LinearLayout llTestTime;

    private AbstractRadioSettingPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio_setting);
        ButterKnife.bind(this);

        presenter = getPresenter();

        presenter.start();
        mRgModel.setOnCheckedChangeListener(this);
        mNpSecond.addTextChangedListener(this);
    }

    protected abstract AbstractRadioSettingPresenter getPresenter();

    @Override
    protected void onPause() {
        super.onPause();
        presenter.saveSettings();
    }

    @OnClick(R.id.btn_show_judgements)
    public void onViewClicked() {
        presenter.showJudgements();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_device_num:
                presenter.updateDeviceSum(position + 1);
                break;

            case R.id.sp_test_vez:
                presenter.updateTestNo(position + 1);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        String testTimeStr = mNpSecond.getText().toString().trim();
        if (!TextUtils.isEmpty(testTimeStr)) {
            int testTime = Integer.parseInt(testTimeStr);
            presenter.updateTestTime(testTime);
        }
    }

    @Override
    public void disableGroupSetting() {
        mLlGroupPattern.setVisibility(View.GONE);
    }

    @Override
    public void disableTestNoSetting() {
        mSpTestVez.setEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void showTestNo(int testNo) {
        mSpTestVez.setSelection(testNo - 1);
    }

    @Override
    public void showDeviceSum(int deviceSum) {
        mSpDeviceNum.setSelection(deviceSum - 1);
    }

    @Override
    public void showGroupMode(int groupMode) {
        if (groupMode == TestConfigs.GROUP_PATTERN_SUCCESIVE) {
            mRgModel.check(R.id.rb_successive);
        } else {
            mRgModel.check(R.id.rb_loop);
        }
    }

    @Override
    public void showTestTime(int testTime) {
        mNpSecond.setText(testTime + "");
    }

    @Override
    public void showMax(int maxTestNo, int maxDeviceSum) {
        Integer[] testNos = new Integer[maxTestNo];
        for (int i = 1; i <= testNos.length; i++) {
            testNos[i - 1] = i;
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testNos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpTestVez.setAdapter(adapter);
        mSpTestVez.setOnItemSelectedListener(this);

        Integer[] deviceSums = new Integer[maxDeviceSum];
        for (int i = 1; i <= deviceSums.length; i++) {
            deviceSums[i - 1] = i;
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceSums);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpDeviceNum.setAdapter(adapter);
        mSpDeviceNum.setOnItemSelectedListener(this);
    }

    @Override
    public void showToast(String err) {
        ToastUtils.showShort(err);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_successive:
                presenter.updateGroupMode(TestConfigs.GROUP_PATTERN_SUCCESIVE);
                break;

            case R.id.rb_loop:
                presenter.updateGroupMode(TestConfigs.GROUP_PATTERN_LOOP);
                break;
        }
    }

}
