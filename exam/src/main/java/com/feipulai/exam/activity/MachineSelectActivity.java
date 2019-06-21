package com.feipulai.exam.activity;

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
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.TupleAdapter;
import com.feipulai.exam.bean.Tuple;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;

import java.util.ArrayList;
import java.util.List;

public class MachineSelectActivity extends BaseTitleActivity
        implements DialogInterface.OnClickListener {

    private List<Tuple> mTupleList = new ArrayList<>();
    private SystemSetting systemSetting;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_machine_select;
    }

    @Override
    protected void initData() {
        systemSetting = SettingHelper.getSystemSetting();

        mTupleList.add(new Tuple(ItemDefault.CODE_TS, "跳绳", R.mipmap.icon_jump_rope, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_YWQZ, "仰卧起坐", R.mipmap.icon_situp, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_LDTY, "立定跳远", R.mipmap.icon_standjump, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_ZWTQQ, "坐位体前屈", R.mipmap.icon_sitreach, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_ZFP, "红外计时", R.mipmap.icon_runtime, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_HWSXQ, "红外实心球", R.mipmap.icon_medicine_ball, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_YTXS, "引体向上", R.mipmap.icon_pullup, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_PQ, "排球垫球", R.mipmap.icon_volleyball, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_MG, "摸高", R.mipmap.mogao, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_FWC, "俯卧撑", R.mipmap.icon_fwc, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_ZCP, "中长跑", R.mipmap.ic_launcher, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_LQYQ, "篮球运球", R.mipmap.ic_launcher, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_ZQYQ, "足球运球", R.mipmap.ic_launcher, 3));
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
        if (itemList.size() == 1) {
            int init = TestConfigs.init(this, machineCode, null, this);
            if (init == TestConfigs.INIT_SUCCESS) {
                systemSetting.setHostId(1);
                SharedPrefsUtil.save(MachineSelectActivity.this, systemSetting);
                ActivityCollector.finishAll();
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
        SharedPrefsUtil.save(MachineSelectActivity.this, systemSetting);
        ActivityCollector.finishAll();
        startActivity(new Intent(this, MainActivity.class));
    }

}
