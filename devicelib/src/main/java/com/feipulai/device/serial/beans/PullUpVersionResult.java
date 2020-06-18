package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/16 0016.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PullUpVersionResult{
	
	//[00] [01]：包头高字节0x54   低字节0x55
	//[02] [03]：长度高字节0x00   低字节0x10
	//[04]：子机id
	//[05]：0x0b   项目编号
	//[06]：0   –单机模式
	//[07]：12   --获取数据
	//[08]：(大版本<<4)+(次版本)
	//[09]：小版本
	//[10]：年 - 2000
	//[11]：月
	//[12]：日
	//[13]：累加和
	//[14] [15]：包尾高字节0x27   低字节0x0d
	
	private int deviceId;
	private String version;
	private String publishDay;
	
	public PullUpVersionResult(byte[] data){
		deviceId = data[4] & 0xff;
		version = (data[8] >>> 4) + "." + (data[8] & 0x0f) + "." + (data[9] & 0xff);
		publishDay = (2000 + (data[10] & 0xff)) + "" + String.format("%02d",(data[11] & 0xff)) + String.format("%02d",(data[12] & 0xff));
		LogUtils.normal("引体向上返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}
	
	public int getDeviceId(){
		return deviceId;
	}
	
	public void setDeviceId(int deviceId){
		this.deviceId = deviceId;
	}
	
	public String getVersion(){
		return version;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getPublishDay(){
		return publishDay;
	}
	
	public void setPublishDay(String publishDay){
		this.publishDay = publishDay;
	}

	@Override
	public String toString() {
		return "PullUpVersionResult{" +
				"deviceId=" + deviceId +
				", version='" + version + '\'' +
				", publishDay='" + publishDay + '\'' +
				'}';
	}
}
