package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/3 0003.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SitReachVersionResult{
	//8     9   10  11  12
	//HI	J	Y	M	D
	//版本：H.I.J
	//日期：Y年M月D日
	//说明：H和I为1位数字；
	//J/Y/M/D均为2位数字
	//不够2位的，前面填0
	
	private String version;
	
	public SitReachVersionResult(byte[] data){
		int H = (data[8] >> 4) & 0x0f;
		int I = data[8] & 0x0f;
		int J = data[9] & 0xff;
		int Y = data[10] & 0xff;
		int M = data[11] & 0xff;
		int D = data[12] & 0xff;
		version = "V" + H + "." + I + "." + J + "\t20" + String.format("%02d",Y) +  String.format("%02d",M)  + String.format("%02d",D) ;
		//Logger.i(version);
		LogUtils.normal("坐位体前屈返回设备数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}
	
	public String getVersion(){
		return version;
	}
	
	public void setVersion(String version){
		this.version = version;
	}

	@Override
	public String toString() {
		return "SitReachVersionResult{" +
				"version='" + version + '\'' +
				'}';
	}
}
