package com.feipulai.host.activity.jump_rope.setting;


/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class JumpRopeSetting{

	private int deviceSum = 20;
	private int testTime = 30;// 一轮测试的时间,单位为秒
	private int deviceGroup = 0;
	private boolean autoPair = true;
	
	public int getDeviceSum(){
		return deviceSum;
	}
	
	public void setDeviceSum(int deviceSum){
		this.deviceSum = deviceSum;
	}
	
	public int getTestTime(){
		return testTime;
	}
	
	public void setTestTime(int testTime){
		this.testTime = testTime;
	}
	
	public int getDeviceGroup(){
		return deviceGroup;
	}
	
	public void setDeviceGroup(int deviceGroup){
		this.deviceGroup = deviceGroup;
	}
	
	public boolean isAutoPair(){
		return autoPair;
	}
	
	public void setAutoPair(boolean autoPair){
		this.autoPair = autoPair;
	}
	
	@Override
	public String toString(){
		return "JumpRopeSetting{" +
				"deviceSum=" + deviceSum +
				", testTime=" + testTime +
				", deviceGroup=" + deviceGroup +
				", autoPair=" + autoPair +
				'}';
	}
	
}
