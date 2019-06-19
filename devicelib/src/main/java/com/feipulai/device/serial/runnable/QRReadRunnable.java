package com.feipulai.device.serial.runnable;

import android.os.Message;
import android.util.Log;


import com.feipulai.device.qrcode.QRManager;
import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialPorter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by pengjf on 2018/11/6.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class QRReadRunnable extends SerialReadRunnable{
	
	private String TAG = "QRReadRunnable";
	private static final int BASE_LENGTH = 1024;
	byte[] data = new byte[BASE_LENGTH];
	
	public QRReadRunnable(InputStream inputStream, SerialPorter.OnDataArrivedListener listener){
		super(inputStream,listener);
	}
	
	@Override
	public void convert(Message msg){
		if(mInputStream == null){
			return;
		}
		try{
			while(mInputStream.available() < 1){
				Thread.sleep(50);
			}
			Thread.sleep(1000);
			int size = mInputStream.read(data);
			byte[] qrCodeBytes = new byte[size];
			Log.i(TAG,"convert: size: " + size);
			System.arraycopy(data,0,qrCodeBytes,0,size);
			Log.i(TAG,"convert: size: " + Arrays.toString(qrCodeBytes));
			if (qrCodeBytes.length > 2 && qrCodeBytes[size - 1] == 0X0A && qrCodeBytes[size - 2] == 0X0D) {
				qrCodeBytes= new byte[size-2];
				System.arraycopy(data, 0, qrCodeBytes, 0, size - 2);
			}
			String qrCode = new String(qrCodeBytes);
			// 释放触发
			QRManager.getInstance().stopScan();
			msg.what = SerialConfigs.QR_CODE_READ;
			msg.obj = qrCode;
			// 这里保证后续触发间隔超过50ms
			Thread.sleep(50);
		}catch(IOException e){
			e.printStackTrace();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
}
