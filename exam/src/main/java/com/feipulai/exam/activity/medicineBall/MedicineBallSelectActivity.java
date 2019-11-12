package com.feipulai.exam.activity.medicineBall;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseGroupActivity;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.medicineBall.more_device.MedicineBallMoreActivity;
import com.feipulai.exam.activity.sargent_jump.SargentSetting;
import com.feipulai.exam.activity.setting.SettingHelper;
import com.feipulai.exam.activity.setting.SystemSetting;
import com.feipulai.exam.adapter.SubItemsSelectAdapter;
import com.feipulai.exam.entity.Item;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MedicineBallSelectActivity extends BaseTitleActivity {


    @BindView(R.id.rv_subitems)
    RecyclerView rvSubitems;
    private SubItemsSelectAdapter adapter;
    private List<Item> itemList;
    private int selectType = 0;//0 项目选择 1 俯卧撑模式选择
    private Context mContext;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_subitems_select;
    }

    @Override
    protected void initData() {
        mContext = this;
        itemList = new ArrayList<>();
        itemList.add(new Item("有线模式"));
        itemList.add(new Item("无线(一对多)模式"));
        rvSubitems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubItemsSelectAdapter(itemList);
        rvSubitems.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MedicineBallSetting setting = SharedPrefsUtil.loadFormSource(mContext, MedicineBallSetting.class);
                setting.setConnectType(position);
                if (SettingHelper.getSystemSetting().getTestPattern() == SystemSetting.PERSON_PATTERN) {
                    startActivity(new Intent(mContext, position == 1? MedicineBallMoreActivity.class : MedicineBallTestActivity.class));
                } else {
                    startActivity(new Intent(mContext, BaseGroupActivity.class));
                }
                SharedPrefsUtil.save(mContext, setting);
                finish();
            }
        });
    }

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        String title =  "实心球模式选择";
        return builder.setTitle(title) ;
    }
}
