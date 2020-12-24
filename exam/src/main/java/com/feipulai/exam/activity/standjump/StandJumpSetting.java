package com.feipulai.exam.activity.standjump;

/**
 * Created by zzs on 2018/11/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class StandJumpSetting {

    /**
     * 测试设置数量
     */
    private int testDeviceCount = 4;

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

    private int[] testPointsArray = new int[]{3, 3, 3, 3};
    private int[] pointsScopeArray = new int[4];

    /**
     * 是否开启判罚
     */
    private boolean isPenalize;

    //就否自动配对
    private boolean autoPair = true;
    //测试模式 0 有线 1 无线
    private int testType = 0;
    private boolean penalizeFoul;


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

    public boolean isAutoPair() {
        return autoPair;
    }

    public void setAutoPair(boolean autoPair) {
        this.autoPair = autoPair;
    }

    public int getTestType() {
        return testType;
    }

    public void setTestType(int testType) {
        this.testType = testType;
    }

    public int[] getTestPointsArray() {
        return testPointsArray;
    }

    public void setTestPointsArray(int[] testPointsArray) {
        this.testPointsArray = testPointsArray;
    }

    public int[] getPointsScopeArray() {
        return pointsScopeArray;
    }

    public void setPointsScopeArray(int[] pointsScopeArray) {
        this.pointsScopeArray = pointsScopeArray;
    }

    public int getPoints() {
        if (pointsScope > 0) {
            return pointsScope;
        } else {
            return testPoints * 100;
        }
    }

    public boolean isPenalizeFoul() {
        return penalizeFoul;
    }

    public void setPenalizeFoul(boolean penalizeFoul) {
        this.penalizeFoul = penalizeFoul;
    }
}
