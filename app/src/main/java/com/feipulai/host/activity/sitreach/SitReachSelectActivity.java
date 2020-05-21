package com.feipulai.host.activity.sitreach;

import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.R;
import com.feipulai.host.activity.main.SubItemsSelectActivity;
import com.feipulai.host.activity.setting.LEDSettingActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.activity.sitreach.more.SitReachMoreActivity;
import com.feipulai.host.activity.standjump.more.StandJumpPairActivity;
import com.feipulai.host.entity.Item;

import butterknife.OnClick;

/**
 * Created by zzs on  2019/6/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachSelectActivity extends SubItemsSelectActivity {

    @Override
    protected void initData() {
        super.initData();
        itemList.clear();
        itemList.add(new Item("有线模式"));
        itemList.add(new Item("无线模式"));
        adapter.notifyDataSetChanged();
        getToolbar().setTitle("坐位体前屈模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                SitReachSetting setting = SharedPrefsUtil.loadFormSource(SitReachSelectActivity.this, SitReachSetting.class);
                setting.setTestType(position);
                if (position == 0) {
                    startActivity(new Intent(SitReachSelectActivity.this,
                            SitReachTestActivity.class));
                } else {
                    startActivity(new Intent(SitReachSelectActivity.this, SitReachMoreActivity.class));
                }
                SharedPrefsUtil.save(SitReachSelectActivity.this, setting);

                finish();
            }
        });
    }


}
