package com.feipulai.exam.activity.grip;

/**
 * Created by pengjf on 2020/6/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class GripSetting {
    private int deviceSum = 4;
    private boolean autoPair = true;
    private int testRound = 1;
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
                '}';
    }

    public int getTestRound() {
        return testRound;
    }

    public void setTestRound(int testRound) {
        this.testRound = testRound;
    }
}
