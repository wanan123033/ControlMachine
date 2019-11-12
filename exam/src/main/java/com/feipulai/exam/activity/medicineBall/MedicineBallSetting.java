package com.feipulai.exam.activity.medicineBall;

/**
 * Created by pengjf on 2018/12/3.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class MedicineBallSetting {
    private String maleFull ;
    private String femaleFull ;
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
    private int spDeviceCount =1;
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = 0;
    private boolean isPenalize;
    private int connectType = 0;

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
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

    public boolean isPenalize() {
        return isPenalize;
    }

    public void setPenalize(boolean penalize) {
        isPenalize = penalize;
    }

    public int getConnectType() {
        return connectType;
    }

    public void setConnectType(int connectType) {
        this.connectType = connectType;
    }
}
