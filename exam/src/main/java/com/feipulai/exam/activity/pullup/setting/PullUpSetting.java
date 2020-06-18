package com.feipulai.exam.activity.pullup.setting;

import com.feipulai.exam.config.TestConfigs;

/**
 * Created by James on 2019/1/17 0017.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PullUpSetting {

	private int deviceSum = 5;
	private int testTime = 30;// 一轮测试的时间,单位为秒
	private int testNo = 1;// 允许测试的次数
	private int groupMode = TestConfigs.GROUP_PATTERN_SUCCESIVE;
	private boolean autoPair = true;
	private boolean isPenalize;
	private boolean isCountless;// 是否为不计时模式
    private int angle = 65;
    private boolean handCheck;
	public boolean isCountless() {
		return isCountless;
	}

	public void setCountless(boolean countless) {
		isCountless = countless;
	}

	public int getDeviceSum() {
		return deviceSum;
	}

	public void setDeviceSum(int deviceSum) {
		this.deviceSum = deviceSum;
	}

	public int getTestTime() {
		return testTime;
	}

	public void setTestTime(int testTime) {
		this.testTime = testTime;
	}

	public int getTestNo() {
		return testNo;
	}

	public void setTestNo(int testNo) {
		this.testNo = testNo;
	}

	public int getGroupMode() {
		return groupMode;
	}

	public void setGroupMode(int groupMode) {
		this.groupMode = groupMode;
	}

	public boolean isAutoPair() {
		return autoPair;
	}

	public void setAutoPair(boolean autoPair) {
		this.autoPair = autoPair;
	}

	public boolean isPenalize() {
		return isPenalize;
	}

	public void setPenalize(boolean penalize) {
		isPenalize = penalize;
	}

	@Override
	public String toString() {
		return "PullUpSetting{" +
				"deviceSum=" + deviceSum +
				", testTime=" + testTime +
				", testNo=" + testNo +
				", groupMode=" + groupMode +
				", autoPair=" + autoPair +
				'}';
	}

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public boolean isHandCheck() {
        return handCheck;
    }

    public void setHandCheck(boolean handCheck) {
        this.handCheck = handCheck;
    }
}
