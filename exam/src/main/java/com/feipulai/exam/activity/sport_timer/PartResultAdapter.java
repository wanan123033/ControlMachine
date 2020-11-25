package com.feipulai.exam.activity.sport_timer;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

class PartResultAdapter extends BaseQuickAdapter<SportTimeResult, BaseViewHolder> {
    public PartResultAdapter(@Nullable List data) {
        super(R.layout.item_part_result, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, SportTimeResult item) {
        helper.setText(R.id.tv_rule, item.getResultRule() + "");
        helper.setText(R.id.tv_receive_index, item.getReceiveIndex() + "");
        helper.setText(R.id.tv_part_result, item.getResultRule() + "");
        helper.setText(R.id.tv_remark, item.getRemark() + "");
    }
}
