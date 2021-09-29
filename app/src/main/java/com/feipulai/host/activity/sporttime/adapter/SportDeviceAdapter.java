package com.feipulai.host.activity.sporttime.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.host.R;
import com.feipulai.host.activity.sporttime.bean.DeviceState;

import java.util.List;

public class SportDeviceAdapter extends BaseQuickAdapter <DeviceState,BaseViewHolder>{
    public SportDeviceAdapter(@Nullable List data) {
        super(R.layout.item_device_title, data);
    }


    @Override
    protected void convert(BaseViewHolder helper, DeviceState item) {
        helper.setText(R.id.device_title,item.getDeviceId()+"");
        helper.setText(R.id.title,item.getDeviceState() == 0?"X" : "√");
        helper.setTextColor(R.id.title,item.getDeviceState() == 0? Color.RED:Color.YELLOW);
    }
}
