package com.feipulai.host.activity.main.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.common.utils.IntentUtil;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseFragment;
import com.feipulai.host.activity.main.MachineSelectActivity;
import com.feipulai.host.activity.main.MainActivity;
import com.feipulai.host.activity.main.SubItemsSelectActivity;
import com.feipulai.host.activity.setting.SettingHelper;
import com.feipulai.host.activity.setting.SystemSetting;
import com.feipulai.host.adapter.TupleAdapter;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.db.DBManager;
import com.feipulai.host.entity.Item;
import com.feipulai.host.bean.Tuple;

import java.util.ArrayList;
import java.util.List;

/**
 * 体测自由测试选择
 * Created by zzs on  2019/9/24
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class FreedomSelectFragment extends BaseFragment implements DialogInterface.OnClickListener {
    private List<Tuple> mTupleList = new ArrayList<>();
    private SystemSetting systemSetting = SettingHelper.getSystemSetting();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_machine_select;
    }

    @Override
    protected void initData() {
        mTupleList.add(new Tuple(ItemDefault.CODE_LDTY, getString(R.string.stand_jump), R.mipmap.icon_standjump, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_HWSXQ, getString(R.string.medicine_ball), R.mipmap.icon_medicine_ball, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_ZFP, getString(R.string.run_time), R.mipmap.icon_runtime, 3));
        mTupleList.add(new Tuple(ItemDefault.CODE_SL, "视力", R.mipmap.icon_sl, 3));
//        mTupleList.add(new Tuple(ItemDefault.CODE_JGCJ, getString(R.string.run_ranger), R.mipmap.icon_jgcj, 3));


        RecyclerView recyclerView = findView(R.id.rv_item);
        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 12);
        recyclerView.setLayoutManager(layoutManager);
        TupleAdapter adapter = new TupleAdapter(mTupleList,true);
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

    private void setCurrentItem(int machineCode) {
        List<Item> itemList = DBManager.getInstance().queryItemsByMachineCode(machineCode);
        if (itemList.size() == 1 || machineCode == ItemDefault.CODE_HW) {
            int init = TestConfigs.init(mContext, machineCode, null, this);
            if (init == TestConfigs.INIT_SUCCESS) {
                systemSetting.setHostId(1);
                systemSetting.setFreedomTest(true);
                SettingHelper.updateSettingCache(systemSetting);
                // 清除所有已启动的Activity
//                ActivityCollector.getInstance().finishAllActivity();
//                startActivity(new Intent(this, MainActivity.class));
                ActivityCollector.getInstance().finishAllActivityExcept(MainActivity.class);
                ActivityCollector.getInstance().finishActivity(MachineSelectActivity.class);
            }
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("machineCode", machineCode);
            bundle.putBoolean("freedomTest", true);
            IntentUtil.gotoActivity(mContext, SubItemsSelectActivity.class, bundle);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        systemSetting.setHostId(1);
        systemSetting.setFreedomTest(true);
        SettingHelper.updateSettingCache(systemSetting);
        // 清除所有已启动的Activity
        ActivityCollector.getInstance().finishAllActivity();
        startActivity(new Intent(mContext, MainActivity.class));
    }
}
