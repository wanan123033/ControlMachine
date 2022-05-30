package com.feipulai.exam.activity.footBall.pair;

import android.content.Context;
import android.os.Build;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.device.manager.BallManager;
import com.feipulai.exam.activity.basketball.BasketBallSetting;
import com.feipulai.exam.activity.basketball.pair.BasketBallLinker;
import com.feipulai.exam.activity.basketball.pair.BasketBallPairPresenter;
import com.feipulai.exam.activity.footBall.FootBallSetting;
import com.feipulai.exam.activity.situp.base_pair.SitPullUpPairContract;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public class FootBallPairPresenter extends BasketBallPairPresenter {

    private Context context;
    private FootBallSetting setting;

    public FootBallPairPresenter(Context context, SitPullUpPairContract.View paramView) {
        super(context, paramView);
        this.context = context;
        if (Build.VERSION.SDK_INT >= 19) {
            Objects.requireNonNull(paramView);
        }
        this.setting = SharedPrefsUtil.loadFormSource(context, FootBallSetting.class);
    }



    @Override
    public void changeAutoPair(boolean isAutoPair) {
        setting.setAutoPair(isAutoPair);
    }


    @Override
    protected boolean isAutoPair() {
        return setting.isAutoPair();
    }



    @Override
    public void saveSettings() {
        SharedPrefsUtil.save(context, setting);
        EventBus.getDefault().post(new BaseEvent(EventConfigs.ITEM_SETTING_UPDATE));
    }

    public FootBallSetting getFootSetting() {
        return setting;
    }
}
