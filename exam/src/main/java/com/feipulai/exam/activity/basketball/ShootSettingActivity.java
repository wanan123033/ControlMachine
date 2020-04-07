package com.feipulai.exam.activity.basketball;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShootSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {


    @BindView(R.id.tv_pair)
    TextView tvPair;
    @BindView(R.id.sp_test_no)
    Spinner spTestNo;
    @BindView(R.id.sp_device_num)
    Spinner spDeviceNum;
    @BindView(R.id.cb_full_skip)
    CheckBox cbFullSkip;
    @BindView(R.id.edit_male_full)
    EditText editMaleFull;
    @BindView(R.id.edit_female_full)
    EditText editFemaleFull;
    @BindView(R.id.ll_full_skip)
    LinearLayout llFullSkip;
    @BindView(R.id.edit_male_shoot)
    EditText editMaleShoot;
    @BindView(R.id.edit_female_shoot)
    EditText editFemaleShoot;
    @BindView(R.id.ll_full_skip_shoot)
    LinearLayout llFullSkipShoot;
    @BindView(R.id.rb_continuous)
    RadioButton rbContinuous;
    @BindView(R.id.rb_loop)
    RadioButton rbLoop;
    @BindView(R.id.rg_group_mode)
    RadioGroup rgGroupMode;
    @BindView(R.id.et_intercept_time)
    EditText etInterceptTime;
    @BindView(R.id.tv_intercept_time_use)
    TextView tvInterceptTimeUse;
    @BindView(R.id.et_sensitivity)
    EditText etSensitivity;
    @BindView(R.id.tv_sensitivity_use)
    TextView tvSensitivityUse;
    @BindView(R.id.et_host_ip)
    EditText etHostIp;
    @BindView(R.id.et_port)
    EditText etPort;
    @BindView(R.id.tv_ip_connect)
    TextView tvIpConnect;
    @BindView(R.id.ll_ip)
    LinearLayout llIp;
    @BindView(R.id.rb_tenths)
    RadioButton rbTenths;
    @BindView(R.id.rb_percentile)
    RadioButton rbPercentile;
    @BindView(R.id.rg_accuracy)
    RadioGroup rgAccuracy;
    @BindView(R.id.tv_accuracy_use)
    TextView tvAccuracyUse;
    @BindView(R.id.sp_carryMode)
    Spinner spCarryMode;
    @BindView(R.id.view_carryMode)
    LinearLayout viewCarryMode;

    private Integer[] testRound = new Integer[]{1, 2, 3};
    private String[] carryMode = new String[]{"四舍五入", "不进位", "非零进位"};
    private ShootSetting setting;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_shoot_setting;
    }

    @Override
    protected void initData() {
        //获取项目设置
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        cbFullSkip.setOnCheckedChangeListener(this);
        if (setting.isFullSkip()) {
            llFullSkip.setVisibility(setting.getTestType() == 2 ? View.VISIBLE : View.GONE);
            llFullSkipShoot.setVisibility(setting.getTestType() == 3 ? View.VISIBLE : View.GONE);
        } else {
            llFullSkip.setVisibility(View.GONE);
            llFullSkipShoot.setVisibility(View.GONE);
        }

        if (setting.getTestPattern() == 0) {//连续测试
            rgGroupMode.check(R.id.rb_continuous);
        } else {
            rgGroupMode.check(R.id.rb_loop);
        }
        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
            rgGroupMode.setVisibility(View.GONE);
        } else {
            rgGroupMode.setVisibility(View.VISIBLE);
        }
        rgGroupMode.setOnCheckedChangeListener(this);

        //设置测试次数
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestNo.setAdapter(adapter);
        spTestNo.setSelection(TestConfigs.getMaxTestCount(this) - 1);
        // 数据库中已经指定了测试次数,就不能再设置了
        spTestNo.setEnabled(TestConfigs.sCurrentItem.getTestNum() == 0);

        //设置进位
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carryMode);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarryMode.setAdapter(adapter);
        spCarryMode.setSelection(TestConfigs.sCurrentItem.getCarryMode() > 0 ? TestConfigs.sCurrentItem.getCarryMode() - 1 : 0);
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_full_skip:
                int type = setting.getTestType();
                if (isChecked && isCheckSetting(type == 2 ? editMaleFull : editMaleShoot,
                        type == 2 ? editFemaleFull : editFemaleShoot)) {
                    setting.setFullSkip(true);
                } else {
                    cbFullSkip.setChecked(false);
                    setting.setFullSkip(false);
                }
                break;
        }
    }

    private boolean isCheckSetting(EditText maleEditText, EditText femaleEdit) {
        if (TextUtils.isEmpty(maleEditText.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入男子满分值");
            return false;
        }
        int type = setting.getTestType();
        if (type == 2) {
            setting.setMaleFullDribble(Integer.valueOf(maleEditText.getText().toString()));
        } else {
            setting.setMaleFullShoot(Integer.valueOf(maleEditText.getText().toString()));
        }

        if (TextUtils.isEmpty(femaleEdit.getText().toString())) {
            ToastUtils.showShort("启动满分跳过必须设置满分值");
            ToastUtils.showShort("请输入女子满分值");
            return false;
        }
        if (type == 2) {
            setting.setFemaleFullDribble(Integer.valueOf(femaleEdit.getText().toString()));
        } else {
            setting.setFemaleFullShoot(Integer.valueOf(femaleEdit.getText().toString()));
        }
        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_loop://循环测试
                setting.setTestPattern(1);
                break;
            case R.id.rb_continuous://连续测试
                setting.setTestPattern(0);
                break;
        }
    }
}
