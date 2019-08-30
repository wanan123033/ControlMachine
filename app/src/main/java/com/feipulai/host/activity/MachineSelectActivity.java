package com.feipulai.host.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.adapter.TupleAdapter;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Item;
import com.feipulai.host.entity.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2017/11/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MachineSelectActivity extends BaseTitleActivity implements DialogInterface.OnClickListener {

    private List<Tuple> mTupleList = new ArrayList<>();
    private SystemSetting systemSetting = SettingHelper.getSystemSetting();

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_machine_select;
    }

    @Override
    protected void initData() {
        mTupleList.add(new Tuple(ItemDefault.CODE_HW, "身高体重", R.mipmap.icon_jump_rope, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_TS, "跳绳", R.mipmap.icon_jump_rope, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_LDTY, "立定跳远", R.mipmap.icon_standjump, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_FHL, "肺活量", R.mipmap.icon_standjump, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_ZWTQQ, "坐位体前屈", R.mipmap.icon_sitreach, 4));
        mTupleList.add(new Tuple(ItemDefault.CODE_HWSXQ, "红外实心球", R.mipmap.icon_medicine_ball, 4));
        mTupleList.add(new Tuple(ItemDefault.CODE_YWQZ, "仰卧起坐", R.mipmap.icon_situp, 4));


        RecyclerView recyclerView = findViewById(R.id.rv_item);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 12);
        recyclerView.setLayoutManager(layoutManager);
        TupleAdapter adapter = new TupleAdapter(mTupleList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                setCurrentItem(mTupleList.get(position).getMachineCode());
            }
        });
        adapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                return mTupleList.get(position).getSpanSize();
            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备切换").addLeftText("返回", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void setCurrentItem(int machineCode) {
        List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);
        if (itemList.size() == 1 || machineCode == ItemDefault.CODE_HW) {
            int init = TestConfigs.init(this, machineCode, null, this);
            if (init == TestConfigs.INIT_SUCCESS) {
                systemSetting.setHostId(1);
                SettingHelper.updateSettingCache(systemSetting);
                // 清除所有已启动的Activity
                ActivityCollector.getInstance().finishAllActivity();
                startActivity(new Intent(this, MainActivity.class));
            }
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("machineCode", machineCode);
            IntentUtil.gotoActivity(this, SubItemsSelectActivity.class, bundle);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        systemSetting.setHostId(1);
        SettingHelper.updateSettingCache(systemSetting);
        // 清除所有已启动的Activity
        ActivityCollector.getInstance().finishAllActivity();
        startActivity(new Intent(this, MainActivity.class));
    }

}
