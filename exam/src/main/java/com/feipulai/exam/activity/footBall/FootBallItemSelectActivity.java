package com.feipulai.exam.activity.footBall;


import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
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
        this.itemList.add(new Item("无线模式"));
        this.adapter.notifyDataSetChanged();
        getToolbar().setTitle("足球运球模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FootBallSetting setting = SharedPrefsUtil.loadFormSource(FootBallItemSelectActivity.this,FootBallSetting.class);
                setting.setTestType(position);
                SharedPrefsUtil.save(FootBallItemSelectActivity.this,setting);
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    LogUtils.operation("跳转至FootballIndividualActivity");
                    IntentUtil.gotoActivity(FootBallItemSelectActivity.this, FootballIndividualActivity.class);
                } else {
                    LogUtils.operation("跳转至BaseGroupActivity");
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
