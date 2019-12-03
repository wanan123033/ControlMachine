package com.feipulai.exam.activity.volleyball;

import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.volleyball.more_devices.VolleyBallMoreTestActivity;
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
        itemList.add(new Item("排球对空垫球(无线-1对1)"));
        itemList.add(new Item("排球对墙垫球(无线-1对1)"));
//        itemList.add(new Item("排球对空垫球(无线-1对多)"));
//        itemList.add(new Item("排球对墙垫球(无线-1对多)"));
        adapter.notifyDataSetChanged();
        getToolbar().setTitle("排球模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                VolleyBallSetting setting = SharedPrefsUtil.loadFormSource(VolleyballPatternSelectActivity.this, VolleyBallSetting.class);
                setting.setTestPattern(position % 2);
//                setting.setType(position >= 2 ? 1 : 0);
                if (position == 0 || position == 1) {
                    setting.setType(0);
                } else if (position == 2 || position == 3) {
                    setting.setType(1);
                } else {
                    setting.setType(2);
                }
                SharedPrefsUtil.save(VolleyballPatternSelectActivity.this, setting);
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    if (position == 2 || position == 3 || position == 0 || position == 1) {
                        startActivity(new Intent(VolleyballPatternSelectActivity.this, VolleyBallIndividual2Activity.class));
                    } else {
                        startActivity(new Intent(VolleyballPatternSelectActivity.this, VolleyBallMoreTestActivity.class));
                    }

                } else {
                    startActivity(new Intent(VolleyballPatternSelectActivity.this, BaseGroupActivity.class));
                }

                finish();
            }
        });
    }

    protected void toastSpeak(String paramString) {
    }
}
