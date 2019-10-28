package com.feipulai.exam.activity.basketball.wiress;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;

import butterknife.BindView;

public class BasketBallPairActivity extends BaseTitleActivity {

    @BindView(R.id.rv_pairs)
    RecyclerView rv_pairs;
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_pair;
    }

    @Override
    protected void initData() {
        rv_pairs.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
    }
}
