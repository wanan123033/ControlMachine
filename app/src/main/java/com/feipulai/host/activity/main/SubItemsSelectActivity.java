package com.feipulai.host.activity.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.adapter.SubItemsSelectAdapter;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.db.MachineItemCodeUtil;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.RoundResult;
import com.feipulai.host.entity.StudentItem;

import java.util.List;

import butterknife.BindView;

/**
 * Created by zzs on  2019/5/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SubItemsSelectActivity extends BaseTitleActivity {


    @BindView(R.id.rv_subitems)
    RecyclerView rvSubitems;
    protected SubItemsSelectAdapter adapter;
    protected List<Item> itemList;
    private boolean isFreedom;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_subitems_select;
    }

    @Override
    protected void initBundle(Bundle bundle) {
        super.initBundle(bundle);
        if (bundle != null) {
            machineCode = bundle.getInt("machineCode");
            isFreedom = bundle.getBoolean("freedomTest", false);
        }
    }

    @Override
    protected void initData() {
        toastSpeak(getString(R.string.please_select_test_item));
        itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);

        rvSubitems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubItemsSelectAdapter(itemList);
        rvSubitems.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                TestConfigs.sCurrentItem = itemList.get(position);
                List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault(machineCode);
                List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault(machineCode);
                MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, itemList.get(position).getItemCode());
                SharedPrefsUtil.putValue(SubItemsSelectActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
                SharedPrefsUtil.putValue(SubItemsSelectActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, itemList.get(position).getItemCode());
                SettingHelper.getSystemSetting().setHostId(1);
                SettingHelper.getSystemSetting().setFreedomTest(isFreedom);
                SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
//                ActivityCollector.getInstance().finishAllActivity();
//                startActivity(new Intent(SubItemsSelectActivity.this, MainActivity.class));
//                finish();
                ActivityCollector.getInstance().finishAllActivityExcept(MainActivity.class);
                finish();
            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle(R.string.sub_item_select_title);
    }

}
