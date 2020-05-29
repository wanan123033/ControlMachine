package com.feipulai.device.udp.result;

import com.feipulai.device.serial.beans.IDeviceResult;

/**
 * Created by zzs on  2019/5/29
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BasketballResult implements IDeviceResult {

    /**
     * 指令类型
     */
    private int type;

    /**
     * 时
     */
    private int hour;
    /**
     * 分
     */
    private int minute;
    /**
     * 秒
     */
    private int second;
    /**
     * 毫秒
     */
    private int hund;
    /**
     * 精度  0表示设置显示精度为十分秒
     * 1表示设置显示精度为百分秒
     */
    private int uPrecision;
    /**
     * 遮挡1,2
     */
    private int ucFD;
    /**
     * 1:1号传感器 2:2号传感器
     */
    private int tNum;
    /**
     * 标志
     */
    private int flag;
    /**
     * 设备工作状态
     * STATUS_FREE		1		//FREE
     * STATUS_WAIT  		2		//WAIT To Start
     * STATUS_RUNING 	3		//Start Run
     * STATUS_PREP  		4		//Prepare to stop
     * STATUS_PAUSE 		5		//Display stop time,But Timer is Running
     */
    private int ucStatus;

    @Override
    public int getResult() {
        return (hour * 60 * 60 * 1000) + (minute * 60 * 1000) + (second * 1000) + (hund);
    }

    @Override
    public void setResult(int result) {

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getHund() {
        return hund;
    }

    public void setHund(int hund) {
        this.hund = hund;
    }

    public int getuPrecision() {
        return uPrecision;
    }

    public void setuPrecision(int uPrecision) {
        this.uPrecision = uPrecision;
    }

    public int getUcFD() {
        return ucFD;
    }

    public void setUcFD(int ucFD) {
        this.ucFD = ucFD;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getUcStatus() {
        return ucStatus;
    }

    public void setUcStatus(int ucStatus) {
        this.ucStatus = ucStatus;
    }

    public int gettNum() {
        return tNum;
    }

    public void settNum(int tNum) {
        this.tNum = tNum;
    }

    @Override
    public String toString() {
        return "BasketballResult{" +
                "type=" + type +
                ", hour=" + hour +
                ", minute=" + minute +
                ", second=" + second +
                ", hund=" + hund +
                ", uPrecision=" + uPrecision +
                ", ucFD=" + ucFD +
                ", tNum=" + tNum +
                ", flag=" + flag +
                ", ucStatus=" + ucStatus +
                '}';
    }


    public String showTime() {
        String time;
        if (hour > 0) {
            time = hour + ":" + minute + ":" + second + "." + hund;
            return time;
        }
        if (minute > 0) {
            time = minute + ":" + second + "." + hund;
            return time;
        }

        if (second > 0) {
            time = second + "." + hund;
            return time;
        }
        return "";
    }
}
