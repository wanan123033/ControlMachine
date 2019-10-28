package com.feipulai.device.serial.beans;

public class VolleyPair868Result {
    private int state;
    private int score;
    private int childId;
    private int electricityState;

    private int deviceid;

    public static final int STATE_FREE = 0;       //空闲
    public static final int STATE_TIME_PREPARE = 1;  //计时准备
    public static final int STATE_TIMING = 2;      //计时中
    public static final int STATE_TIME_END = 3;    //计时结束
    public static final int STATE_COUNT_PREPARE = 0x11;  //计数准备
    public static final int STATE_COUNTING = 0x12;   //计数中
    public static final int STATE_COUNT_END = 0x13;  //计数结束

    public static final int ELECTRICITY_STATE_NOMAL = 0x81;  //电量充足
    public static final int ELECTRICITY_STATE_INADEQUATE = 0x80;  //电量不足

    public VolleyPair868Result(byte[] data) {
        state = data[12];
        score = data[13] * 0x0100 + data[14];
        deviceid = data[5];
        childId = data[6];
        electricityState = data[15];
    }
    public int getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public int getElectricityState() {
        return electricityState;
    }

    public int getDeviceid() {
        return deviceid;
    }

    public int getChildId() {
        return childId;
    }
}
