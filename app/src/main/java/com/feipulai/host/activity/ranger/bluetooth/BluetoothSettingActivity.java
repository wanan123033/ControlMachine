package com.feipulai.host.activity.ranger.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.feipulai.common.utils.SharedPrefsUtil;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.spputils.SppUtils;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.feipulai.host.activity.ranger.RangerSetting;
import com.feipulai.host.view.OperateProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class BluetoothSettingActivity extends BaseTitleActivity implements AdapterView.OnItemClickListener{
    @BindView(R.id.rv_bluetooth)
    ListView rv_bluetooth;
    private RangerSetting setting;
    private BluetoothListAdapter adapter;
    private List<BluetoothDevice> devices;
    private SppUtils utils;
    private BluetoothDevice device;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("蓝牙设置");
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.dialog_bluetooth;
    }

    @Override
    protected void initData() {
        devices = new ArrayList<>();
        setting = SharedPrefsUtil.loadFormSource(this, RangerSetting.class);
        adapter = new BluetoothListAdapter(devices,this);
        rv_bluetooth.setAdapter(adapter);
        utils = BluetoothManager.getSpp(this);
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
                OperateProgressBar.removeLoadingUiIfExist(BluetoothSettingActivity.this);
                Toast.makeText(getApplicationContext(), "连接成功 "+name, Toast.LENGTH_SHORT).show();
                setting.setBluetoothName(name);
                setting.setBluetoothMac(address);
                SharedPrefsUtil.save(getApplicationContext(),setting);
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext(), "断开连接", Toast.LENGTH_SHORT).show();
//                utils.connect(device.getAddress());
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext(), "请试着重启一下蓝牙再试一次", Toast.LENGTH_SHORT).show();

                utils.connect(device.getAddress());
            }
        });

        rv_bluetooth.setOnItemClickListener(this);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        device = adapter.getItem(position);
        ParcelUuid[] uuids = device.getUuids();
        for (ParcelUuid uuid : uuids){
            Log.e("TAG----",uuid.getUuid().toString());
        }
        OperateProgressBar.showLoadingUi(this,"正在连接中...");
        utils.connect(device.getAddress());
    }
    @OnClick({R.id.btn_search})
    public void onClick(View view){
        utils.cancelDiscovery();
        devices.clear();
        utils.startDiscovery();
    }
}
