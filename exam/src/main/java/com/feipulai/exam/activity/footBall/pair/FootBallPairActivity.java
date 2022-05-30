package com.feipulai.exam.activity.footBall.pair;

import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.pair.BallPairAdapter;
import com.feipulai.exam.activity.basketball.pair.BasketBallPairActivity;
import com.feipulai.exam.activity.footBall.FootBallSetting;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class FootBallPairActivity
        extends BasketBallPairActivity implements CompoundButton.OnCheckedChangeListener {

    private FootBallSetting setting;

    public SitPullUpPairPresenter getPresenter() {
        return new FootBallPairPresenter(this, this);
    }


    @Override
    public void initSetting() {
        SitPullUpPairPresenter presenter = getPresenter();
        if (presenter instanceof FootBallPairPresenter) {
            setting = ((FootBallPairPresenter) presenter).getFootSetting();
        }
        swCurrencyState.setChecked(setting.getUseLedType() == 1);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setting.setUseLedType(isChecked ? 1 : 0);
    }

}