package com.feipulai.exam.activity.basketball;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.basketball.motion.BasketBallMotionTestActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.entity.Item;
import com.orhanobut.logger.utils.LogUtils;

public class BasketBallSelectActivity
        extends SubItemsSelectActivity {
    protected void initData() {
        super.initData();
        this.itemList.clear();
        this.itemList.add(new Item("有线运球模式"));
        this.itemList.add(new Item("无线运球模式（V6.4）"));
        this.itemList.add(new Item("往返运球投篮模式"));
        this.itemList.add(new Item("无线运球模式（V6.6）"));
        this.itemList.add(new Item("运动计时模式"));
//        this.itemList.add(new Item("投篮模式"));
        this.adapter.notifyDataSetChanged();
        getToolbar().setTitle("篮球模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BasketBallSetting setting = SharedPrefsUtil.loadFormSource(BasketBallSelectActivity.this, BasketBallSetting.class);
                if (position == 3) {
                    setting.setTestType(1);
                    setting.setDeviceVersion(1);
                } else{
                    setting.setTestType(position);
                }
                ShootSetting shootSetting = SharedPrefsUtil.loadFormSource(BasketBallSelectActivity.this, ShootSetting.class);
                shootSetting.setTestType(position);
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    switch (position) {
                        case 0:
                        case 1:
                        case 3:
                            LogUtils.operation("跳转:篮球有线无线运球模式");
                            IntentUtil.gotoActivity(BasketBallSelectActivity.this, BasketballIndividualActivity.class);
                            break;
                        case 2:
                            LogUtils.operation("跳转:篮球往返运球投篮模式");
                            IntentUtil.gotoActivity(BasketBallSelectActivity.this, DribbleShootActivity.class);
                            break;
                        case 4:
                            IntentUtil.gotoActivity(BasketBallSelectActivity.this, BasketBallMotionTestActivity.class);
                            break;
//                        case 3:
//                            LogUtils.operation("跳转:篮球投篮模式");
//                            IntentUtil.gotoActivity(BasketBallSelectActivity.this, BasketBallShootActivity.class);
//                            break;
                    }

                } else {
                    IntentUtil.gotoActivity(BasketBallSelectActivity.this, BaseGroupActivity.class);
                }
                SharedPrefsUtil.save(BasketBallSelectActivity.this, setting);
                BasketBallSelectActivity.this.finish();
            }
        });
    }

    @Override
    protected void toastSpeak(String paramString) {
    }
}

