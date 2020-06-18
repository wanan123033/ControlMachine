package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by James on 2017/10/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class JumpVersion{
	
	private String versionCode;
	private String versionDate;
	private DateFormat mDateFormat = new SimpleDateFormat("yyMMdd");
	
	//0x54 0x55 0x00 0x10 0x00 0x02 0x00 0x0C(命令字节) byte8(大版本) byte9（小版本） byte10（年） byte11（月） byte12（日） byte13(累加和) 0x27 0x0d
	public JumpVersion(byte[] result){
		versionCode = String.valueOf(result[8] >> 4 & 0x0f) + "." + (result[8] & 0x0f) + "."
				+ (result[9] >> 4 & 0x0f) + (result[9] & 0x0f);
		versionDate = String.valueOf(result[10]) + "年" + String.valueOf(result[11]) + "月" + String.valueOf(result[12]) + "日";
		LogUtils.normal("跳绳返回数据(解析前):"+result.length+"---"+StringUtility.bytesToHexString(result)+"---\n(解析后):"+toString());
	}
	
	public String getVersionCode(){
		return versionCode;
	}
	
	public String getVersionDate(){
		return versionDate;
	}
	
	public Date getDate(){
		Date date = null;
		try{
			date = mDateFormat.parse(versionDate);
		}catch(ParseException e){
			e.printStackTrace();
		}
		return date;
	}

	@Override
	public String toString() {
		return "JumpVersion{" +
				"versionCode='" + versionCode + '\'' +
				", versionDate='" + versionDate + '\'' +
				", mDateFormat=" + mDateFormat +
				'}';
	}
}
