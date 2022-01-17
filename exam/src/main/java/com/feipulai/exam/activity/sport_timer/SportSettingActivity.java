package com.feipulai.exam.activity.sport_timer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.manager.SportTimerManger;
import com.feipulai.device.serial.RadioManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.CorrespondTestActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.sport_timer.bean.SportTimerSetting;
import com.feipulai.exam.activity.sport_timer.pair.SportPairActivity;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SportSettingActivity extends BaseTitleActivity implements AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener, RadioManager.OnRadioArrivedListener {

    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    @BindView(R.id.sp_test_times)
    Spinner spTestTimes;
    @BindView(R.id.sp_carry_mode)
    Spinner spCarryMode;
    @BindView(R.id.sp_digital)
    Spinner spDigital;
    @BindView(R.id.tv_init_way)
    TextView tvInitWay;
    @BindView(R.id.rg_model)
    RadioGroup rg_model;
    @BindView(R.id.btn_sync_time)
    TextView syncTime;
    @BindView(R.id.et_test_min)
    EditText etTestMin;
    @BindView(R.id.et_sense)
    EditText etSense;
    @BindView(R.id.rb_continue)
    RadioButton rbContinue;
    @BindView(R.id.rb_recycle)
    RadioButton rbRecycle;
    @BindView(R.id.tv_pair)
    TextView tvPair;
    private SportTimerSetting setting;
    private String[] carryMode = new String[]{"四舍五入", "不进位", "非零进位"};
    private String[] digital = new String[]{"十分位", "百分位", "千分位"};
    private String[] deviceCount = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private String[] testCount = new String[]{"1", "2", "3"};

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sport_setting;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, SportTimerSetting.class);
        if (setting == null)
            setting = new SportTimerSetting();
        //精度
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, carryMode);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCarryMode.setAdapter(adapter);
        spCarryMode.setSelection(setting.getCarryMode());
        spCarryMode.setOnItemSelectedListener(this);
        //进位
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, digital);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDigital.setAdapter(adapter1);
        spDigital.setOnItemSelectedListener(this);
        spDigital.setSelection(setting.getDigital());
        //设备数量
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, deviceCount);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(adapter2);
        spDeviceCount.setOnItemSelectedListener(this);
        spDeviceCount.setSelection(setting.getDeviceCount() - 1);

        //测试次数
        if (TestConfigs.getMaxTestCount(this) > 3) {
            testCount = new String[TestConfigs.getMaxTestCount(this)];
            for (int i = 0; i < testCount.length; i++) {
                testCount[i] = i + 1 + "";
            }
        }
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, testCount);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestTimes.setAdapter(adapter3);
        spTestTimes.setOnItemSelectedListener(this);
        spTestTimes.setSelection(setting.getTestTimes() - 1);
        if (TestConfigs.sCurrentItem.getTestNum() > 1) {
            spTestTimes.setSelection(TestConfigs.sCurrentItem.getTestNum() - 1);
            spTestTimes.setEnabled(false);
        }

        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN) {//分组
            rg_model.setVisibility(View.VISIBLE);
            //测试模式
            int testModel = setting.getGroupType();
            rg_model.check(testModel == 1 ? R.id.rb_continue : R.id.rb_recycle);//1连续  0循环
            rg_model.setOnCheckedChangeListener(this);
        }

        RadioManager.getInstance().setOnRadioArrived(this);
        stm.setDeviceState(SettingHelper.getSystemSetting().getHostId(), 0);
        etSense.setText(setting.getSensity()+"");
        etTestMin.setText(setting.getMinEidit()+"");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_device_count:
                setting.setDeviceCount(position + 1);
                break;
            case R.id.sp_test_times:
                setting.setTestTimes(position + 1);
                break;
            case R.id.sp_carry_mode:
                setting.setCarryMode(position);
                TestConfigs.sCurrentItem.setCarryMode(position + 1);
                break;
            case R.id.sp_digital:
                setting.setDigital(position);
                TestConfigs.sCurrentItem.setDigital(position + 1);
                break;
        }
    }
    SportTimerManger stm = new SportTimerManger();
    @Override
    protected void onStop() {
        super.onStop();
        if (TextUtils.isEmpty(etSense.getText().toString())){
            setting.setSensity(20);
        }else {
            setting.setSensity(Integer.parseInt(etSense.getText().toString().trim()));
        }
        stm.setSensitiveTime(SettingHelper.getSystemSetting().getHostId(),setting.getMinEidit());
        if (TextUtils.isEmpty(etTestMin.getText().toString())){
            setting.setMinEidit(1);
        }else {
            setting.setMinEidit(Integer.parseInt(etTestMin.getText().toString().trim()));
        }
        stm.setMinTime(SettingHelper.getSystemSetting().getHostId(),setting.getMinEidit());
        DBManager.getInstance().updateItem(TestConfigs.sCurrentItem);
        SharedPrefsUtil.save(this, setting);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }

    @OnClick({R.id.tv_init_way, R.id.tv_pair, R.id.btn_sync_time,R.id.btn_connect})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_init_way:
                startActivity(new Intent(this, SportInitWayActivity.class));
                break;
            case R.id.tv_pair:
                startActivity(new Intent(this, SportPairActivity.class));
                break;
            case R.id.btn_sync_time:
                stm.syncTime(SettingHelper.getSystemSetting().getHostId(), getTime());
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        int runNum = setting.getDeviceCount();
//                        sportTimerManger.syncTime(SettingHelper.getSystemSetting().getHostId(), getTime());
//                        for (int i = 0; i < runNum; i++) {
//                            try {
//                                Thread.sleep(200);
//                                sportTimerManger.getTime(i + 1, SettingHelper.getSystemSetting().getHostId());
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                },500);
                break;
            case R.id.btn_connect:
                startActivity(new Intent(this, CorrespondTestActivity.class));
                break;
        }
    }

    public int getTime() {
        Calendar Cld = Calendar.getInstance();
        int HH = Cld.get(Calendar.HOUR_OF_DAY);
        int mm = Cld.get(Calendar.MINUTE);
        int SS = Cld.get(Calendar.SECOND);
        int MI = Cld.get(Calendar.MILLISECOND);
        return HH * 60 * 60 * 1000 + mm * 60 * 1000 + SS * 1000 + MI;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_continue:
                setting.setGroupType(1);
                break;
            case R.id.rb_recycle:
                setting.setGroupType(0);
                break;
        }
    }

    @Override
    public void onRadioArrived(Message msg) {

    }

}
