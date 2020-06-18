package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2017/12/4.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ConverterVersion {
	
	//【3】数据长度N=16，1字节
	//【4:11】版本号，字符串格式
	//【12:19】日期，字符串格式
	//【4+N】校验和，1字节，= sum{2:3+N}取余
	private String versionCode;
	private String date;
	
	public ConverterVersion(byte[] data){
		if(data.length != 16){
			return;
		}
		//char ch;
		StringBuilder stringBuilder = new StringBuilder();
		//get version code
		for(int i = 0;i < 8;i++){
			//ch = (char)(data[i] & 0xff);
			stringBuilder.append((char)(data[i] & 0xff));
		}
		versionCode = stringBuilder.toString();
		stringBuilder = new StringBuilder();
		
		//get date
		for(int i = 8;i < data.length;i++){
			stringBuilder.append((char)(data[i] & 0xff));
		}
		date = stringBuilder.toString();
		LogUtils.normal("返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());
	}
	
	public String getVersionCode(){
		return versionCode;
	}
	
	public void setVersionCode(String versionCode){
		this.versionCode = versionCode;
	}
	
	public String getDate(){
		return date;
	}
	
	public void setDate(String date){
		this.date = date;
	}

	@Override
	public String toString() {
		return "ConverterVersion{" +
				"versionCode='" + versionCode + '\'' +
				", date='" + date + '\'' +
				'}';
	}
}
