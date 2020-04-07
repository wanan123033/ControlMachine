package com.feipulai.exam.activity.ranger;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.ranger.adapter.BluetoothListAdapter;
import com.feipulai.exam.spputils.SppUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BluetoothSettingActivity extends BaseTitleActivity implements AdapterView.OnItemClickListener {

    private List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothListAdapter adapter;
    private SppUtils utils;

    @BindView(R.id.rv_bluetooth)
    ListView rv_bluetooth;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        builder.setTitle("蓝牙设置");
        return super.setToolbar(builder);
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.dialog_bluetooth;
    }

    @Override
    protected void initData() {
        utils = BluetoothManager.getSpp(getApplicationContext());
        adapter = new BluetoothListAdapter(devices,getApplicationContext());
        rv_bluetooth.setAdapter(adapter);
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
                Toast.makeText(getApplicationContext(), "连接成功 "+name, Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext(), "断开连接", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT).show();
            }
        });

        rv_bluetooth.setOnItemClickListener(this);
    }

    @OnClick({R.id.btn_search})
    public void onClick(View view){
        devices.clear();
        utils.startDiscovery();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice item = adapter.getItem(position);
        utils.connect(item.getAddress());
    }
}
