package com.feipulai.host.activity.standjump;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.activity.main.SubItemsSelectActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.standjump.Freedom.StandJumpFreedomActivity;
import com.feipulai.host.activity.standjump.Freedom.StandJumpRadioFreedomActivity;
import com.feipulai.host.activity.standjump.more.StandJumpMoreActivity;
import com.feipulai.host.entity.Item;

public class StandJumpSelectActivity
        extends SubItemsSelectActivity {
    protected void initData() {
        super.initData();
        this.itemList.clear();
        this.itemList.add(new Item("有线模式"));
        this.itemList.add(new Item("无线模式"));
        this.adapter.notifyDataSetChanged();
        getToolbar().setTitle("立定跳远模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                StandJumpSetting setting = SharedPrefsUtil.loadFormSource(StandJumpSelectActivity.this, StandJumpSetting.class);
                setting.setTestType(position);
                if (position == 0) {
                    if (SettingHelper.getSystemSetting().isFreedomTest()) {
                        IntentUtil.gotoActivity(StandJumpSelectActivity.this, StandJumpFreedomActivity.class);
                    } else {
                        IntentUtil.gotoActivity(StandJumpSelectActivity.this, StandJumpTestActivity.class);
                    }

                } else {
                    if (SettingHelper.getSystemSetting().isFreedomTest()) {
                        IntentUtil.gotoActivity(StandJumpSelectActivity.this, StandJumpRadioFreedomActivity.class);
                    } else {
                        IntentUtil.gotoActivity(StandJumpSelectActivity.this, StandJumpMoreActivity.class);
                    }

                }
                SharedPrefsUtil.save(StandJumpSelectActivity.this, setting);
                StandJumpSelectActivity.this.finish();
            }
        });
    }

    protected void toastSpeak(String paramString) {
    }
}
