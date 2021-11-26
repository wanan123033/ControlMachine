package com.feipulai.exam.activity.sport_timer.bean;

public class SportTimerSetting {
    public boolean isAutoPair() {
        return isAutoPair;
    }

    public void setAutoPair(boolean autoPair) {
        isAutoPair = autoPair;
    }

    private boolean isAutoPair;
    private int deviceCount = 1;
    private int carryMode;//成绩进位
    private int digital;//成绩精度
    private int testTimes = 1;
    private String initRoute ;
    private int groupType;//0循环测试1连续测试
    private int minEidit = 1;
    private int sensity = 50;
    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public int getCarryMode() {
        return carryMode;
    }

    public void setCarryMode(int carryMode) {
        this.carryMode = carryMode;
    }

    public int getDigital() {
        return digital;
    }

    public void setDigital(int digital) {
        this.digital = digital;
    }

    public int getTestTimes() {
        return testTimes;
    }

    public void setTestTimes(int testTimes) {
        this.testTimes = testTimes;
    }

    public String getInitRoute() {
        return initRoute;
    }

    public void setInitRoute(String initRoute) {
        this.initRoute = initRoute;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public int getMinEidit() {
        return minEidit;
    }

    public void setMinEidit(int minEidit) {
        this.minEidit = minEidit;
    }

    public int getSensity() {
        return sensity;
    }

    public void setSensity(int sensity) {
        this.sensity = sensity;
    }
}
