package com.feipulai.exam.activity.footBall.motion;

import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.feipulai.device.led.LEDManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.motion.BaskBallMotionPairPresenter;
import com.feipulai.exam.activity.basketball.reentry.BallReentryPairAdapter;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class FootballMotionPairActivity extends SitPullPairActivity implements CompoundButton.OnCheckedChangeListener {
    private LEDManager mLEDManager = new LEDManager(LEDManager.LED_VERSION_4_8);

    @BindView(R.id.sw_currency_state)
    Switch swCurrencyState;

    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new FootballMotionPairPresenter(this, this);
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
                    this.pairs.get(i).getBaseDevice().setDeviceName("远红外");
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
        if (presenter instanceof FootballMotionPairPresenter) {
            int usbLedType = ((FootballMotionPairPresenter) presenter).getUsbLedType();
            swCurrencyState.setChecked(usbLedType == 1);
        }
        swCurrencyState.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (presenter instanceof FootballMotionPairPresenter) {
            ((FootballMotionPairPresenter) presenter).setUsbLedType(isChecked ? 1 : 0);
        }
    }

    @OnClick(R.id.tv_currency_connect)
    public void onClick(View view) {
        String title = TestConfigs.machineNameMap.get(machineCode)
                + " " + SettingHelper.getSystemSetting().getHostId();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), SettingHelper.getSystemSetting().getHostId(), 1);

        mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, title, 0, true, false, LEDManager.MIDDLE, 1);
        mLEDManager.showSubsetString(SettingHelper.getSystemSetting().getHostId(), 1, "菲普莱体育", 3, 3, false, true, 1);

    }
}
