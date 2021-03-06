package com.feipulai.exam.activity.pushUp;

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
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class PushUpSettingActivity
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
    @BindView(R.id.sp_interval_time)
    Spinner spIntervalTime;
    @BindView(R.id.sp_timeout_dispose)
    Spinner spTimeoutDispose;
    @BindView(R.id.sp_device_num)
    Spinner mSpDeviceNum;
    private Integer[] testRound = new Integer[]{1, 2, 3};
    private String[] timeoutDispose = new String[]{"??????", "?????????"};
    private PushUpSetting setting;
    public static int SETTING_RESULT_OK = 0X10;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_pushup_setting;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestNo.setAdapter(spTestRoundAdapter);
        spTestNo.setSelection(TestConfigs.getMaxTestCount(this) - 1);
        // ???????????????????????????????????????,?????????????????????
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

        Integer[] intervalTime = new Integer[PushUpSetting.MAX_INTERVAL_TIME - 1];
        for (int i = 0; i <= PushUpSetting.MAX_INTERVAL_TIME; i++) {
            //???????????????????????????
            if (i > 0 && i < 3)
                continue;
            intervalTime[i == 0 ? i : i - 2] = i;
        }
        ArrayAdapter spIntervalTimeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intervalTime);
        spIntervalTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIntervalTime.setAdapter(spIntervalTimeAdapter);
        spIntervalTime.setSelection(setting.getIntervalTime() == 0 ? setting.getIntervalTime() : setting.getIntervalTime() - 2);
        ArrayAdapter spTimeoutDisposeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, timeoutDispose);
        spTimeoutDisposeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTimeoutDispose.setAdapter(spTimeoutDisposeAdapter);
        spTimeoutDispose.setSelection(setting.getTimeoutDispose());

        //????????????????????????????????????
        if (setting.getTestType() == 0) {
            ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new Integer[]{1});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpDeviceNum.setAdapter(adapter);
            // ???????????????????????????????????????,?????????????????????
            mSpDeviceNum.setEnabled(false);
        } else {
            Integer[] deviceSums = new Integer[PushUpSetting.MAX_DEVICE];
            for (int i = 0; i < deviceSums.length; i++) {
                deviceSums[i] = i + 1;
            }
            ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deviceSums);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpDeviceNum.setAdapter(adapter);
            mSpDeviceNum.setSelection(setting.getDeviceSum() - 1);
        }
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("????????????") ;
    }

    @Override
    public void finish() {
        EventBus.getDefault().post(new BaseEvent(EventConfigs.ITEM_SETTING_UPDATE));
        SharedPrefsUtil.save(this, setting);
        Logger.i("?????????????????????:" + setting.toString());
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

    @OnItemSelected({R.id.sp_test_no, R.id.sp_interval_time, R.id.sp_timeout_dispose, R.id.sp_device_num})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_test_no:
                setting.setTestNo(position + 1);
                break;
            case R.id.sp_timeout_dispose:
                setting.setTimeoutDispose(position);
                break;
            case R.id.sp_interval_time:
                setting.setIntervalTime(position);
                if (position > 0) {
                    setting.setIntervalTime(position + 2);
                }
                break;
            case R.id.sp_device_num:
                setting.setDeviceSum(position + 1);
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
                ToastUtils.showShort("???????????????,????????????");
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
                ToastUtils.showShort("???????????????????????????1000");
            } else {
                setting.setMaleFullScore(value);
            }
        }

        text = editFemaleFull.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("???????????????????????????1000");
            } else {
                setting.setFemaleFullScore(value);
            }
        }

        text = etTestTime.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            value = Integer.parseInt(text);
            if (value > 1000) {
                ToastUtils.showShort("???????????????????????????1000");
            } else {
                setting.setTestTime(value);
            }
        }
    }

}
