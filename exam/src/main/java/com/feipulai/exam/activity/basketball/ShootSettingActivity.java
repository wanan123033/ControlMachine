package com.feipulai.exam.activity.basketball;

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

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;

import butterknife.BindView;
import butterknife.OnItemSelected;

public class ShootSettingActivity extends BaseTitleActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

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


    @BindView(R.id.rb_tenths)
    RadioButton rbTenths;
    @BindView(R.id.rb_percentile)
    RadioButton rbPercentile;
    @BindView(R.id.rg_accuracy)
    RadioGroup rgAccuracy;

    @BindView(R.id.sp_carryMode)
    Spinner spCarryMode;
    @BindView(R.id.sp_shoot_no)
    Spinner spShootNo;
    @BindView(R.id.sp_start_no)
    Spinner spStartNo;
    @BindView(R.id.sp_back1_no)
    Spinner spBack1No;
    @BindView(R.id.sp_back2_no)
    Spinner spBack2No;
    @BindView(R.id.sp_intercept_no)
    Spinner spInterceptNo;
    @BindView(R.id.view_carryMode)
    LinearLayout viewCarryMode;

    private Integer[] testRound = new Integer[]{1, 2, 3};
    private String[] backRound = new String[]{"1号", "2号", "3号","4号", "5号", "6号","7号", "8号"};
    private String[] interceptRound = new String[]{"1起点", "2折返1", "3投篮","4折返2", "5投篮", "6折返1","7投篮", "8折返2","9投篮"};
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
        ArrayAdapter adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carryMode);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarryMode.setAdapter(adapter1);
        spCarryMode.setSelection(TestConfigs.sCurrentItem.getCarryMode() > 0 ? TestConfigs.sCurrentItem.getCarryMode() - 1 : 0);

        //设置投篮折返点
        ArrayAdapter adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spShootNo.setAdapter(adapter2);
        spShootNo.setSelection(0);
        spShootNo.setEnabled(false);

        //设置1号折返点
        ArrayAdapter adapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBack1No.setAdapter(adapter3);
        spBack1No.setSelection(setting.getBack1No());
        //设置2号折返点
        ArrayAdapter adapter4 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBack2No.setAdapter(adapter4);
        spBack2No.setSelection(setting.getBack2No());

        //设置起点
        ArrayAdapter adapter5 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStartNo.setAdapter(adapter5);
        spStartNo.setSelection(setting.getStartNo());
        //设置指定点
        ArrayAdapter adapter6 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, interceptRound);
        adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterceptNo.setAdapter(adapter6);
        spInterceptNo.setSelection(setting.getInterceptNo());
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

    @OnItemSelected({R.id.sp_carryMode, R.id.sp_test_no,R.id.sp_intercept_no,R.id.sp_start_no,
            R.id.sp_back1_no,R.id.sp_back2_no})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_carryMode:
                TestConfigs.sCurrentItem.setCarryMode(position + 1);
                break;
            case R.id.sp_test_no:
                setting.setTestNo(position + 1);
                break;
            case R.id.sp_intercept_no:
                if (position<3){
                    spInterceptNo.setSelection(setting.getInterceptNo());
                    toastSpeak("至少有一次投篮折返");
                    return;
                }
                setting.setInterceptNo(position);
                break;
            case R.id.sp_start_no:
                if (position == 0){
                    spStartNo.setSelection(setting.getStartNo());
                    toastSpeak("起始截点不能为1号");
                    return;
                }
                setting.setStartNo(position);
                break;
            case R.id.sp_back1_no:
                if (position == 0){
                    spBack1No.setSelection(setting.getStartNo());
                    toastSpeak("起始截点不能为1号");
                    return;
                }
                setting.setBack1No(position);
                break;
            case R.id.sp_back2_no:
                if (position == 0){
                    spBack2No.setSelection(setting.getStartNo());
                    toastSpeak("起始截点不能为1号");
                    return;
                }
                setting.setBack2No(position);
                break;
        }
    }
}
