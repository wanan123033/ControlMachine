package com.feipulai.exam.activity.ranger.usb;


import android.hardware.usb.UsbDevice;

import com.feipulai.exam.activity.ranger.driver.UsbSerialDriver;

public class ListItem {
    public UsbDevice device;
    public int port;
    public UsbSerialDriver driver;

    ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
        this.device = device;
        this.port = port;
        this.driver = driver;
    }
}