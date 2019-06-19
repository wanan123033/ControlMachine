package com.feipulai.device.printer;

import android.os.Message;


import com.feipulai.device.serial.IOPower;
import com.feipulai.device.serial.SerialParams;
import com.feipulai.device.serial.SerialPorter;

import java.io.UnsupportedEncodingException;

/**
 * Created by James on 2018/7/24 0024.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrinterManager{
	
	public static final int LEFT = 0;
	public static final int MIDDLE = 1;
	public static final int RIGHT = 2;
	
	private static volatile PrinterManager sPrinterManager;
	private SerialPorter mSerialPorter;
	private OnPrinterListener mOnPrinterListener;
	
	private PrinterManager(){
		IOPower.getInstance().setPrinterPwr(1);
		mSerialPorter = new SerialPorter(SerialParams.PRINTER,new SerialPorter.OnDataArrivedListener(){
			@Override
			public void onDataArrived(Message msg){
				if(mOnPrinterListener != null){
					mOnPrinterListener.onPrinterListener(msg);
				}
			}
		});
	}
	
	public static PrinterManager getInstance(){
		if(sPrinterManager == null){
			synchronized(PrinterManager.class){
				if(sPrinterManager == null){
					sPrinterManager = new PrinterManager();
				}
			}
		}
		return sPrinterManager;
	}
	
	public void setOnPrinterListener(OnPrinterListener onPrinterListener){
		mOnPrinterListener = onPrinterListener;
	}
	
	public void print(String text){
		try{
			byte[] cmd = (text + "\n").getBytes("GBK");
			mSerialPorter.sendCommand(cmd);
			//Log.i("james",StringUtility.bytesToHexString(cmd));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
	}
	
	// MMP
	public void printNln(String text){
		try{
			byte[] cmd = (text).getBytes("GBK");
			mSerialPorter.sendCommand(cmd);
			//Log.i("james",StringUtility.bytesToHexString(cmd));
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取打印机状态
	 */
	public void getState(){
		byte[] cmd = {0x1d,0x72,0x01};
		mSerialPorter.sendCommand(cmd);
	}
	
	/**
	 * 初始化打印机
	 * 清除打印缓冲器中的数据并且复位打印机配置
	 * 接收缓冲区中的数据不被清除
	 */
	public void init(){
		byte[] cmd = {0x1b,0x40};
		mSerialPorter.sendCommand(cmd);
	}
	
	/**
	 * 设置行间距为默认值
	 * 行间距默认值为6点行(6 * 0.125mm)
	 */
	public void resetLineSpacing(){
		byte[] cmd = {0x1b,0x32};
		mSerialPorter.sendCommand(cmd);
	}
	
	/**
	 * 设置行间距为n点行(n * 0.125mm)
	 * 0 <= n <= 255
	 */
	public void setLineSpacing(int n){
		byte[] cmd = {0x1b,0x33,(byte)(n & 0xff)};
		mSerialPorter.sendCommand(cmd);
	}
	
	/**
	 * 设置倍高和倍宽
	 * 建议一般倍高不倍宽
	 * @param doubleHeight 倍高
	 * @param doubleWidth  倍宽
	 */
	public void doubleHW(boolean doubleHeight,boolean doubleWidth){
		byte[] cmd = {0x1b,0x21,0x00};
		if(doubleHeight){
			cmd[2] |= 0x10;
		}
		if(doubleWidth){
			cmd[2] |= 0x20;
		}
		mSerialPorter.sendCommand(cmd);
	}
	
	public void nCRLF(int n){
		for(int i = 0;i < n;i++){
			print("\n");
		}
	}
	
	/**
	 * 设置西文右侧字符间距n点行(n * 0.125mm)
	 * 当字符打印模式为倍宽模式时,该指令设置的间距随之改变
	 * 仅用于ASCII字符,不影响汉字设定
	 *
	 * @param n 0 <= n <= 255
	 */
	public void setASCIISpacing(int n){
		byte[] cmd = {0x1b,0x20,(byte)(n & 0xff)};
		mSerialPorter.sendCommand(cmd);
	}
	
	/**
	 * 设置中文左右边距n点行(n * 0.125mm)
	 * 仅用于汉字字符,不影响ASCII字符设定
	 * 0 <= n <= 255
	 *
	 * @param left  左边距
	 * @param right 右边距
	 */
	public void setGBKSpacing(int left,int right){
		byte[] cmd = {0x1c,0x53,(byte)(left & 0xff),(byte)(right & 0xff)};
		mSerialPorter.sendCommand(cmd);
	}
	
	/**
	 * 打印自检页
	 */
	public void selfCheck(){
		byte[] cmd = {0x12,0x54};
		mSerialPorter.sendCommand(cmd);
	}
	
	public void setPointSize(int x,int y){
		byte[] cmd = {0x1d,0x50,(byte)(x & 0xff),(byte)(y & 0xff)};
		mSerialPorter.sendCommand(cmd);
	}
	
	/**
	 * 设置对其方式
	 *
	 * @param align 对其方式,取值{@link #LEFT} {@link #MIDDLE} {@link #RIGHT}
	 */
	public void setAlign(int align){
		byte[] cmd = {0x1b,0x61,(byte)(align & 0xff)};
		mSerialPorter.sendCommand(cmd);
	}
	
	public void close(){
		mSerialPorter.close();
		IOPower.getInstance().setPrinterPwr(0);
		sPrinterManager = null;
	}
	
	public interface OnPrinterListener{
		void onPrinterListener(Message msg);
	}
	
}

