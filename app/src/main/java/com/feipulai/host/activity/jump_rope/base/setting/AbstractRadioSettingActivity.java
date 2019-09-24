package com.feipulai.host.activity.jump_rope.base.setting;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class AbstractRadioSettingActivity
        extends BaseTitleActivity
        implements RadioSettingContract.View<AbstractRadioSettingPresenter>,
        TextWatcher,
        AdapterView.OnItemSelectedListener {

    @BindView(R.id.sp_device_num)
    Spinner mSpDeviceNum;
    @BindView(R.id.et_test_time)
    EditText mNpSecond;

    private AbstractRadioSettingPresenter presenter;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_setting;
    }

    @Override
    protected void initData() {
        presenter = getPresenter();
        presenter.start();

        mNpSecond.addTextChangedListener(this);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.item_setting_title);
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
    public void showDeviceSum(int deviceSum) {
        mSpDeviceNum.setSelection(deviceSum - 1);
    }

    @Override
    public void showTestTime(int testTime) {
        mNpSecond.setText(testTime + "");
    }

    @Override
    public void showMax(int maxDeviceSum) {
        Integer[] deviceSums = new Integer[maxDeviceSum];
        for (int i = 1; i <= deviceSums.length; i++) {
            deviceSums[i - 1] = i;
        }
        mSpDeviceNum.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceSums));
        mSpDeviceNum.setOnItemSelectedListener(this);
    }

    @Override
    public void showToast(String err) {
        ToastUtils.showShort(err);
    }

}
