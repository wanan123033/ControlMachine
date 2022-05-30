package com.feipulai.exam.activity.basketball.pair;

import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.led.LEDManager;
import com.feipulai.device.serial.beans.Basketball868Result;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
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

public class BasketBallPairActivity
        extends SitPullPairActivity implements CompoundButton.OnCheckedChangeListener {
    private int hostId;
    private LEDManager mLEDManager;
    private BasketBallSetting setting;

    public SitPullUpPairPresenter getPresenter() {
        return new BasketBallPairPresenter(this, this);
    }

    private List<StuDevicePair> pairs;
    @BindView(R.id.sw_currency_state)
    public Switch swCurrencyState;

    @Override
    public void initView(boolean isAutoPair, List pairs) {
        super.initView(isAutoPair, pairs);
        this.pairs = pairs;
        this.mRvPairs.setLayoutManager(new GridLayoutManager(this, 1));
        this.mAdapter = new BallPairAdapter(this, pairs);
        this.mRvPairs.setAdapter(this.mAdapter);
        this.mRvPairs.setClickable(true);
        this.mAdapter.setOnItemClickListener(this);
        swCurrencyState.setOnCheckedChangeListener(this);

        hostId = SettingHelper.getSystemSetting().getHostId();
        mLEDManager = new LEDManager(LEDManager.LED_VERSION_4_8);

        initSetting();
    }

    public void initSetting() {
        SitPullUpPairPresenter presenter = getPresenter();
        if (presenter instanceof BasketBallPairPresenter) {
            setting = ((BasketBallPairPresenter) presenter).getSetting();
        }
        if (setting != null) {
            swCurrencyState.setChecked(setting.getUseLedType() == 1);
        }
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_radio_pair_currency;
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
                case 0://子机
                case 1://子机
                    return 3;
                case 2://LED
                    return 2;
            }

        }
        return 0;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setting.setUseLedType(isChecked ? 1 : 0);
    }

    @OnClick(R.id.tv_currency_connect)
    public void onClick(View view) {
        String title = TestConfigs.machineNameMap.get(machineCode)
                + " " + SettingHelper.getSystemSetting().getHostId();
        mLEDManager.link(SettingHelper.getSystemSetting().getUseChannel(), TestConfigs.sCurrentItem.getMachineCode(), hostId, 1);

        mLEDManager.showSubsetString(hostId, 1, title, 0, true, false, LEDManager.MIDDLE, 1);
        mLEDManager.showSubsetString(hostId, 1, "菲普莱体育", 3, 3, false, true, 1);

    }
}