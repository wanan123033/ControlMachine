package com.feipulai.testandroid.activity;

/**
 * Created by pengjf on 2019/10/16.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VcWrapper {
    public VcWrapper(int deviceId , int result){
        this.deviceId = deviceId;
        this.result = result ;
    }
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    private int deviceId;
    private int result;
}
