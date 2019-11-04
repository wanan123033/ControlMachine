package com.feipulai.exam.activity.basketball;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.entity.Item;

public class BasketBallSelectActivity
        extends SubItemsSelectActivity
{
    protected void initData()
    {
        super.initData();
        this.itemList.clear();
        this.itemList.add(new Item("有线模式"));
        this.itemList.add(new Item("无线模式"));
        this.adapter.notifyDataSetChanged();
        getToolbar().setTitle("篮球运球模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                BasketBallSetting setting =  SharedPrefsUtil.loadFormSource(BasketBallSelectActivity.this, BasketBallSetting.class);
                setting.setTestType(position);
                if (SettingHelper.getSystemSetting().getTestPattern() == 0) {
                    IntentUtil.gotoActivity(BasketBallSelectActivity.this, BasketballIndividualActivity.class);
                }else{
                    IntentUtil.gotoActivity(BasketBallSelectActivity.this, BaseGroupActivity.class);
                }
                SharedPrefsUtil.save(BasketBallSelectActivity.this, setting);
                BasketBallSelectActivity.this.finish();
            }
        });
    }

    protected void toastSpeak(String paramString) {}
}
