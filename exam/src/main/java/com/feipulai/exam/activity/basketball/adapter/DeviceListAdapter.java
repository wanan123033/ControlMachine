package com.feipulai.exam.activity.basketball.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;
import com.feipulai.exam.bean.DeviceDetail;

import java.util.List;

public class DeviceListAdapter extends BaseQuickAdapter<DeviceDetail, DeviceListAdapter.ViewHolder> {

    public DeviceListAdapter(@Nullable List<DeviceDetail> data) {
        super(R.layout.rv_stu_dev_item, data);
    }

    @Override
    protected void convert(ViewHolder viewHolder, DeviceDetail deviceDetail) {

    }

    public static class ViewHolder extends BaseViewHolder {

        public ViewHolder(View view) {
            super(view);
        }
    }
}
