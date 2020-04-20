package com.feipulai.exam.activity.ranger.bluetooth;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.feipulai.device.spputils.SppUtils;
import com.feipulai.exam.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BluetoothSettingDialog extends AlertDialog.Builder implements AdapterView.OnItemClickListener, View.OnClickListener {
    @BindView(R.id.rv_bluetooth)
    ListView rv_bluetooth;

    BluetoothListAdapter adapter;
    List<BluetoothDevice> devices = new ArrayList<>();
    SppUtils utils;

    public BluetoothSettingDialog(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_bluetooth,null,false);
        ButterKnife.bind(this,view);
        setView(view);
        setTitle("蓝牙设置");
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(true);
        adapter = new BluetoothListAdapter(devices,context);
        rv_bluetooth.setAdapter(adapter);
        utils = BluetoothManager.getSpp(context);

        utils.setOnDeviceCallBack(new SppUtils.OnDeviceCallBack() {
            @Override
            public void onDeviceCallBack(BluetoothDevice device) {
                devices.add(device);
                adapter.notifyDataSetChanged();
            }
        });
        utils.startDiscovery();

        utils.setBluetoothConnectionListener(new SppUtils.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getContext(), "连接成功 "+name, Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getContext(), "断开连接", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getContext(), "连接失败", Toast.LENGTH_SHORT).show();
            }
        });

        rv_bluetooth.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice item = adapter.getItem(position);
        utils.connect(item.getAddress());
    }
    @OnClick({R.id.btn_search})
    public void onClick(View view){
        utils.cancelDiscovery();
        devices.clear();
        utils.startDiscovery();
    }
}
