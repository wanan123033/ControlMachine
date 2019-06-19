package com.feipulai.device.printer;

import android.os.Message;


import com.feipulai.device.serial.SerialConfigs;
import com.feipulai.device.serial.SerialPorter;
import com.feipulai.device.serial.runnable.SerialReadRunnable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by James on 2018/11/13 0013.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PrinterReadRunnable extends SerialReadRunnable {
	
	private String TAG = "PrinterReadRunnable";
	private static final int BASE_LENGTH = 1024;
	byte[] data = new byte[BASE_LENGTH];
	
	public PrinterReadRunnable(InputStream fileInputStream, SerialPorter.OnDataArrivedListener proxyListener){
		super(fileInputStream,proxyListener);
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
			Thread.sleep(100);
			int size = mInputStream.read(data);
			byte[] result = new byte[size];
			//Log.i(TAG,"convert: size: " + size);
			System.arraycopy(data,0,result,0,size);
			//Log.i(TAG,"convert: size: " + Arrays.toString(qrCodeBytes));
			if(size == 1){
				msg.what = SerialConfigs.PRINTER_STATE;
				msg.obj = new PrinterState(result[0]);
			}
			Thread.sleep(50);
		}catch(IOException e){
			e.printStackTrace();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
}
