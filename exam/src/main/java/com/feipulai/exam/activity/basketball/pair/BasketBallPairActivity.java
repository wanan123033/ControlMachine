package com.feipulai.exam.activity.basketball.pair;

import android.support.v7.widget.GridLayoutManager;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

public class BasketBallPairActivity
        extends SitPullPairActivity {
    public SitPullUpPairPresenter getPresenter() {
        return new BasketBallPairPresenter(this, this);
    }

    private List<StuDevicePair> pairs;

    @Override
    public void initView(boolean isAutoPair, List pairs) {
        super.initView(isAutoPair, pairs);
        this.pairs = pairs;
        this.mRvPairs.setLayoutManager(new GridLayoutManager(this, 1));
        this.mAdapter = new BallPairAdapter(this, pairs);
        this.mRvPairs.setAdapter(this.mAdapter);
        this.mRvPairs.setClickable(true);
        this.mAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.BACKBALL_FREQUENCY_DATA) {
            Basketball868Result result = (Basketball868Result) baseEvent.getData();
            if (getFocusPositionDeviceCode() == result.getDeviceCode()) {
                pairs.get(presenter.focusPosition).getBaseDevice().setDeviceVersion(result.getVersionNum());
            }
        }
    }

    /**
     * 定位选择配对的是那个设备类型 3 子机 2 LED
     *
     * @return
     */
    public int getFocusPositionDeviceCode() {
        if (TestConfigs.sCurrentItem.getMachineCode() == ItemDefault.CODE_LQYQ) {
            switch (presenter.focusPosition) {
                case 0:
                    return 3;
                case 1:
                    return 2;
            }
        } else {
            switch (presenter.focusPosition) {
                case 0:
                case 1:
                    return 3;
                case 2:
                    return 2;
            }

        }
        return 0;
    }
}