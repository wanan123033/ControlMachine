package com.feipulai.exam.activity;

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
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.adapter.SubItemsSelectAdapter;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.db.MachineItemCodeUtil;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.MachineResult;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.StudentItem;

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

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_subitems_select;
    }

    @Override
    protected void initBundle(Bundle bundle) {
        super.initBundle(bundle);
        if (bundle != null) {
            machineCode = bundle.getInt("machineCode");
        }
    }

    @Override
    protected void initData() {
        toastSpeak("请选择当前考试对应项目");
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
                List<MachineResult> machineResults = DBManager.getInstance().queryMachineResultByItemCodeDefault(machineCode);
                MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, machineResults, itemList.get(position).getItemCode());
                SharedPrefsUtil.putValue(SubItemsSelectActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
                SharedPrefsUtil.putValue(SubItemsSelectActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, itemList.get(position).getItemCode());
                SettingHelper.getSystemSetting().setHostId(1);
                SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
                ActivityCollector.getInstance().finishAllActivityExcept(MainActivity.class);
//                startActivity(new Intent(SubItemsSelectActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("项目选择");
    }

}
