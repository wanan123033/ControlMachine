package com.feipulai.device.serial;

import android.os.Message;

import com.feipulai.device.serial.command.ConvertCommand;

/**
 * Created by James on 2018/11/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class RadioManager{
	
	private SerialPorter mSerialPorter;
	private volatile OnRadioArrivedListener mOnRadioArrived;
	private static volatile RadioManager instance;
	public static final int RADIO_INTERVAL = 100;
	private long mlastSendTime;
	
	private RadioManager(){}
	
	public synchronized static RadioManager getInstance(){
		if(instance == null){
			instance = new RadioManager();
		}
		return instance;
	}
	
	public synchronized void init(){
		IOPower.getInstance().setUhfcommPwr(1);
		mSerialPorter = new SerialPorter(SerialParams.RADIO,new SerialPorter.OnDataArrivedListener(){
			@Override
			public void onDataArrived(Message msg){
				if(mOnRadioArrived != null){
					mOnRadioArrived.onRadioArrived(msg);
				}
			}
		});
	}
	
	// 程序退出时才调用
	public synchronized void close(){
	    if (mSerialPorter == null)
	        return;
		mSerialPorter.close();
		IOPower.getInstance().setUhfcommPwr(0);
		instance = null;
	}
	
	public void setOnRadioArrived(OnRadioArrivedListener onRadioArrived){
		mOnRadioArrived = onRadioArrived;
	}
	
	// 这个地方必须锁住,万恶之源
	public synchronized void sendCommand(ConvertCommand convertCommand){
		ensureInterval();
		if (mSerialPorter == null)
		    return;
		mSerialPorter.sendCommand(convertCommand);
	}

	private void ensureInterval(){
		try{
			//Thread.sleep(RADIO_INTERVAL);
			long curTime = System.currentTimeMillis();
			long expectTime = mlastSendTime + RADIO_INTERVAL;
			if(curTime < expectTime){
				//Log.i("james",expectTime - curTime + "");
				Thread.sleep(expectTime - curTime);
			}
			mlastSendTime = System.currentTimeMillis();
		}catch(InterruptedException e){
			// 保留打断标志
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}
	
	public interface OnRadioArrivedListener{
		void onRadioArrived(Message msg);
	}
	
}
