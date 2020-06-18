package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/16 0016.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PullUpStateResult implements IDeviceResult{
	
	//[00] [01]：包头高字节0x54   低字节0x55
	//[02] [03]：长度高字节0x00   低字节0x10
	//[04]：子机id
	//[05]：0x0b  项目编号
	//[06]：0   –单机模式
	//[07]：4   --获取数据
	//[08]：有效计数值
	//[09]：总计数值
	//[10]：子机状态
	//[11]：电池电量（百分比值）
	//[12]：间隔时间，单位为s
	//[13]：累加和
	//[14] [15]：包尾高字节0x27   低字节0x0d
	
	private int deviceId;
	private int validCountNum;
	private int totalCountNum;
	private int state;//空闲=1 准备=2 计数=3 结束=4 其他=未知
	private int batteryLeft;
	private int interval;
	
	public static final int STATE_FREE = 1;
	public static final int STATE_READY = 2;
	public static final int STATE_COUNTING = 3;
	public static final int STATE_ENDED = 4;
	
	public PullUpStateResult(byte[] data){
		deviceId = data[4] & 0xff;
		validCountNum = data[8] & 0xff;
		totalCountNum = data[9] & 0xff;
		state = data[10] & 0xff;
		batteryLeft = data[11] & 0xff;
		interval = data[12] & 0xff;
		LogUtils.normal("引体向上返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}

	public PullUpStateResult(){
	}
	
	public int getDeviceId(){
		return deviceId;
	}
	
	public void setDeviceId(int deviceId){
		this.deviceId = deviceId;
	}
	
	public int getValidCountNum(){
		return validCountNum;
	}
	
	public void setValidCountNum(int validCountNum){
		this.validCountNum = validCountNum;
	}
	
	public int getTotalCountNum(){
		return totalCountNum;
	}
	
	public void setTotalCountNum(int totalCountNum){
		this.totalCountNum = totalCountNum;
	}
	
	public int getState(){
		return state;
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public int getBatteryLeft(){
		return batteryLeft;
	}
	
	public void setBatteryLeft(int batteryLeft){
		this.batteryLeft = batteryLeft;
	}
	
	public int getInterval(){
		return interval;
	}
	
	public void setInterval(int interval){
		this.interval = interval;
	}
	
	@Override
	public int getResult(){
		return getValidCountNum();
	}

	@Override
	public void setResult(int result){
		totalCountNum = result;
	}

	@Override
	public String toString() {
		return "PullUpStateResult{" +
				"deviceId=" + deviceId +
				", validCountNum=" + validCountNum +
				", totalCountNum=" + totalCountNum +
				", state=" + state +
				", batteryLeft=" + batteryLeft +
				", interval=" + interval +
				'}';
	}
}
