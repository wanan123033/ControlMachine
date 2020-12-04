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
    private int degree;
    private int martType;
    private int testTimes = 1;
    private String initRoute ;
    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getMartType() {
        return martType;
    }

    public void setMartType(int martType) {
        this.martType = martType;
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
}
