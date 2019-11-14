package com.feipulai.exam.activity.standjump;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.standjump.more.StandJumpMoreActivity;
import com.feipulai.exam.entity.Item;

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
                if (SettingHelper.getSystemSetting().getTestPattern() == 0) {
                    if (position == 0) {
                        IntentUtil.gotoActivity(StandJumpSelectActivity.this, StandJumpTestActivity.class);
                    } else {
                        IntentUtil.gotoActivity(StandJumpSelectActivity.this, StandJumpMoreActivity.class);
                    }

                } else {
                    IntentUtil.gotoActivity(StandJumpSelectActivity.this, BaseGroupActivity.class);
                }
                SharedPrefsUtil.save(StandJumpSelectActivity.this, setting);
                StandJumpSelectActivity.this.finish();
            }
        });
    }

    protected void toastSpeak(String paramString) {
    }
}
