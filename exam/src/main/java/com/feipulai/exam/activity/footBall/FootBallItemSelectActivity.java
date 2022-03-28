package com.feipulai.exam.activity.footBall;


import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.basketball.BasketBallSelectActivity;
import com.feipulai.exam.activity.basketball.reentry.BallReentryActivity;
import com.feipulai.exam.activity.footBall.motion.FootballMotionActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.entity.Item;
import com.orhanobut.logger.utils.LogUtils;

public class FootBallItemSelectActivity extends SubItemsSelectActivity {

    @Override
    protected void initData() {

        super.initData();
        this.itemList.clear();
        this.itemList.add(new Item("有线模式"));
        this.itemList.add(new Item("无线运球模式（V6.4）"));
        this.itemList.add(new Item("无线运球模式（V6.6）"));
        this.itemList.add(new Item("运动计时模式"));
        this.adapter.notifyDataSetChanged();
        getToolbar().setTitle("足球运球模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FootBallSetting setting = SharedPrefsUtil.loadFormSource(FootBallItemSelectActivity.this, FootBallSetting.class);
                if (position == 2) {
                    setting.setTestType(1);
                    setting.setDeviceVersion(1);
                } else {
                    setting.setTestType(position);
                }

                SharedPrefsUtil.save(FootBallItemSelectActivity.this, setting);
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    switch (position) {
                        case 0:
                        case 1:
                        case 2:
                            IntentUtil.gotoActivity(FootBallItemSelectActivity.this, FootballIndividualActivity.class);
                            break;
                        case 3:
                            setting.setDeviceVersion(1);
                            setting.setTestType(position);
                            IntentUtil.gotoActivity(FootBallItemSelectActivity.this, FootballMotionActivity.class);
                            break;
                    }

                } else {
                    IntentUtil.gotoActivity(FootBallItemSelectActivity.this, BaseGroupActivity.class);
                }
                finish();
            }
        });
    }

    @Override
    protected void toastSpeak(String msg) {

    }
}
