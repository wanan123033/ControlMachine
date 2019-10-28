package com.feipulai.device.serial.beans;

public class Basketball868Result {
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

    public byte[] getData() {
        return data;
    }

    //子设备状态
    public int state;

    //拦截次数
    public int sum;

    //小时
    public int hour;

    //分钟
    public int minth;

    //秒
    public int sencond;

    //毫秒(精准度10ms)
    public int minsencond;

    public byte[] data;
    public Basketball868Result(byte[] data) {
        state = data[12];
        sum = data[13];
        hour = data[14];
        minth = data[15];
        sencond = data[16];
        minsencond = data[17];
        this.data = data;
    }
}
