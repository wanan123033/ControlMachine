package com.feipulai.host.activity.pullup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.pullup.check.PullUpCheckActivity;
import com.feipulai.host.activity.pullup.setting.PullUpSetting;
import com.feipulai.host.activity.pullup.test.PullUpIndividualActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.config.TestConfigs;

import butterknife.OnClick;

public class PullUpSelectActivity extends BaseTitleActivity {


    private PullUpSetting setting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_pullup_select;
    }

    @Override
    protected void initData() {
        setting = SharedPrefsUtil.loadFormSource(this, PullUpSetting.class);
    }

    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title =  TestConfigs.machineNameMap.get(machineCode)
                + SettingHelper.getSystemSetting().getHostId()
                + "号机-"
                + SettingHelper.getSystemSetting().getTestName();
        builder.setTitle(title) ;
        return builder;
    }

    @OnClick({R.id.tv_count_time, R.id.tv_countless})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.tv_count_time:
                setting.setCountless(false);
                startActivity(new Intent(this, PullUpCheckActivity.class));
                finish();
                break;

            case R.id.tv_countless:
                setting.setCountless(true);
                startActivity(new Intent(this,
                        PullUpIndividualActivity.class));
                finish();
                break;
        }
    }
}
