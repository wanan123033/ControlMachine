package com.feipulai.exam.bean;

import java.io.Serializable;

/**
 * Created by zzs on  2021/1/5
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ActivateBean implements Serializable {

    private static final long serialVersionUID = -7023485467077162337L;
    private long activateMode; //激活方式（暂时不用）：0.后台激活，1.激活码激活
    private long activateTime = 0L; //激活时间，时间戳
    private long currentRunTime;//已运行时长，毫秒
    private String deviceIdentify;//设备唯一编码，安卓IMEI
    private String softwareCode;//软件编号
    private String softwareName;//软件名称
    private long validEndTime;//有效截止时间，时间戳
    private long validRunTime;//有效可运行时长 毫秒
    private long currentTime;//当前时间

    //本地使用天数 与更新时间
    private long useDeviceTime = 0;
    private long updateTime = 0L;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public long getActivateMode() {
        return activateMode;
    }

    public void setActivateMode(long activateMode) {
        this.activateMode = activateMode;
    }

    public long getActivateTime() {
        return activateTime;
    }

    public void setActivateTime(long activateTime) {
        this.activateTime = activateTime;
    }

    public long getCurrentRunTime() {
        return currentRunTime;
    }

    public void setCurrentRunTime(long currentRunTime) {
        this.currentRunTime = currentRunTime;
    }

    public String getDeviceIdentify() {
        return deviceIdentify;
    }

    public void setDeviceIdentify(String deviceIdentify) {
        this.deviceIdentify = deviceIdentify;
    }

    public String getSoftwareCode() {
        return softwareCode;
    }

    public void setSoftwareCode(String softwareCode) {
        this.softwareCode = softwareCode;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public long getValidEndTime() {
        return validEndTime;
    }

    public void setValidEndTime(long validEndTime) {
        this.validEndTime = validEndTime;
    }

    public long getValidRunTime() {
        return validRunTime;
    }

    public void setValidRunTime(long validRunTime) {
        this.validRunTime = validRunTime;
    }

    public long getUseDeviceTime() {
        return useDeviceTime;
    }

    public void setUseDeviceTime(long useDeviceTime) {
        this.useDeviceTime = useDeviceTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

}
