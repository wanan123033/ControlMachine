package com.feipulai.exam.activity.sport_timer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.sport_timer.bean.SportTimerSetting;
import com.feipulai.exam.activity.sport_timer.pair.SportPairActivity;
import com.feipulai.exam.config.TestConfigs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SportSettingActivity extends BaseTitleActivity implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    @BindView(R.id.sp_test_times)
    Spinner spTestTimes;
    @BindView(R.id.sp_mark_degree)
    Spinner spMarkDegree;
    @BindView(R.id.sp_mark_type)
    Spinner spMarkType;
    @BindView(R.id.tv_init_way)
    TextView tvInitWay;
    private SportTimerSetting setting;
    private String[] degree = new String[]{"四舍五入", "不进位", "非零进位"};
    private String[] martType = new String[]{"十分位", "百分位", "千分位"};
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
                android.R.layout.simple_spinner_item, degree);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMarkDegree.setAdapter(adapter);
        spMarkDegree.setSelection(setting.getDegree());
        spMarkDegree.setOnItemSelectedListener(this);
        //进位
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, martType);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMarkType.setAdapter(adapter1);
        spMarkType.setOnItemSelectedListener(this);
        spMarkType.setSelection(setting.getMartType());
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
        spTestTimes.setSelection(setting.getTestTimes()-1);
        if (TestConfigs.sCurrentItem.getTestNum() > 1) {
            spTestTimes.setSelection(TestConfigs.sCurrentItem.getTestNum() - 1);
            spTestTimes.setEnabled(false);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_device_count:
                setting.setDeviceCount(position + 1);
                break;
            case R.id.sp_test_times:
                setting.setTestTimes(position+1);
                break;
            case R.id.sp_mark_degree:
                setting.setDegree(position);
                break;
            case R.id.sp_mark_type:
                setting.setMartType(position);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPrefsUtil.save(this, setting);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @OnClick({ R.id.tv_init_way,R.id.tv_pair})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_init_way:
                startActivity(new Intent(this, SportInitWayActivity.class));
                break;
            case R.id.tv_pair:
                startActivity(new Intent(this, SportPairActivity.class));
                break;
        }
    }
}
