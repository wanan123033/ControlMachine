package com.feipulai.exam.activity.jump_rope.setting;

import com.feipulai.exam.config.TestConfigs;

/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class JumpRopeSetting{

	private int deviceSum = 20;
	private int testTime = 30;// 一轮测试的时间,单位为秒
	private int testNo = 1;// 允许测试的次数
	
	public static final int GROUP_PATTERN_SUCCESIVE = TestConfigs.GROUP_PATTERN_SUCCESIVE;
	public static final int GROUP_PATTERN_LOOP = TestConfigs.GROUP_PATTERN_LOOP;
	private int groupMode = GROUP_PATTERN_SUCCESIVE;
	
	private int deviceGroup = 0;
	private boolean autoPair = true;
	
	
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
				", testNo=" + testNo +
				", groupMode=" + groupMode +
				", deviceGroup=" + deviceGroup +
				", autoPair=" + autoPair +
				'}';
	}
	
}
