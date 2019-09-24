package com.feipulai.host.activity.situp.setting;


/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SitUpSetting {

	private int deviceSum = 5;
	private int testTime = 30;// 一轮测试的时间,单位为秒
	private boolean autoPair = true;
	private int angle = 65;
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

	public boolean isAutoPair(){
		return autoPair;
	}

	public void setAutoPair(boolean autoPair){
		this.autoPair = autoPair;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	@Override
	public String toString() {
		return "SitUpSetting{" +
				"deviceSum=" + deviceSum +
				", testTime=" + testTime +
				", autoPair=" + autoPair +
				'}';
	}

}
