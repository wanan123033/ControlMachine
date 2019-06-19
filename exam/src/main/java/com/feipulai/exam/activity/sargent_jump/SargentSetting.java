package com.feipulai.exam.activity.sargent_jump;

/**
 * Created by pengjf on 2019/5/16.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SargentSetting {

    //1无线 0有线（初始化有线）
    private int type = 0 ;
    /**
     * 助跑 0原地起跳 1助跑（初始化助跑）
     */
    private int runUp = 1;
    private String maleFull;
    /**
     * 是否满分返回
     */
    boolean isFullReturn = false;
    /**
     * 测试次数
     */
    private int testTimes = 1;
    /**
     * 设备数量
     */
    private int spDeviceCount = 1;
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = 0;
    private String femaleFull;

    public int getBaseHeight() {
        return baseHeight;
    }

    public void setBaseHeight(int baseHeight) {
        this.baseHeight = baseHeight;
    }

    private int baseHeight;

    public int getRunUp() {
        return runUp;
    }

    public void setRunUp(int runUp) {
        this.runUp = runUp;
    }

    public String getMaleFull() {
        return maleFull;
    }

    public void setMaleFull(String maleFull) {
        this.maleFull = maleFull;
    }

    public String getFemaleFull() {
        return femaleFull;
    }

    public void setFemaleFull(String femaleFull) {
        this.femaleFull = femaleFull;
    }

    public boolean isFullReturn() {
        return isFullReturn;
    }

    public void setFullReturn(boolean fullReturn) {
        isFullReturn = fullReturn;
    }

    public int getTestTimes() {
        return testTimes;
    }

    public void setTestTimes(int testTimes) {
        this.testTimes = testTimes;
    }

    public int getSpDeviceCount() {
        return spDeviceCount;
    }

    public void setSpDeviceCount(int spDeviceCount) {
        this.spDeviceCount = spDeviceCount;
    }

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
