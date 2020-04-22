package com.feipulai.exam.activity.sitreach;

import android.content.Intent;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.LEDSettingActivity;
import com.feipulai.exam.activity.SubItemsSelectActivity;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.pushUp.PushUpIndividualActivity;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.pushUp.check.PushUpCheckActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.activity.sitreach.more_device.SitReachMoreActivity;
import com.feipulai.exam.activity.standjump.more.StandJumpPairActivity;
import com.feipulai.exam.entity.Item;

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
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    if ( position == PushUpSetting.WIRED_TYPE) {
                        startActivity(new Intent(SitReachSelectActivity.this,
                                SitReachTestActivity.class));
                    } else {
                        startActivity(new Intent(SitReachSelectActivity.this,
                                SitReachMoreActivity.class));
                    }

                } else {
                    startActivity(new Intent(SitReachSelectActivity.this, BaseGroupActivity.class));
                }
                SharedPrefsUtil.save(SitReachSelectActivity.this, setting);
                finish();
            }
        });
    }

}
