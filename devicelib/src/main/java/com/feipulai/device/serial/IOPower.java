package com.feipulai.device.serial;

/**
 * Created by James on 2018/10/22 0022.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class IOPower{
	
	static {
		System.loadLibrary("io_power");
	}
	
	private static IOPower sIOPower = new IOPower();
	
	private IOPower(){}
	
	public static IOPower getInstance(){
		return sIOPower;
	}
	
	/**
	 * 二维码上电
	 * @param status 为1时上电,为0时断电
	 */
	public native void setBarcodePwr(int status);
	
	/**
	 * 扫描头触发
	 * @param status 为0时触发
	 */
	public native void setBarcodetrig(int status);
	
	/**
	 * 指纹上电
	 * @param status 为1时上电,为0时断电
	 */
	public native void setFingerPwr(int status);
	/**
	 * 身份证上电
	 * @param status 为1时上电,为0时断电
	 */
	public native void setIdentityPwr(int status);
	
	/**
	 * IC卡上电
	 * @param status 为1时上电,为0时断电
	 */
	public native void setICCardPwr(int status);
	
	/**
	 * 打印机上电
	 * @param status 为1时上电,为0时断电
	 */
	public native void setPrinterPwr(int status);
	
	/**
	 * 无线通讯上电
	 * @param status 为1时上电,为0时断电
	 */
	public native void setUhfcommPwr(int status);
	
}
