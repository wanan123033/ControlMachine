package com.feipulai.host.activity.vccheck;

/**
 * Created by pengjf on 2020/6/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripSetting {
    private int deviceSum = 4;
    private boolean autoPair = true;
    private int testRound = 1;
    /**
     * 分组测试模式 0 连续 1 循环
     */
    private int testPattern = 0;
    public int getDeviceSum(){
        return deviceSum;
    }

    public void setDeviceSum(int deviceSum){
        this.deviceSum = deviceSum;
    }

    public boolean isAutoPair(){
        return autoPair;
    }

    public void setAutoPair(boolean autoPair){
        this.autoPair = autoPair;
    }



    @Override
    public String toString() {
        return "SitUpSetting{" +
                "deviceSum=" + deviceSum +
                ", autoPair=" + autoPair +
                ", testPattern=" + testPattern +
                '}';
    }

    public int getTestRound() {
        return testRound;
    }

    public void setTestRound(int testRound) {
        this.testRound = testRound;
    }

    public int getTestPattern() {
        return testPattern;
    }

    public void setTestPattern(int testPattern) {
        this.testPattern = testPattern;
    }
}
