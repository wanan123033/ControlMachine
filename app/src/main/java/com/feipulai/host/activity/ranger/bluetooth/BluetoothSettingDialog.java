package com.feipulai.host.activity.ranger.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.host.R;
import com.feipulai.host.activity.ranger.RangerSetting;
import com.feipulai.host.view.OperateProgressBar;

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
    private BluetoothDevice device;
    private RangerSetting setting;

    public BluetoothSettingDialog(Context context) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_bluetooth,null,false);
        ButterKnife.bind(this,view);
        setView(view);
        setTitle("蓝牙设置");
        setIcon(android.R.drawable.ic_dialog_info);
        setCancelable(false);
        setting = SharedPrefsUtil.loadFormSource(context,RangerSetting.class);
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
                OperateProgressBar.removeLoadingUiIfExist((Activity) getContext());
                Toast.makeText(getContext(), "连接成功 "+name, Toast.LENGTH_SHORT).show();
                setting.setBluetoothName(name);
                setting.setBluetoothMac(address);
                SharedPrefsUtil.save(getContext(),setting);
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getContext(), "断开连接", Toast.LENGTH_SHORT).show();
//                utils.connect(device.getAddress());
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getContext(), "请试着重启一下蓝牙再试一次", Toast.LENGTH_SHORT).show();

                utils.connect(device.getAddress());
            }
        });

        rv_bluetooth.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice item = adapter.getItem(position);
        device = item;
        ParcelUuid[] uuids = device.getUuids();
        for (ParcelUuid uuid : uuids){
            Log.e("TAG----",uuid.getUuid().toString());
        }
        OperateProgressBar.showLoadingUi((Activity) getContext(),"正在连接中...");
        utils.connect(item.getAddress());
    }
    @OnClick({R.id.btn_search})
    public void onClick(View view){
        utils.cancelDiscovery();
        devices.clear();
        utils.startDiscovery();
    }
}
