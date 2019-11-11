package com.feipulai.exam.activity.basketball.bean;

import com.feipulai.exam.activity.jump_rope.bean.BaseDeviceState;

/**
 * Created by zzs on  2019/11/4
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BallDeviceState extends BaseDeviceState {

    private String deviceSerial;//序列号

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }
}
