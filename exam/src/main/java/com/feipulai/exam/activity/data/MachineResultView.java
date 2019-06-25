package com.feipulai.exam.activity.data;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.data.adapter.DataMacResultAdapter;
import com.feipulai.exam.entity.MachineResult;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 机器成绩显示View
 * Created by zzs on  2019/6/25
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MachineResultView extends LinearLayout {

    @BindView(R.id.rv_macResult)
    RecyclerView rvMacResult;
    private Context mContext;

    public MachineResultView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MachineResultView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_machine_result, null);
        ButterKnife.bind(this, view);
        addView(view);
        rvMacResult.setLayoutManager(new LinearLayoutManager(mContext));


    }

    public void setData(List<MachineResult> machineResultList) {
        rvMacResult.setAdapter(new DataMacResultAdapter(machineResultList));
    }
}
