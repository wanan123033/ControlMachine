package com.feipulai.exam.activity.sport_timer;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

class TimeResultAdapter extends BaseQuickAdapter <SportTimeResult,BaseViewHolder>{
    public TimeResultAdapter(@Nullable List data) {
        super(R.layout.item_time_result, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, SportTimeResult item) {
        helper.setText(R.id.item_txt_round,item.getRound()+"");
        helper.setText(R.id.item_txt_round,item.getRound()+"");
        helper.setText(R.id.item_txt_round,item.getRound()+"");
    }
}
