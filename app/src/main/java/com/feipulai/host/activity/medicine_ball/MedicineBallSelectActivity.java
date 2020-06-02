package com.feipulai.host.activity.medicine_ball;

import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.host.activity.main.SubItemsSelectActivity;
import com.feipulai.host.activity.sitreach.SitReachSetting;
import com.feipulai.host.activity.sitreach.SitReachTestActivity;
import com.feipulai.host.activity.sitreach.more.SitReachMoreActivity;
import com.feipulai.host.entity.Item;

/**
 * Created by zzs on  2019/6/27
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MedicineBallSelectActivity extends SubItemsSelectActivity {

    @Override
    protected void initData() {
        super.initData();
        itemList.clear();
        itemList.add(new Item("有线模式"));
        itemList.add(new Item("无线模式"));
        adapter.notifyDataSetChanged();
        getToolbar().setTitle("红外实心球模式选择");
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MedicineBallSetting setting = SharedPrefsUtil.loadFormSource(MedicineBallSelectActivity.this, MedicineBallSetting.class);
                setting.setTestType(position);
                if (position == 0) {
                    startActivity(new Intent(MedicineBallSelectActivity.this,
                            MedicineBallTestActivity.class));
                } else {
                    startActivity(new Intent(MedicineBallSelectActivity.this, SitReachMoreActivity.class));
                }
                SharedPrefsUtil.save(MedicineBallSelectActivity.this, setting);

                finish();
            }
        });
    }


}
