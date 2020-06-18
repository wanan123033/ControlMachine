package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/12/7.
 */

public class JumpRopeResult implements IDeviceResult{
	
	private int result;//计数
	private int state;//状态
	private int handId;//手柄号
	private int handGroup;
	private int batteryLeftPercent;
	private int factoryId;//对于老版本的跳绳,所有的出产编号都是0
	private int stumbleTimes;// 绊绳次数
	
	public JumpRopeResult(byte[] result){
		this.result = ((result[7] & 0xff) << 8) + (result[8] & 0xff);
		state = result[6] & 0xff;
		handId = result[4] & 0xff;
		handGroup = result[10] & 0xff;
		//新版跳绳,添加了电池电量百分比、出产编号和绊绳次数
		
		if(result.length == 18){
			batteryLeftPercent = result[13] & 0xff;
			factoryId = ((((result[14] & 0xff) << 8) + (result[15] & 0xff)) << 8) + (result[16] & 0xff);
			stumbleTimes = result[17] & 0xff;
		}else{
			batteryLeftPercent = getPower(result[9] & 0xff);
		}
		//Log.i("james","handId:" + handId + "\t\tfacttoryId:" + factoryId);
		LogUtils.normal("跳绳返回数据(解析前):"+result.length+"---"+StringUtility.bytesToHexString(result)+"---\n(解析后):"+toString());
	}
	
	// vol 100-150,表示 1.0-1.5v, 1.1v时,为低电量,这里直接映射到百分数,1.1v映射到10%
	private int getPower(int vol){
		return vol - 100;
	}
	
	public int getHandGroup(){
		return handGroup;
	}
	
	public void setHandGroup(int handGroup){
		this.handGroup = handGroup;
	}
	
	public int getHandId(){
		return handId;
	}
	
	public void setHandId(int handId){
		this.handId = handId;
	}
	
	public int getState(){
		return state;
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public int getResult(){
		return result;
	}
	
	public void setResult(int result){
		this.result = result;
	}
	
	public int getBatteryLeftPercent(){
		return batteryLeftPercent;
	}
	
	public void setBatteryLeftPercent(int batteryLeftPercent){
		this.batteryLeftPercent = batteryLeftPercent;
	}
	
	public int getFactoryId(){
		return factoryId;
	}
	
	public void setFactoryId(int factoryId){
		this.factoryId = factoryId;
	}
	
	public int getStumbleTimes(){
		return stumbleTimes;
	}
	
	public void setStumbleTimes(int stumbleTimes){
		this.stumbleTimes = stumbleTimes;
	}
	
	@Override
	public String toString(){
		return "jumpRopeResult{" +
				"result=" + result +
				", state=" + state +
				", handId=" + handId +
				", handGroup=" + handGroup +
				", batteryLeftPercent=" + batteryLeftPercent +
				", factoryId=" + factoryId +
				", stumbleTimes=" + stumbleTimes +
				'}';
	}
	
}
