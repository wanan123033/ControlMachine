package com.feipulai.host.activity.medicine_ball;

/**
 * Created by zzs on 2018/11/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class MedicineBallSetting {
    private int testType = 0;
    /**
     * 测试设置数量
     */
    private int testDeviceCount = 1;

    /**
     * 测试次数
     */
    private int testCount = 1;

    /**
     * 基础距离
     */
    private int basePoint;
    private boolean autoPair;

    public int getBasePoint() {
        return basePoint;
    }

    public void setBasePoint(int basePoint) {
        this.basePoint = basePoint;
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



    @Override
    public String toString() {
        return "StandJumpSetting{" +
                "testDeviceCount=" + testDeviceCount +
                ", testCount=" + testCount +
                ", basePoint=" + basePoint +
                '}';
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
}
