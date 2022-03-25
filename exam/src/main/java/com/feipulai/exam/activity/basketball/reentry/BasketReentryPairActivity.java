package com.feipulai.exam.activity.basketball.reentry;

import android.support.v7.widget.GridLayoutManager;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;

import java.util.List;

public class BasketReentryPairActivity extends SitPullPairActivity {
    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new BallReentryPairPresenter(this, this);
    }

    private List<StuDevicePair> pairs;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_ball_reentry_pair;
    }

    @Override
    public void initView(boolean isAutoPair, List pairs) {
        super.initView(isAutoPair, pairs);
        this.pairs = pairs;
        for (int i = 0; i < this.pairs.size(); i++) {
            switch (i) {
                case 0:
                    this.pairs.get(i).getBaseDevice().setDeviceName("-");
                    break;
                case 1:
                    this.pairs.get(i).getBaseDevice().setDeviceName("近红外");
                    break;
                case 2:
                    this.pairs.get(i).getBaseDevice().setDeviceName("折返点");
                    break;
                case 3:
                    this.pairs.get(i).getBaseDevice().setDeviceName("计时屏");
                    break;
            }
        }
        this.mRvPairs.setLayoutManager(new GridLayoutManager(this, 1));
        this.mAdapter = new BallReentryPairAdapter(this, pairs);
        this.mRvPairs.setAdapter(this.mAdapter);
        this.mRvPairs.setClickable(true);
        this.mAdapter.setOnItemClickListener(this);
    }
}
