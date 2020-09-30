package com.feipulai.host.activity.vision.Radio;

import java.io.Serializable;

/**
 * Created by zzs on  2020/9/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VisionSetting implements Serializable {


    private int distance = 0;//  0 : 5米 1：2.5米 2：1米

    private int testType = 0;// 0 ：5分  1 ： 小数

    private int stopTime = 0;//0不限时

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getTestType() {
        return testType;
    }

    public void setTestType(int testType) {
        this.testType = testType;
    }

    public int getStopTime() {
        return stopTime;
    }

    public void setStopTime(int stopTime) {
        this.stopTime = stopTime;
    }
}
