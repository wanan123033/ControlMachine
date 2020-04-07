package com.feipulai.exam.activity.ranger.adapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.feipulai.exam.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BluetoothListAdapter extends BaseAdapter {

    private List<BluetoothDevice> data;
    private Context context;

    public BluetoothListAdapter(@Nullable List<BluetoothDevice> data, Context context) {
        this.data = data;
        this.context = context;

    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_bluetooth,parent,false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice device = getItem(position);
        holder.tv_name.setText(device.getName());
        holder.tv_mac.setText(device.getAddress());
        return convertView;
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
