package com.feipulai.exam.activity.basketball.pair;

import android.support.v7.widget.GridLayoutManager;

import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.List;

public class BasketBallPairActivity
        extends SitPullPairActivity {
    public SitPullUpPairPresenter getPresenter() {
        return new BasketBallPairPresenter(this, this);
    }

    @Override
    public void initView(boolean isAutoPair, List pairs) {
        super.initView(isAutoPair, pairs);
        this.mRvPairs.setLayoutManager(new GridLayoutManager(this, 1));
        this.mAdapter = new BallPairAdapter(this, pairs);
        this.mRvPairs.setAdapter(this.mAdapter);
        this.mRvPairs.setClickable(true);
        this.mAdapter.setOnItemClickListener(this);
    }
}