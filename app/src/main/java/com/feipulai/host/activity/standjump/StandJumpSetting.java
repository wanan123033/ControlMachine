package com.feipulai.host.activity.standjump;

/**
 * Created by zzs on 2018/11/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpSetting {

    /**
     * 测试设置数量
     */
    private int testDeviceCount = 1;

    /**
     * 测试次数
     */
    private int testCount = 1;
    /***
     *
     * 测试杆数 点数（1 杆 100个点）
     */
    private int testPoints = 3;
    /**
     * 测试范围
     */
    private int pointsScope;


    public int getPointsScope() {
        return pointsScope;
    }

    public void setPointsScope(int pointsScope) {
        this.pointsScope = pointsScope;
    }

    public int getTestCount() {
        return testCount;
    }

    public void setTestCount(int testCount) {
        this.testCount = testCount;
    }

    public int getTestDeviceCount() {
        return testDeviceCount;
    }

    public void setTestDeviceCount(int testDeviceCount) {
        this.testDeviceCount = testDeviceCount;
    }

    public int getTestPoints() {
        return testPoints;
    }

    public void setTestPoints(int testPoints) {
        this.testPoints = testPoints;
    }

    @Override
    public String toString() {
        return "StandJumpSetting{" +
                "testDeviceCount=" + testDeviceCount +
                ", testCount=" + testCount +
                ", testPoints=" + testPoints +
                ", pointsScope=" + pointsScope +
                '}';
    }
}
