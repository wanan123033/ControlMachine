package com.feipulai.exam.activity.pullup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.pullup.check.PullUpCheckActivity;
import com.feipulai.exam.activity.pullup.setting.PullUpSetting;
import com.feipulai.exam.activity.pullup.test.PullUpGroupActivity;
import com.feipulai.exam.activity.pullup.test.PullUpIndividualActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;

import butterknife.OnClick;

/**
 * Created by zzs on  2019/5/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
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
        builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                        SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN
                                ? PullUpGroupActivity.class
                                : PullUpIndividualActivity.class));
                finish();
                break;
        }
    }

}
