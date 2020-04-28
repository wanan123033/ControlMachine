package com.feipulai.device.serial.beans;

import com.orhanobut.logger.examlogger.LogUtils;

public class Basketball868Result {
    private int frequency;
    private int deviceId;
    //子设备状态
    /**
     * 离线：0x00
     * 空闲：0x01
     * 等待：0x02
     * 计时：0x03
     * 暂停：0x05（暂停显示时间，不停表只针对显示屏）
     * 结束：0x06
     */
    private int state;
    //拦截次数
    private int sum;
    //小时
    private int hour;
    //分钟
    private int minth;
    //秒
    private int sencond;
    //毫秒(精准度10ms)
    private int minsencond;

    private String serialNumber;//设备序列号
    private int deviceCode;// 3 子机 2 LED
    public final static int DEVICE_LED = 2;
    private int interceptSecond;//默认5秒
    private int sensitivity;//灵敏度
    private int uPrecision;//进位

    public Basketball868Result() {
    }

    public Basketball868Result(byte[] data) {
        state = data[12];
        sum = data[13];
        hour = data[14];
        minth = data[15];
        sencond = data[16];
        minsencond = data[17];
        if (data[7] == 0x02) {
            deviceId = data[15];
        } else {
            deviceId = data[6];
        }

        frequency = data[12];
        serialNumber = (data[8] & 0xff) + (data[9] & 0xff) + (data[10] & 0xff) + (data[11] & 0xff) + "";
        deviceCode = data[4];
        sensitivity = data[16] & 0xff;
        interceptSecond = data[17] & 0xff;

        if (data[7] == 0x0a) {
            sensitivity = data[13] & 0xff;
            interceptSecond = data[14] & 0xff;
            uPrecision = data[15] & 0xff;

        }

        LogUtils.normal("篮球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getState() {
        return state;
    }

    public int getSum() {
        return sum;
    }

    public int getHour() {
        return hour;
    }

    public int getMinth() {
        return minth;
    }

    public int getSencond() {
        return sencond;
    }

    public int getMinsencond() {
        return minsencond;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinth(int minth) {
        this.minth = minth;
    }

    public void setSencond(int sencond) {
        this.sencond = sencond;
    }

    public void setMinsencond(int minsencond) {
        this.minsencond = minsencond;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(int deviceCode) {
        this.deviceCode = deviceCode;
    }

    public int getInterceptSecond() {
        return interceptSecond;
    }

    public void setInterceptSecond(int interceptSecond) {
        this.interceptSecond = interceptSecond;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getuPrecision() {
        return uPrecision;
    }

    public void setuPrecision(int uPrecision) {
        this.uPrecision = uPrecision;
    }

    @Override
    public String toString() {
        return "Basketball868Result{" +
                "frequency=" + frequency +
                ", deviceId=" + deviceId +
                ", state=" + state +
                ", sum=" + sum +
                ", hour=" + hour +
                ", minth=" + minth +
                ", sencond=" + sencond +
                ", minsencond=" + minsencond +
                ", serialNumber='" + serialNumber + '\'' +
                ", deviceCode=" + deviceCode +
                ", interceptSecond=" + interceptSecond +
                ", sensitivity=" + sensitivity +
                '}';
    }


    public long getInterceptTime() {
        return hour * 60 * 60 * 1000 + minth * 60 * 1000 + sencond * 1000 + minsencond * 10;
    }
}
