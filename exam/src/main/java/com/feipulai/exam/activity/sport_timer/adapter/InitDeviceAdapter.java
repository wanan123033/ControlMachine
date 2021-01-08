package com.feipulai.exam.activity.sport_timer.adapter;

import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

public class InitDeviceAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public InitDeviceAdapter(@Nullable List<String> data) {
        super(R.layout.item_route_device, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.device_title, item);
    }
}
