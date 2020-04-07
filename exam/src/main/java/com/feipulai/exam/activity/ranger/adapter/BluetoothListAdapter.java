package com.feipulai.exam.activity.ranger.adapter;


import android.bluetooth.BluetoothDevice;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BluetoothListAdapter extends BaseQuickAdapter<BluetoothDevice,BluetoothListAdapter.ViewHolder> {

    public BluetoothListAdapter(@Nullable List<BluetoothDevice> data) {
        super(R.layout.item_bluetooth,data);
    }

    @Override
    protected void convert(ViewHolder viewHolder, BluetoothDevice bluetoothDevice) {
        viewHolder.tv_mac.setText(bluetoothDevice.getAddress());
        viewHolder.tv_name.setText(bluetoothDevice.getName());
    }

    static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.tv_name)
        TextView tv_name;
        @BindView(R.id.tv_mac)
        TextView tv_mac;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }
}
