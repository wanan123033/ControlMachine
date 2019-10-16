package com.feipulai.exam.activity.sitreach;

/**
 * Created by zzs on 2018/11/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachSetting {

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
    private double manFull = 0;
    /**
     * 女满分值
     */
    private double womenFull = 0;
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = 0;

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

    public double getManFull() {
        return manFull;
    }

    public void setManFull(double manFull) {
        this.manFull = manFull;
    }

    public double getWomenFull() {
        return womenFull;
    }

    public void setWomenFull(double womenFull) {
        this.womenFull = womenFull;
    }

    @Override
    public String toString() {
        return "SitReachSetting{" +
                "testDeviceCount=" + testDeviceCount +
                ", testCount=" + testCount +
                ", isFullReturn=" + isFullReturn +
                ", manFull=" + manFull +
                ", womenFull=" + womenFull +
                ", testPattern=" + testPattern +
                '}';
    }
}
