package com.feipulai.device.serial.runnable;

import android.os.Message;

import com.feipulai.device.serial.SerialPorter;

import java.io.InputStream;

/**
 * Created by pengjf on 2018/10/29.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public abstract class SerialReadRunnable implements Runnable {
	
    private static final String TAG = "SerialPortReadThread";
    protected InputStream mInputStream;
    private volatile boolean isInterrupt;
	private SerialPorter.OnDataArrivedListener mListener;
    
    public SerialReadRunnable(InputStream inputStream, SerialPorter.OnDataArrivedListener listener) {
        mInputStream = inputStream;
        mListener = listener;
        if(listener == null){
        	throw new RuntimeException("listener can't be null");
        }
    }
    
    @Override
    public void run() {
	    Message msg ;
        while (!isInterrupt) {
	        msg = new Message();
            convert(msg);
//            Log.e("TAG","---------msg.what = "+msg.what);
            if(msg.what != 0){
            	mListener.onDataArrived(msg);
            }
        }
    }
	
    public abstract void convert(Message msg);
    
    public void stop() {
    	isInterrupt = true;
    }

}
