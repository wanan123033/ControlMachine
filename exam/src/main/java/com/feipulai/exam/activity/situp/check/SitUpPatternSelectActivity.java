package com.feipulai.exam.activity.situp.check;

import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.situp.newSitUp.SitUpArmCheckActivity;
import com.feipulai.exam.activity.situp.setting.SitUpSetting;
import com.feipulai.exam.entity.Item;

/**
 * Created by zzs on  2019/6/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitUpPatternSelectActivity extends SubItemsSelectActivity {

    @Override
    protected void initData() {
        super.initData();
        itemList.clear();
        itemList.add(new Item("模式一:腰带感应"));
        itemList.add(new Item("模式二:腰带感应+肩胛感应"));
        adapter.notifyDataSetChanged();
        getToolbar().setTitle("仰卧起坐模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                SitUpSetting setting = SharedPrefsUtil.loadFormSource(SitUpPatternSelectActivity.this, SitUpSetting.class);
                setting.setTestType(position);
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    if (position == 0) {
                        IntentUtil.gotoActivity(SitUpPatternSelectActivity.this, SitUpCheckActivity.class);
                    } else {
                        IntentUtil.gotoActivity(SitUpPatternSelectActivity.this, SitUpArmCheckActivity.class);
                    }
                } else {
                    startActivity(new Intent(SitUpPatternSelectActivity.this, BaseGroupActivity.class));
                }
                SharedPrefsUtil.save(SitUpPatternSelectActivity.this, setting);
                finish();
            }
        });
    }

    @Override
    protected void toastSpeak(String msg) {

    }
}
