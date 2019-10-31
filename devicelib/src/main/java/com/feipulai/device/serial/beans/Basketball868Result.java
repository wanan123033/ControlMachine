package com.feipulai.device.serial.beans;

public class Basketball868Result {
    private int frequency;
    private int deviceId;
    //子设备状态
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

    public Basketball868Result(byte[] data) {
        state = data[12];
        sum = data[13];
        hour = data[14];
        minth = data[15];
        sencond = data[16];
        minsencond = data[17];
        deviceId = data[13];
        frequency = data[12];
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
}
