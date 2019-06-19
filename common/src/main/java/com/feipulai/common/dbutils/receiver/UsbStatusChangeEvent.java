package com.feipulai.common.dbutils.receiver;

import android.hardware.usb.UsbDevice;

/**
 * Created by swson 2018/8/28
 */
public class UsbStatusChangeEvent{
	
	public boolean isConnected;
	public boolean isGetPermission;
	public UsbDevice usbDevice;
	
	public UsbStatusChangeEvent(boolean isConnected,boolean isGetPermission,UsbDevice usbDevice){
		this.isConnected = isConnected;
		this.isGetPermission = isGetPermission;
		this.usbDevice = usbDevice;
	}
	
}