package com.feipulai.exam.activity.basketball.motion;

import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.led.LEDManager;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.reentry.BallReentryPairAdapter;
import com.feipulai.exam.activity.basketball.reentry.BallReentryPairPresenter;
import com.feipulai.exam.activity.jump_rope.bean.StuDevicePair;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.situp.base_pair.SitPullPairActivity;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairPresenter;
import com.feipulai.exam.config.TestConfigs;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BasketMotionPairActivity extends SitPullPairActivity implements CompoundButton.OnCheckedChangeListener {

    private int hostId;
    private LEDManager mLEDManager;

    @BindView(R.id.sw_currency_state)
    Switch swCurrencyState;
    @Override
    public SitPullUpPairPresenter getPresenter() {
        return new BaskBallMotionPairPresenter(this, this);
    }

    private List<StuDevicePair> pairs;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_ball_reentry_pair;
    }

    @Override
    public void initView(boolean isAutoPair, List pairs) {
        super.initView(isAutoPair, pairs);
        hostId = SettingHelper.getSystemSetting().getHostId();
        mLEDManager = new LEDManager(LEDManager.LED_VERSION_4_8);
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
                    this.pairs.get(i).getBaseDevice().setDeviceName("计时屏");
                    break;
            }
        }
        this.mRvPairs.setLayoutManager(new GridLayoutManager(this, 1));
        this.mAdapter = new BallReentryPairAdapter(this, pairs);
        this.mRvPairs.setAdapter(this.mAdapter);
        this.mRvPairs.setClickable(true);
        this.mAdapter.setOnItemClickListener(this);
        if (presenter instanceof BaskBallMotionPairPresenter){
            int usbLedType = ((BaskBallMotionPairPresenter) presenter).getUsbLedType();
            swCurrencyState.setChecked(usbLedType == 1);
        }
        swCurrencyState.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (presenter instanceof BaskBallMotionPairPresenter){
            ((BaskBallMotionPairPresenter) presenter).setUsbLedType(isChecked?1:0);
        }
    }
    @OnClick(R.id.tv_currency_connect)
    public void onClick(View view){
        String title = TestConfigs.machineNameMap.get(machineCode)
                + " " + SettingHelper.getSystemSetting().getHostId();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), hostId, 1);

        mLEDManager.showSubsetString(hostId, 1, title, 0, true, false, LEDManager.MIDDLE, 1);
        mLEDManager.showSubsetString(hostId, 1, "菲普莱体育", 3, 3, false, true, 1);

    }
}