// 使用demo
//
//		PrinterManager.getInstance().setOnPrinterListener(new PrinterManager.OnPrinterListener(){
//@Override
//public void onPrinterListener(Message msg){
//		switch(msg.what){
//		case SerialConfigs.PRINTER_STATE:
//		PrinterState state = (PrinterState)msg.obj;
//		Log.i("james","receive state:" + state.toString());
//		}
//		}
//		});
//		PrinterManager.getInstance().getState();
//		PrinterManager.getInstance().init();
//		//PrinterManager.getInstance().print("正常测试-OK");
//		//PrinterManager.getInstance().nCRLF(2);
//		//PrinterManager.getInstance().setAlign(PrinterManager.RIGHT);
//		//PrinterManager.getInstance().print("右对齐测试-OK");
//		//PrinterManager.getInstance().setAlign(PrinterManager.LEFT);
//		//PrinterManager.getInstance().print("左对齐测试-OK");
//		//PrinterManager.getInstance().setAlign(PrinterManager.MIDDLE);
//		//PrinterManager.getInstance().print("居中测试--OK");
//		//PrinterManager.getInstance().nCRLF(2);
//
//
//		PrinterManager.getInstance().doubleHW(true,false);
//		PrinterManager.getInstance().print("倍高-OK");
//		PrinterManager.getInstance().print("倍高一行能打印出多少字呢,我的天,垃圾打印机模块");
//		//PrinterManager.getInstance().doubleHW(false,true);
//		//PrinterManager.getInstance().print("倍宽-OK");
//		//PrinterManager.getInstance().doubleHW(true,true);
//		//PrinterManager.getInstance().print("倍高倍宽-OK");
//		//PrinterManager.getInstance().doubleHW(false,false);
//		//PrinterManager.getInstance().print("倍高倍宽取消-OK");
//
//		//PrinterManager.getInstance().setFontSize(2,2);
//		//PrinterManager.getInstance().print("字体大小设置-OK");
//		//PrinterManager.getInstance().setFontSize(3,2);
//		//PrinterManager.getInstance().print("字体大小设置-OK");
//		//PrinterManager.getInstance().setFontSize(2,3);
//		//PrinterManager.getInstance().print("字体大小设置-OK");
//		//PrinterManager.getInstance().setFontSize(3,3);
//		//PrinterManager.getInstance().print("字体大小设置-OK");
//		//PrinterManager.getInstance().setFontSize(4,2);
//		//PrinterManager.getInstance().print("字体大小设置-OK");
//		//PrinterManager.getInstance().setBold(true);
//		//PrinterManager.getInstance().print("粗体-OK");
//		//PrinterManager.getInstance().setBold(false);
//		//PrinterManager.getInstance().print("粗体取消-OK");
//
//		PrinterManager.getInstance().setPointSize(50,50);
//		PrinterManager.getInstance().print("字体大小设置-OK");
//		//PrinterManager.getInstance().setLineSpacing(30);
//		//PrinterManager.getInstance().print("设置行间距30点行");
//		//PrinterManager.getInstance().print("设置行间距30点行");
//		//
//		//PrinterManager.getInstance().resetLineSpacing();
//		//PrinterManager.getInstance().print("重置行间距");
//		//PrinterManager.getInstance().print("重置行间距");
//		//
//		//PrinterManager.getInstance().setASCIISpacing(10);
//		//PrinterManager.getInstance().print("ascii line space 10 dot");
//		//
//		//PrinterManager.getInstance().setGBKSpacing(20,20);
//		//PrinterManager.getInstance().print("中文左右20点行");
//		PrinterManager.getInstance().nCRLF(2);
////
////PrinterManager.getInstance().init();
////PrinterManager.getInstance().selfCheck();