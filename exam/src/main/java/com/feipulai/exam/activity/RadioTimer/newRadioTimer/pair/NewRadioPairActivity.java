package com.feipulai.exam.activity.RadioTimer.newRadioTimer.pair;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.Switch;

import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.jump_rope.adapter.DevicePairAdapter;

import butterknife.BindView;

public class NewRadioPairActivity extends BaseTitleActivity {

    @BindView(R.id.sw_auto_pair)
    Switch mSwAutoPair;
    @BindView(R.id.rv_pairs)
    public RecyclerView mRvPairs;
    public DevicePairAdapter mAdapter;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_new_radio_pair;
    }

    @Override
    protected void initData() {

    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备匹配");
    }
}
