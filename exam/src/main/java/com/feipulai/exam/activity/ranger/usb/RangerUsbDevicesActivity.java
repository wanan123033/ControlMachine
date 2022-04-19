package com.feipulai.exam.activity.ranger.usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.ranger.driver.UsbSerialDriver;
import com.feipulai.exam.activity.ranger.driver.UsbSerialProber;
import com.feipulai.exam.adapter.RangerUsbDevicesAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class RangerUsbDevicesActivity extends BaseTitleActivity implements AdapterView.OnItemClickListener {
    private List<ListItem> listItems;
    @BindView(R.id.rv_devices)
    RecyclerView rv_devices;
    private UsbManager usbManager;

    private RangerUsbDevicesAdapter devicesAdapter;
    @Override
    protected BaseToolbar.Builder setToolbar(BaseToolbar.Builder builder) {
        return builder.setTitle("测距USB设备列表").addRightText("刷新", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDevices();
            }
        });
    }
    public void refreshDevices() {
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        listItems.clear();
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
            }
            if(driver != null) {
                for(int port = 0; port < driver.getPorts().size(); port++)
                    listItems.add(new ListItem(device, port, driver));
            } else {
                listItems.add(new ListItem(device, 0, null));
            }
        }
        devicesAdapter = new RangerUsbDevicesAdapter(this,listItems);
        rv_devices.setAdapter(devicesAdapter);
        devicesAdapter.setOnItemClickListener(this);
    }
    @Override
    protected int setLayoutResID() {
        return R.layout.activity_ranger_usb_devices;
    }

    @Override
    protected void initData() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        listItems = new ArrayList<>();
        rv_devices.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDevices();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        connectDevice(listItems.get(position));
    }

    private void connectDevice(ListItem item) {
        UsbDevice device = null;
        for(UsbDevice v : usbManager.getDeviceList().values())
            if(v.getDeviceId() == item.device.getDeviceId())
                device = v;
        if(device == null) {
            ToastUtils.showShort("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if(driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if(driver == null) {
            ToastUtils.showShort("connection failed: no driver for device");
            return;
        }
        if(driver.getPorts().size() < item.port) {
            ToastUtils.showShort("connection failed: not enough ports at device");
            return;
        }
        RangerUsbSerialUtils.getInstance(this).connectDevice(usbManager,device,driver,item);
    }
}
