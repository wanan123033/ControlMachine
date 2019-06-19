package com.feipulai.exam.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.pushUp.PushUpIndividualActivity;
import com.feipulai.exam.activity.pushUp.PushUpSetting;
import com.feipulai.exam.activity.pushUp.check.PushUpCheckActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.SubItemsSelectAdapter;
import com.feipulai.exam.config.SharedPrefsConfigs;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.db.MachineItemCodeUtil;
import com.feipulai.exam.entity.Item;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.view.baseToolbar.BaseToolbar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by zzs on  2019/5/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SubItemsSelectActivity extends BaseTitleActivity {


    @BindView(R.id.rv_subitems)
    RecyclerView rvSubitems;
    private SubItemsSelectAdapter adapter;
    private List<Item> itemList;
    private int selectType = 0;//0 项目选择 1 俯卧撑模式选择

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_subitems_select;
    }

    @Override
    protected void initBundle(Bundle bundle) {
        super.initBundle(bundle);
        if (bundle != null) {
            selectType = bundle.getInt("selectType", 0);
            machineCode = bundle.getInt("machineCode");
        }
    }

    @Override
    protected void initData() {
        if (selectType == 0) {
            toastSpeak("请选择当前考试对应项目");
            itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);

        } else {
            itemList = new ArrayList<>();
            itemList.add(new Item("有线模式"));
            itemList.add(new Item("无线模式"));
        }


        rvSubitems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubItemsSelectAdapter(itemList);
        rvSubitems.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                switch (selectType) {
                    case 0:
                        TestConfigs.sCurrentItem = itemList.get(position);
                        List<RoundResult> roundResults = DBManager.getInstance().queryResultsByItemCodeDefault(machineCode);
                        List<StudentItem> studentItems = DBManager.getInstance().queryStuItemsByItemCodeDefault(machineCode);
                        MachineItemCodeUtil.fillDefaultItemCode(studentItems, roundResults, itemList.get(position).getItemCode());
                        SharedPrefsUtil.putValue(SubItemsSelectActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.MACHINE_CODE, machineCode);
                        SharedPrefsUtil.putValue(SubItemsSelectActivity.this, SharedPrefsConfigs.DEFAULT_PREFS, SharedPrefsConfigs.ITEM_CODE, itemList.get(position).getItemCode());
                        SettingHelper.getSystemSetting().setHostId(1);
                        SettingHelper.updateSettingCache(SettingHelper.getSystemSetting());
                        ActivityCollector.finishAll();
                        startActivity(new Intent(SubItemsSelectActivity.this, MainActivity.class));
                        break;
                    case 1:

                        PushUpSetting setting = SharedPrefsUtil.loadFormSource(SubItemsSelectActivity.this, PushUpSetting.class);
                        setting.setTestType(position);
                        if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                            if ((position == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() == 1) || position == PushUpSetting.WIRED_TYPE) {
                                startActivity(new Intent(SubItemsSelectActivity.this,
                                        PushUpIndividualActivity.class));
                            } else {
                                startActivity(new Intent(SubItemsSelectActivity.this,
                                        PushUpCheckActivity.class));
                            }

                        } else {
                            startActivity(new Intent(SubItemsSelectActivity.this, BaseGroupActivity.class));
                        }
//                        if ((position == PushUpSetting.WIRELESS_TYPE && setting.getDeviceSum() == 1) || position == PushUpSetting.WIRED_TYPE) {
//                            startActivity(new Intent(SubItemsSelectActivity.this,
//                                    SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.GROUP_PATTERN ?
//                                            PushUpGroupActivity.class : PushUpIndividualActivity.class));
//                        } else {
//                            startActivity(new Intent(SubItemsSelectActivity.this,
//                                    PushUpCheckActivity.class));
//                        }


                        SharedPrefsUtil.save(SubItemsSelectActivity.this, setting);
                        break;
                }
                finish();
            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title = selectType == 0 ? "项目选择" : "俯卧撑模式选择";
        return builder.setTitle(title).addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
