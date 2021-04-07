package com.feipulai.exam.activity.RadioTimer.newRadioTimer;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.config.TestConfigs;

public class RadioDeviceDetailActivity extends BaseTitleActivity {

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_device_deail;
    }

    @Override
    protected void initData() {

    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {

        return builder.setTitle("设备详情");
    }
}
