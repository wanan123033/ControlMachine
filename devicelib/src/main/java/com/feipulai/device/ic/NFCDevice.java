package com.feipulai.device.ic;

import android.app.Activity;
import android.util.Log;

import com.feipulai.device.serial.IOPower;
import com.feipulai.device.serial.SerialParams;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Lib.FWReader.S8.function_S8;


/**
 * NFC模块
 * Created by zzs on 2018/11/2
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class NFCDevice{
	
	private boolean isOpen;
	private function_S8 call_s8;
	private int hdev = -1;
	private volatile boolean isShutDown;
	private ExecutorService mExecutor;
	private OnICCardListener mCardListener;
	
	/**
	 * 开启设备,开始识别IC卡
	 * 在{@link Activity#onResume()}中调用
	 */
	public void open(Activity activity){
		if(isOpen){
			return;
		}
		for (int i = 0; i < 3; i++) {
			IOPower.getInstance().setICCardPwr(1);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		call_s8 = new function_S8(activity);
		mExecutor = Executors.newSingleThreadExecutor();
		isShutDown = false;
		// 这是还没有开始读卡
		mExecutor.execute(new ICCardReadRunnable());
		isOpen = true;
	}
	
	/**
	 * 销毁串口通讯
	 * 在{@link Activity#onPause()}中调用,避免进入下一个界面要使用时出现占用的情况
	 */
	public void close(){
		isShutDown = true;
		if(mExecutor != null){
			mExecutor.shutdownNow();
		}
		// TODO: 2019/3/11 从日志上来看,在执行这个方法时,可能存在anr情况
		// call_s8.fw_exit(hdev);
		isOpen = false;
		IOPower.getInstance().setICCardPwr(0);
	}
	
	//try read ic card information
	private class ICCardReadRunnable implements Runnable {
		
		private static final long READ_INTERVAL = 1500L;
		
		@Override
		public void run(){
			try{
				while(!isShutDown){
					tryReadIC();
					Thread.sleep(READ_INTERVAL);
				}
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		
	}
	
	private void tryReadIC(){
		if(hdev == -1){
			hdev = call_s8.fw_init_ex(1,
					SerialParams.IC_Card.getPath().toCharArray(),
					SerialParams.IC_Card.getBaudRate());
		}
		if(hdev != -1){
			//Log.i("james",hdev + "");
			/**
			 * 寻卡,包含了{@link #request(int,short,int[])} {@link #anticoll(int,short,int[])} 和{@link #select(int,int,short[])}的功能
			 *
			 * @param lDevice 串口设备标志符
			 * @param iMode   寻卡模式:0---IDLE模式,一次只对一张卡操作;1---ALL模式,一次可对多张卡操作
			 * @param pSnr    字符数组,用于接收返回的10进制字符串卡号
			 * @return 成功时返回0
			 */
			char[] pSnrM1 = new char[255];
			int result = call_s8.fw_card_str(hdev,(short)0,pSnrM1);
			//Log.i("result",result + "");
			if(result == 0){
				if(mCardListener != null){
					mCardListener.onICCardFound();
				}
				/**
				 * 中止对该卡操作
				 * @param lDevice 串口设备标志符
				 * 成功时返回0
				 * 使用 fw_card()函数时,有个_Mode 参数,如果_Mode=0 则在对卡进行操作完毕后,执行 fw_halt();
				 * 则该卡进入 HALT 模式,则必须把卡移开感应区再进来才能寻得这张卡。
				 */
				call_s8.fw_halt(hdev);
			}
			/**
			 * 闭端口
			 * @param hdev 串口设备标志符
			 * 成功时返回0
			 */
			//call_s8.fw_exit(hdev);
			Log.i("NFCDevice","tryRead IC CARD once");
		}
	}
	
	/**
	 * 获取模块版本号
	 */
	public char[] getVersion(){
		//接收版本信息数组
		char[] version = new char[512];
		/**
		 * 获取模块版本号
		 * @param hdev    串口设备标志符
		 * @param version 字符数组,用于接收版本号
		 * @return 成功时返回0
		 */
		int result = call_s8.fw_getver(hdev,version);
		if(result == 0){
			return version;
		}
		return null;
	}
	
	/**
	 * 将密码装入读写模块RAM中
	 *
	 * @param iMode  装入密码模式  0-----keyA  4-----keyB
	 * @param iSecNr 扇区号
	 * @param pKey   写入读卡器中的卡密码
	 */
	public boolean loadKey(short iMode,short iSecNr,char[] pKey){
		int result = call_s8.fw_load_key(hdev,iMode,iSecNr,pKey);
		return result == 0;
	}
	
	/**
	 * 核对密码
	 *
	 * @param iMod 装入密码模式   0-----keyA,4-----keyB
	 * @param iSec 要验证密码的扇区号
	 * @return 成功时返回0
	 */
	public boolean authentication(short iMod,short iSec){
		int result = call_s8.fw_authentication(hdev,iMod,iSec);
		return result == 0;
	}

	/**
	 * 修改扇区密码
	 *
	 * @param newKey  新密码
	 * @param secNr   扇区号
	 * @param szCtrlW 控制位
	 * @param szKeyB  keyB
	 */
	public boolean changeKeyHex(char[] newKey,short secNr,char[] szCtrlW,char[] szKeyB){
		char[] strNewkey = new char[255];
		call_s8.hex_a(strNewkey,newKey,2 * (newKey.length));
		int result = call_s8.fw_changeKey_hex(hdev,secNr,strNewkey,szCtrlW,szKeyB);
		return result == 0;
	}
	
	public boolean write(short iAdr,char[] tWrite){
		int result = call_s8.fw_write(hdev,iAdr,tWrite);
		return result == 0;
	}
	
	/**
	 * 向指定块写入数据
	 *
	 * @param iAdr   块地址
	 * @param tWrite 要写入的数据
	 * @return 成功时返回0
	 */
	public boolean writeHex(short iAdr,char[] tWrite){
		char[] strHexWrite = new char[1024];
		call_s8.hex_a(strHexWrite,tWrite,2 * tWrite.length);
		int result = call_s8.fw_write_hex(hdev,iAdr,strHexWrite);
		return result == 0;
	}
	
	/**
	 * 读取指定块信息
	 *
	 * @param iAdr 块号
	 * @return 成功时返回0
	 */
	public char[] read(short iAdr){
		char[] strHexRead = new char[16];
		int result = call_s8.fw_read(hdev,iAdr,strHexRead);
		if(result == 0){
			return strHexRead;
		}else{
			return null;
		}
	}
	
	public interface OnICCardListener{
		void onICCardFound();
	}
	
	public void setOnICCardListener(OnICCardListener mNfcListener){
		this.mCardListener = mNfcListener;
	}
	
}
