package com.feipulai.host.activity.sitreach;

/**
 * Created by zzs on 2018/11/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachSetting {

    /**
     * 测试设置数量
     */
    private int testDeviceCount = 1;

    private boolean autoPair;
    private int testType;
    //有线
    public static final int WIRED_TYPE = 0;
    //无线
    public static final int WIRELESS_TYPE = 1;



    public int getTestDeviceCount() {
        return testDeviceCount;
    }

    public void setTestDeviceCount(int testDeviceCount) {
        this.testDeviceCount = testDeviceCount;
    }


    @Override
    public String toString() {
        return "SitReachSetting{" +
                "testDeviceCount=" + testDeviceCount +
                '}';
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
}
