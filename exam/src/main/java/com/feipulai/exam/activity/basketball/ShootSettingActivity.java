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
    @BindView(R.id.ll_shoot_setting)
    LinearLayout shootSetting;

    private Integer[] testRound = new Integer[]{1, 2, 3};
    private String[] backRound = new String[]{"1???", "2???", "3???", "4???", "5???", "6???", "7???", "8???"};
    private String[] interceptRound = new String[]{"1??????", "2??????1", "3??????", "4??????2", "5??????", "6??????1", "7??????", "8??????2", "9??????"};
    private String[] carryMode = new String[]{"????????????", "?????????", "????????????"};
    private ShootSetting setting;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_shoot_setting;
    }

    @Override
    protected void initData() {
        //??????????????????
        setting = SharedPrefsUtil.loadFormSource(this, ShootSetting.class);
        cbFullSkip.setOnCheckedChangeListener(this);
        if (setting.isFullSkip()) {
            llFullSkip.setVisibility(setting.getTestType() == 2 ? View.VISIBLE : View.GONE);
            llFullSkipShoot.setVisibility(setting.getTestType() == 3 ? View.VISIBLE : View.GONE);
        } else {
            llFullSkip.setVisibility(View.GONE);
            llFullSkipShoot.setVisibility(View.GONE);
        }

        if (setting.getTestPattern() == 0) {//????????????
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

        //??????????????????
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestNo.setAdapter(adapter);

        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            spTestNo.setEnabled(false);
            // ???????????????????????????????????????,?????????????????????
            spTestNo.setSelection(TestConfigs.sCurrentItem.getTestNum() - 1);
            setting.setTestNo(TestConfigs.sCurrentItem.getTestNum());
        } else {
            spTestNo.setSelection(setting.getTestNo() - 1);
        }

        //????????????
        ArrayAdapter adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carryMode);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarryMode.setAdapter(adapter1);
        spCarryMode.setSelection(TestConfigs.sCurrentItem.getCarryMode() > 0 ? TestConfigs.sCurrentItem.getCarryMode() - 1 : 0);

        //?????????????????????
        ArrayAdapter adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spShootNo.setAdapter(adapter2);
        spShootNo.setSelection(0);
        spShootNo.setEnabled(false);

        //??????1????????????
        ArrayAdapter adapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBack1No.setAdapter(adapter3);
        spBack1No.setSelection(setting.getBack1No() - 1);
        //??????2????????????
        ArrayAdapter adapter4 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBack2No.setAdapter(adapter4);
        spBack2No.setSelection(setting.getBack2No() - 1);

        //????????????
        ArrayAdapter adapter5 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, backRound);
        adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spStartNo.setAdapter(adapter5);
        spStartNo.setSelection(setting.getStartNo() - 1);
        //???????????????
        ArrayAdapter adapter6 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, interceptRound);
        adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInterceptNo.setAdapter(adapter6);
        spInterceptNo.setSelection(setting.getInterceptNo() - 1);
        shootSetting.setVisibility(setting.getTestType() == 2 ? View.VISIBLE : View.GONE);
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
            ToastUtils.showShort("???????????????????????????????????????");
            ToastUtils.showShort("????????????????????????");
            return false;
        }
        int type = setting.getTestType();
        if (type == 2) {
            setting.setMaleFullDribble(Integer.valueOf(maleEditText.getText().toString()));
        } else {
            setting.setMaleFullShoot(Integer.valueOf(maleEditText.getText().toString()));
        }

        if (TextUtils.isEmpty(femaleEdit.getText().toString())) {
            ToastUtils.showShort("???????????????????????????????????????");
            ToastUtils.showShort("????????????????????????");
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
            case R.id.rb_loop://????????????
                setting.setTestPattern(1);
                break;
            case R.id.rb_continuous://????????????
                setting.setTestPattern(0);
                break;
        }
    }

    @OnItemSelected({R.id.sp_carryMode, R.id.sp_test_no, R.id.sp_intercept_no, R.id.sp_start_no,
            R.id.sp_back1_no, R.id.sp_back2_no})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_carryMode:
                TestConfigs.sCurrentItem.setCarryMode(position + 1);
                break;
            case R.id.sp_test_no:
                setting.setTestNo(position + 1);
                break;
            case R.id.sp_intercept_no:
                if (position < 3) {
                    spInterceptNo.setSelection(setting.getInterceptNo() - 1);
                    toastSpeak("???????????????????????????");
                    return;
                }
                setting.setInterceptNo(position + 1);
                break;
            case R.id.sp_start_no:
                if (position == 0) {
                    spStartNo.setSelection(setting.getStartNo() - 1);
                    toastSpeak("?????????????????????1???");
                    return;
                }
                setting.setStartNo(position + 1);
                break;
            case R.id.sp_back1_no:
                if (position == 0) {
                    spBack1No.setSelection(setting.getStartNo() - 1);
                    toastSpeak("?????????????????????1???");
                    return;
                }
                setting.setBack1No(position + 1);
                break;
            case R.id.sp_back2_no:
                if (position == 0) {
                    spBack2No.setSelection(setting.getStartNo() - 1);
                    toastSpeak("?????????????????????1???");
                    return;
                }
                setting.setBack2No(position + 1);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPrefsUtil.save(this, setting);
    }
}
