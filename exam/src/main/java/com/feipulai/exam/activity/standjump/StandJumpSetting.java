package com.feipulai.exam.activity.standjump;

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
    /**
     * 是否满分跳过
     */
    private boolean isFullReturn = false;
    /**
     * 男满分值
     */
    private int manFull = 0;
    /**
     * 女满分值
     */
    private int womenFull = 0;
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = 0;

    /***
     *
     * 测试杆数 点数（1 杆 100个点）
     */
    private int testPoints = 3;
    /**
     * 测试范围
     */
    private int pointsScope;
    /**
     * 是否开启判罚
     */
    private boolean isPenalize;

    public boolean isPenalize() {
        return isPenalize;
    }

    public void setPenalize(boolean penalize) {
        isPenalize = penalize;
    }

    public int getPointsScope() {
        return pointsScope;
    }

    public void setPointsScope(int pointsScope) {
        this.pointsScope = pointsScope;
    }

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
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

    public boolean isFullReturn() {
        return isFullReturn;
    }

    public void setFullReturn(boolean fullReturn) {
        isFullReturn = fullReturn;
    }

    public int getManFull() {
        return manFull;
    }

    public void setManFull(int manFull) {
        this.manFull = manFull;
    }

    public int getWomenFull() {
        return womenFull;
    }

    public void setWomenFull(int womenFull) {
        this.womenFull = womenFull;
    }

    public int getTestPoints() {
        return testPoints;
    }

    public void setTestPoints(int testPoints) {
        this.testPoints = testPoints;
    }
}
