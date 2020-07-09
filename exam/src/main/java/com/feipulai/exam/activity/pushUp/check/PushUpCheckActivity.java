package com.feipulai.exam.activity.pushUp.check;

import android.app.Activity;

import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.pushUp.PushUpSettingActivity;
import com.feipulai.exam.activity.pushUp.PushUpIndividualActivity;
import com.feipulai.exam.activity.pushUp.pair.PushUpPairActivity;
import com.feipulai.exam.activity.pushUp.test.PushUpTestActivity;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckActivity;
import com.feipulai.exam.activity.situp.base_check.SitPullUpCheckPresenter;
import com.feipulai.exam.config.BaseEvent;
import com.feipulai.exam.config.EventConfigs;

public class PushUpCheckActivity extends SitPullUpCheckActivity {

    @Override
    protected int setAFRFrameLayoutResID() {
        return R.id.frame_camera;
    }

    @Override
    protected SitPullUpCheckPresenter getPresenter() {
        return new PushUpCheckPresenter(this, this);
    }


    @Override
    public void onEventMainThread(BaseEvent baseEvent) {
        super.onEventMainThread(baseEvent);
        if (baseEvent.getTagInt() == EventConfigs.ITEM_SETTING_UPDATE) {
            PushUpSetting setting = SharedPrefsUtil.loadFormSource(this, PushUpSetting.class);
            if (setting.getTestType() == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() == 1) {
                IntentUtil.gotoActivity(PushUpCheckActivity.this, PushUpIndividualActivity.class);
                finish();
            }
        }
    }

    @Override
    protected Class<?> getProjectSettingActivity() {
        return PushUpSettingActivity.class;
    }

    @Override
    protected Class<? extends Activity> getTestActivity() {
        return PushUpTestActivity.class;
    }

    @Override
    protected Class<? extends Activity> getPairActivity() {
        return PushUpPairActivity.class;
    }

}
