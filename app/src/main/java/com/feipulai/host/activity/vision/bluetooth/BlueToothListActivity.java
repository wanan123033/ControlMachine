package com.feipulai.host.activity.vision.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.view.PullToRefreshView;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.host.R;
import com.feipulai.host.activity.base.BaseTitleActivity;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

/**
 * Created by zzs on  2020/4/13
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BlueToothListActivity extends BaseTitleActivity {

    @BindView(R.id.rv_bluetooth)
    RecyclerView rvBluetooth;
    @BindView(R.id.refreshview)
    PullToRefreshView refreshview;
    private List<SearchResult> mDevices = new ArrayList<>();
    private BlueToothListAdapter adapter;
    private BluetoothDevice mDevice;
    private boolean mConnected;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("设备列表");
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_bluetooth_list;
    }

    @Override
    protected void initData() {
        rvBluetooth.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BlueToothListAdapter(mDevices);
        rvBluetooth.setAdapter(adapter);

        //蓝牙启动状态监听
        ClientManager.getClient().registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed));
                if (openOrClosed) {
                    searchDevice();
                }
            }
        });

        ClientManager.getClient().openBluetooth();

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mDevice = BluetoothUtils.getRemoteDevice(mDevices.get(position).getAddress());
                BlueToothHelper.getBlueBind().setBluetoothMac(mDevice.getAddress());
                ClientManager.getClient().registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
                connectDeviceIfNeeded();
            }
        });
        searchDevice();

        refreshview.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                searchDevice();
            }
        });
    }

    //蓝牙连接状态
    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);
            connectDeviceIfNeeded();
        }
    };

    /**
     * 扫描设备
     */
    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(5000, 2).build();
        ClientManager.getClient().search(request, mSearchResponse);
    }

    /**
     * 蓝牙连接
     */
    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            ClientManager.connectDevice(mDevice.getAddress(), new BleConnectResponse() {
                @Override
                public void onResponse(int code, BleGattProfile bleGattProfile) {
                    if (code == REQUEST_SUCCESS) {
                        //设置读取
                        ClientManager.getGattProfile(bleGattProfile);
                        toastSpeak("连接成功");
                        finish();

                    }
                }
            });
        }
    }


    /**
     * 扫描监听
     */
    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            BluetoothLog.w("MainActivity.onSearchStarted");
            mDevices.clear();
            getToolbar().setTitle("扫描蓝牙中…");
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                Collections.sort(mDevices, new Comparator<SearchResult>() {
                    @Override
                    public int compare(SearchResult lhs, SearchResult rhs) {
                        return rhs.rssi - lhs.rssi;
                    }
                });
                adapter.notifyDataSetChanged();

            }

        }

        @Override
        public void onSearchStopped() {
            BluetoothLog.w("MainActivity.onSearchStopped");
            getToolbar().setTitle("设备列表");
            refreshview.finishRefresh();
        }

        @Override
        public void onSearchCanceled() {
            BluetoothLog.w("MainActivity.onSearchCanceled");
            getToolbar().setTitle("设备列表");
            refreshview.finishRefresh();
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().stopSearch();
    }

    @Override
    protected void onDestroy() {
        if (mDevice != null) {
            ClientManager.getClient().disconnect(mDevice.getAddress());
            ClientManager.getClient().unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        }
        super.onDestroy();
    }

}
