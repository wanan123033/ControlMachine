package com.feipulai.exam.activity.situp.setting;

import com.feipulai.exam.config.TestConfigs;

/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SitUpSetting {
	private int testType = 0;
	private int deviceSum = 5;
	private int testTime = 30;// 一轮测试的时间,单位为秒
	private int testNo = 1;// 允许测试的次数
	private int groupMode = TestConfigs.GROUP_PATTERN_SUCCESIVE;
	private boolean autoPair = true;
	private boolean isPenalize;
	private int angle = 65;

	public int getGroupMode(){
		return groupMode;
	}
	
	public void setGroupMode(int groupMode){
		this.groupMode = groupMode;
	}

	@Deprecated
	public int getTestNo(){
		return testNo;
	}
	
	public void setTestNo(int testNo){
		this.testNo = testNo;
	}
	
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

	public boolean isPenalize() {
		return isPenalize;
	}

	public void setPenalize(boolean penalize) {
		isPenalize = penalize;
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
				", testNo=" + testNo +
				", groupMode=" + groupMode +
				", angle=" + angle +
				", autoPair=" + autoPair +
				'}';
	}

	public int getTestType() {
		return testType;
	}

	public void setTestType(int testType) {
		this.testType = testType;
	}
}
