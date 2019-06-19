package com.feipulai.common.dbutils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import org.greenrobot.eventbus.EventBus;

/**
 * USB插拔 BroadcastReceiver
 */
public class UsbStateChangeReceiver extends BroadcastReceiver{
	
	public static final String ACTION_USB_PERMISSION = "com.example.usbreadwriterdaemon.USB_PERMISSION";
	private static final String TAG = "UsbStateChangeReceiver";
	
	@Override
	public void onReceive(Context context,Intent intent){
		String action = intent.getAction();
		UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		if(device == null){
			return;
		}
		if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
			EventBus.getDefault().post(new UsbStatusChangeEvent(true,false,device));
		}else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)){
			EventBus.getDefault().post(new UsbStatusChangeEvent(false,false,device));
		}else if(ACTION_USB_PERMISSION.equals(action)){
			// 允许权限申请
			if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false)){
				EventBus.getDefault().post(new UsbStatusChangeEvent(true,true,device));
			}
		}
	}
	
}