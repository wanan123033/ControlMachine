package com.feipulai.host.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ActivityCollector;
import com.feipulai.device.ic.utils.ItemDefault;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseActivity;
import com.feipulai.host.adapter.TupleAdapter;
import com.feipulai.host.config.SharedPrefsConfigs;
import com.feipulai.host.config.TestConfigs;
import com.feipulai.host.entity.Tuple;
import com.feipulai.host.utils.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by James on 2017/11/22.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 *
 */
public class MachineSelectActivity extends BaseActivity implements DialogInterface.OnClickListener {

    private List<Tuple> mTupleList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_select);

        setTitle("测试机器选择");
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mTupleList.add(new Tuple(ItemDefault.CODE_HW, "身高体重"));
        mTupleList.add(new Tuple(ItemDefault.CODE_TS, "跳绳"));
        mTupleList.add(new Tuple(ItemDefault.CODE_LDTY, "立定跳远"));
        mTupleList.add(new Tuple(ItemDefault.CODE_FHL, "肺活量"));
        mTupleList.add(new Tuple(ItemDefault.CODE_ZWTQQ, "坐位体前屈"));
        mTupleList.add(new Tuple(ItemDefault.CODE_HWSXQ, "红外实心球"));
        mTupleList.add(new Tuple(ItemDefault.CODE_YWQZ, "仰卧起坐"));

        RecyclerView recyclerView = findViewById(R.id.rv_item);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);
        TupleAdapter adapter = new TupleAdapter(mTupleList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                setCurrentItem(mTupleList.get(position).getMachineCode());
            }
        });
    }

    private void setCurrentItem(int machineCode) {
        int init = TestConfigs.init(this, machineCode, null,this);
        if (init == TestConfigs.INIT_SUCCESS) {
            // 清除所有已启动的Activity
            ActivityCollector.finishAll();
            startActivity(new Intent(this, MainActivity.class));
            SharedPrefsUtil.putValue(this, SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.HOST_ID,1);
        }
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        // 清除所有已启动的Activity
        ActivityCollector.finishAll();
        startActivity(new Intent(this, MainActivity.class));
        SharedPrefsUtil.putValue(this, SharedPrefsConfigs.DEFAULT_PREFS,SharedPrefsConfigs.HOST_ID,1);
    }
    
}
