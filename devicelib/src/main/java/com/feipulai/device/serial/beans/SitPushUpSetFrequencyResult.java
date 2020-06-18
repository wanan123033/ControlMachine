package com.feipulai.device.serial.beans;

/**
 * Created by James on 2018/5/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

import com.orhanobut.logger.utils.LogUtils;

/**
 * 子机开机0频段发送
 * [00] [01]：包头高字节0x54  低字节0x55
 * [02] [03]：长度高字节0x00  低字节0x10
 * [04]：子机号
 * [05]：项目编号   5—仰卧起坐    8—俯卧撑
 * [06]：0   –单机模式
 * [07]：11   --设备频段号命令
 * [08]: 通道号
 * [09]: 速率
 * [10]-[12]: 0 0 0
 * [13]：累加和（从02到12共11个字节算术和的低字节）
 * [14] [15]：包尾高字节0x27   低字节0x0d
 * <p>
 * 若 子机号 或 通道号 不同,主机切换到刚才收到的通道号,下发:
 * [00] [01]：包头高字节0x54  低字节0x44
 * [02] [03]：长度高字节0x00  低字节0x10
 * [04]：子机号
 * [05]：项目编号   5—仰卧起坐    8—俯卧撑
 * [06]：0   –单机模式
 * [07]：11   --设备频段号命令
 * [08]: 通道号
 * [09]: 速率
 * [10]-[12]: 0 0 0
 * [13]：累加和（从02到12共11个字节算术和的低字节）
 * [14] [15]：包尾高字节0x27   低字节0x0d
 * <p>
 * 发完后，切换到本机通道号，等待接收上面 0x54 0x55的命令
 */
public class SitPushUpSetFrequencyResult {
	
	private int frequency;// 设备运行在的频率号
	private int deviceId;// 设备ID,可以通过主机设置
	private int rate;
	private int projectCode;//5—仰卧起坐    8—俯卧撑 0xb 引体向上手臂检测
	
	//54  55  00  10  01  08  00  0b  19  04  00  00  00  00  27  0d
	//54  55  00  10  01  0B  00  0B  19  04  00  00  00  44  27  0D  00  00
	//rx_channel = buf[8];    //频段号
	//rx_rf_rate = buf[9];     //速率
	//rx_dev_id = buf[4];     //设备号
	public SitPushUpSetFrequencyResult(byte[] data){
		projectCode = data[5] & 0xff;
		frequency = data[8] & 0xff;
		rate = data[9] & 0xff;
		deviceId = data[4] & 0xff;
		LogUtils.normal("硬件设置参数返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

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
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(!(o instanceof SitPushUpSetFrequencyResult)){
			return false;
		}
		
		SitPushUpSetFrequencyResult that = (SitPushUpSetFrequencyResult)o;
		
		if(getFrequency() != that.getFrequency()){
			return false;
		}
		if(getDeviceId() != that.getDeviceId()){
			return false;
		}
		if(getRate() != that.getRate()){
			return false;
		}
		return getProjectCode() == that.getProjectCode();
	}
	
	@Override
	public int hashCode(){
		int result = getFrequency();
		result = 31 * result + getDeviceId();
		result = 31 * result + getRate();
		result = 31 * result + getProjectCode();
		return result;
	}
	
	@Override
	public String toString(){
		return "SitUpSetFrequencyResult{" +
				"frequency=" + frequency +
				", deviceId=" + deviceId +
				", rate=" + rate +
				", projectCode=" + projectCode +
				'}';
	}
	
}
