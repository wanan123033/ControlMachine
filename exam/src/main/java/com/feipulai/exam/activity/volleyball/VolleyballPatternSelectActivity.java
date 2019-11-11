package com.feipulai.exam.activity.volleyball;

import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.volleyball.more_devices.VolleyBallMore2TestActivity;
import com.feipulai.exam.activity.volleyball.more_devices.VolleyBallMoreGroupActivity;
import com.feipulai.exam.activity.volleyball.more_devices.VolleyBallMoreTestActivity;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Item;

/**
 * Created by zzs on  2019/6/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VolleyballPatternSelectActivity extends SubItemsSelectActivity {

    @Override
    protected void initData() {
        super.initData();
        itemList.clear();
        itemList.add(new Item("排球对空垫球"));
        itemList.add(new Item("排球对墙垫球"));
        itemList.add(new Item("排球对空垫球(无线)"));
        itemList.add(new Item("排球对墙垫球(无线)"));
        adapter.notifyDataSetChanged();
        getToolbar().setTitle("排球模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                VolleyBallSetting setting = SharedPrefsUtil.loadFormSource(VolleyballPatternSelectActivity.this, VolleyBallSetting.class);
                setting.setTestPattern(position % 2);
                setting.setType(position >= 2 ? 1 : 0);
                SharedPrefsUtil.save(VolleyballPatternSelectActivity.this, setting);

                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN ) {
                    if (setting.getType() == 1) {
                        startActivity(new Intent(getApplicationContext(), VolleyBallMore2TestActivity.class));
                    } else {
                        startActivity(new Intent(VolleyballPatternSelectActivity.this, TestConfigs.proActivity.get(TestConfigs.sCurrentItem.getMachineCode())));
                    }
                } else {

                    startActivity(new Intent(VolleyballPatternSelectActivity.this, BaseGroupActivity.class));
                }

//                finish();
            }
        });
    }
}
