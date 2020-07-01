package com.feipulai.exam.activity.grip;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;


import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnItemSelected;

/**
 * Created by pengjf on 2020/6/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripSettingActivity extends BaseTitleActivity  {

    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
    @BindView(R.id.sp_test_round)
    Spinner spTestRound;
    @BindView(R.id.rg_test_pattern)
    RadioGroup rgTestPattern;
    @BindView(R.id.ll_full_skip)
    LinearLayout fullSkip;
    @BindView(R.id.ll_full)
    LinearLayout full;
    @BindView(R.id.tv_device_check)
    TextView deviceCheck;
    private Integer[] testRound = new Integer[]{1, 2, 3};

    private GripSetting gripSetting;


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sitreach_setting;
    }

    @Override
    protected void initData() {
        //获取项目设置
        gripSetting = SharedPrefsUtil.loadFormSource(this, GripSetting.class);
        if (gripSetting == null)
            gripSetting = new GripSetting();
        ArrayAdapter spTestRoundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, testRound);
        spTestRoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTestRound.setAdapter(spTestRoundAdapter);

        ArrayAdapter spDeviceCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"1","2","3","4"});
        spDeviceCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(spDeviceCountAdapter);
        spDeviceCount.setSelection(gripSetting.getDeviceSum()-1);

//        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
//            rgTestPattern.setVisibility(View.GONE);
//        } else {
//            rgTestPattern.setVisibility(View.VISIBLE);
//        }
        fullSkip.setVisibility(View.GONE);
        full.setVisibility(View.GONE);
        rgTestPattern.setVisibility(View.GONE);
        deviceCheck.setVisibility(View.GONE);
        if (TestConfigs.sCurrentItem.getTestNum() != 0) {
            // 数据库中已经指定了测试次数,就不能再设置了
            spTestRound.setEnabled(false);
            spTestRound.setSelection(TestConfigs.sCurrentItem.getTestNum() - 1);
        } else {
            spTestRound.setSelection(gripSetting.getTestRound() - 1);
        }


//        if (gripSetting.getTestPattern() == 0) {//连续测试
//            rgTestPattern.check(R.id.rb_continuous_test);
//        } else {
//            rgTestPattern.check(R.id.rb_circulation_test);
//        }


//        if (gripSetting.getManFull() > 0) {
//            editManFull.setText(gripSetting.getManFull() + "");
//        }
//        if (gripSetting.getWomenFull() > 0) {
//            editWomenFull.setText(gripSetting.getWomenFull() + "");
//        }
//        cbFullReturn.setChecked(gripSetting.isFullReturn());
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }


    @OnItemSelected({R.id.sp_test_round,R.id.sp_device_count})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
            case R.id.sp_test_round:
                gripSetting.setTestRound(position + 1);
                EventBus.getDefault().post(new BaseEvent(EventConfigs.UPDATE_TEST_COUNT));
                break;
            case R.id.sp_device_count:
                gripSetting.setDeviceSum(position+1);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsUtil.save(this, gripSetting);
    }


}
