package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PullUpSetFrequencyResult{

	private int frequency;
	private int deviceId;
	private int rate;
	private int projectCode;
	
	//54  55  00  10  01  08  00  0b  19  04  00  00  00  00  27  0d
	//54  55  00  10  01  0B  00  0B  19  04  00  00  00  44  27  0D  00  00
	//rx_channel = buf[8];    //频段号
	//rx_rf_rate = buf[9];     //速率
	//rx_dev_id = buf[4];     //设备号
	public PullUpSetFrequencyResult(byte[] data){
		projectCode = data[5] & 0xff;
		frequency = data[8] & 0xff;
		rate = data[9] & 0xff;
		deviceId = data[4] & 0xff;
		LogUtils.normal("引体向上返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}

	public int getFrequency(){
		return frequency;
	}

	public void setFrequency(int frequency){
		this.frequency = frequency;
	}

	public int getDeviceId(){
		return deviceId;
	}

	public int getRate(){
		return rate;
	}

	public void setRate(int rate){
		this.rate = rate;
	}

	public int getProjectCode(){
		return projectCode;
	}

	public void setProjectCode(int projectCode){
		this.projectCode = projectCode;
	}

	@Override
	public String toString() {
		return "PullUpSetFrequencyResult{" +
				"frequency=" + frequency +
				", deviceId=" + deviceId +
				", rate=" + rate +
				", projectCode=" + projectCode +
				'}';
	}
	
}
