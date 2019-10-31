package com.feipulai.exam.activity.basketball;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.basketball.wiress.BasketBallWiressActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.config.TestConfigs;

import butterknife.OnClick;

public class BasketBallSelectActivity extends BaseTitleActivity {
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_basketball_select;
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        builder.setTitle("模式选择");
        return builder;
    }

    @Override
    protected void initData() {

    }

    @OnClick({R.id.tv_wireless,R.id.tv_wired})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_wireless:
                startActivity(new Intent(this, BasketBallWiressActivity.class));
                break;
            case R.id.tv_wired:
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    startActivity(new Intent(BasketBallSelectActivity.this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
                } else {
                    startActivity(new Intent(BasketBallSelectActivity.this, BaseGroupActivity.class));
                }
                break;
        }
    }
}
