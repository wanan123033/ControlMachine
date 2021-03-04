package com.feipulai.exam.activity.situp.newSitUp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;

public class NewSitUpPairActivity extends BaseTitleActivity {


    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_sit_up_pair;
    }

    @Override
    protected void initData() {

    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备连接");
    }
}
