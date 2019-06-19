package com.feipulai.device;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.idcard.IDCardDevice;
import com.feipulai.device.qrcode.QRManager;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

public class CheckDeviceOpener implements Handler.Callback, NFCDevice.OnICCardListener, IDCardDevice.OnIDReadListener, QRManager.QRCodeListener {
	
	private NFCDevice nfcd;
	private IDCardDevice idCardDevice;
	
	private HandlerThread handlerThread;
	private Handler handler;
	
	private volatile boolean openId;
	private volatile boolean openIC;
	private volatile boolean openQR;
	
	private volatile Activity activity;
	
	private volatile OnCheckDeviceArrived listener;
	
	private static volatile CheckDeviceOpener opener;
	
	private CheckDeviceOpener() {
		nfcd = new NFCDevice();
		idCardDevice = new IDCardDevice();
		
		handlerThread = new HandlerThread("handlerThread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper(), this);
	}
	
	public synchronized static CheckDeviceOpener getInstance() {
		if (opener == null) {
			opener = new CheckDeviceOpener();
		}
		return opener;
	}
	
	public void open(Activity activity, boolean openId, boolean openIC, boolean openQR) {
		this.activity = activity;
		this.openId = openId;
		this.openIC = openIC;
		this.openQR = openQR;
		handler.sendEmptyMessage(0);
	}
	
	public void close() {
		activity = null;
		listener = null;
		handler.sendEmptyMessage(1);
	}
	
	public void destroy() {
		handlerThread.quit();
		opener = null;
	}
	
	public void setQrLength(int length){
		QRManager.getInstance().setQrLength(length);
	}
	
	public void setOnCheckDeviceArrived(OnCheckDeviceArrived listener) {
		this.listener = listener;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			
			case 0:
				if (openIC) {
					nfcd.setOnICCardListener(this);
					nfcd.open(activity);
				}
				
				if (openId) {
					idCardDevice.open(activity);
					idCardDevice.setOnIDReadListener(this);
				}
				
				if (openQR) {
					QRManager.getInstance().setQRCodeListener(this);
					QRManager.getInstance().startScan();
				}
				break;
			
			case 1:
				listener = null;
				
				nfcd.close();
				// nfcd.setOnICCardListener(null);
				
				idCardDevice.close();
				// idCardDevice.setOnIDReadListener(null);
				
				QRManager.getInstance().stopScan();
				// QRManager.getInstance().setQRCodeListener(null);
				
				break;
		}
		return true;
	}
	
	@Override
	public void onICCardFound() {
		if (listener != null) {
			listener.onICCardFound(nfcd);
		}
	}
	
	@Override
	public void onIdCardRead(IDCardInfo idCardInfo) {
		if (listener != null) {
			listener.onIdCardRead(idCardInfo);
		}
	}
	
	@Override
	public void onQrArrived(String qrCode) {
		if (listener != null) {
			listener.onQrArrived(qrCode);
		}
	}
	
	@Override
	public void onWrongLength(int length, int expectLength) {
		if (listener != null) {
			listener.onQRWrongLength(length, expectLength);
		}
	}
	
	public interface OnCheckDeviceArrived {
		void onICCardFound(NFCDevice nfcd);
		
		void onIdCardRead(IDCardInfo idCardInfo);
		
		void onQrArrived(String qrCode);
		
		void onQRWrongLength(int length, int expectLength);
	}
	
}
