package com.feipulai.host.activity.vccheck;

import android.content.Intent;
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
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.grip_dynamometer.pair.GripPairActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.config.EventConfigs;
import com.feipulai.host.config.TestConfigs;


import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnItemSelected;

/**
 * Created by pengjf on 2020/6/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripSettingActivity extends BaseTitleActivity{

    @BindView(R.id.sp_device_count)
    Spinner spDeviceCount;
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

        ArrayAdapter spDeviceCountAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"1","2","3","4"});
        spDeviceCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDeviceCount.setAdapter(spDeviceCountAdapter);
        spDeviceCount.setSelection(gripSetting.getDeviceSum()-1);

        deviceCheck.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目设置");
    }


    @OnItemSelected({R.id.sp_device_count})
    public void spinnerItemSelected(Spinner spinner, int position) {
        switch (spinner.getId()) {
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
