package com.feipulai.exam.activity.volleyball.more_devices;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseCheckActivity;
import com.feipulai.exam.entity.Student;

import butterknife.BindView;

public class BaseVolleyBallMoreActivity extends BaseCheckActivity {
    @BindView(R.id.ll_top)
    RecyclerView ll_top;

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_sargent_jump_more;
    }

    @Override
    protected void initData() {
        super.initData();
        ll_top.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
    }

    @Override
    public void onCheckIn(Student student) {

    }
}
