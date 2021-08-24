package com.feipulai.exam.activity.pushUp;

import com.feipulai.exam.config.TestConfigs;

/**
 * 俯卧撑设置
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class PushUpSetting {
    //有线
    public static final int WIRED_TYPE = 0;
    //无线
    public static final int WIRELESS_TYPE = 1;
    public static final int MAX_DEVICE = 28;
    public static final int MAX_INTERVAL_TIME = 15;
    public static final int NO_TIME_LIMIT = 0;
    private int testTime = NO_TIME_LIMIT;// 一轮测试的时间,单位为秒
    private int testNo = 1;// 允许测试的次数
    private int groupMode = TestConfigs.GROUP_PATTERN_SUCCESIVE;
    private int deviceSum = 5;
    /**
     * 配对设置 是否自动配对
     */
    private boolean autoPair = true;
    /**
     * 满分跳过
     */
    private boolean fullSkip = false;
    private int maleFullScore;
    private int femaleFullScore;
    /**
     * 间隔时间
     */
    private int intervalTime = 0;
    /**
     * 超时处理 0 停止 1 不计数
     */
    private int timeoutDispose = 0;

    private int testType = 0;//0 有线 1 无线  2  无线距离

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

    public boolean isFullSkip() {
        return fullSkip;
    }

    public void setFullSkip(boolean fullSkip) {
        this.fullSkip = fullSkip;
    }

    public int getMaleFullScore() {
        return maleFullScore;
    }

    public void setMaleFullScore(int maleFullScore) {
        this.maleFullScore = maleFullScore;
    }

    public int getFemaleFullScore() {
        return femaleFullScore;
    }

    public void setFemaleFullScore(int femaleFullScore) {
        this.femaleFullScore = femaleFullScore;
    }


    public int getIntervalTime() {
        return intervalTime;
    }

    public void setIntervalTime(int intervalTime) {
        this.intervalTime = intervalTime;
    }

    public int getTimeoutDispose() {
        return timeoutDispose;
    }

    public void setTimeoutDispose(int timeoutDispose) {
        this.timeoutDispose = timeoutDispose;
    }

    public int getDeviceSum() {
        return deviceSum;
    }

    public void setDeviceSum(int deviceSum) {
        this.deviceSum = deviceSum;
    }

    public int getTestType() {
        return testType;
    }

    public void setTestType(int testType) {
        this.testType = testType;
    }

    public boolean isAutoPair() {
        return autoPair;
    }

    public void setAutoPair(boolean autoPair) {
        this.autoPair = autoPair;
    }

    @Override
    public String toString() {
        return "PushUpSetting{" +
                "testTime=" + testTime +
                ", testNo=" + testNo +
                ", groupMode=" + groupMode +
                ", deviceSum=" + deviceSum +
                ", autoPair=" + autoPair +
                ", fullSkip=" + fullSkip +
                ", maleFullScore=" + maleFullScore +
                ", femaleFullScore=" + femaleFullScore +
                ", intervalTime=" + intervalTime +
                ", timeoutDispose=" + timeoutDispose +
                ", testType=" + testType +
                '}';
    }
}
