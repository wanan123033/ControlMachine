package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

public class ShoulderResult implements IDeviceResult{
    private byte [] data;
    private int hostId;
    private int state  = -1;
    private int battery;
    public ShoulderResult() {

    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getLongTime() {
        return longTime;
    }

    public void setLongTime(int longTime) {
        this.longTime = longTime;
    }
    private int sumTimes;
    private int currentTime;

    public int getSumTimes() {
        return sumTimes;
    }

    public void setSumTimes(int sumTimes) {
        this.sumTimes = sumTimes;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    private int deviceId;
    private int frequency;
    private int longTime;
    public ShoulderResult(byte[] data){
        this.data = data;
        switch (data[7]){
            case 14:
                if (data.length == 15 || data.length == 14){
                    deviceId = data[6];
                    frequency = data[10];
                    hostId = data[5];
                }
                break;
            case 23:
                if (data.length == 14){
                    deviceId = data[6];
                    frequency = data[10];
                    hostId = data[5];
                }
                break;
            case 1://同步时间

                break;
            case 2://获取时间
            case 4://获取状态
                if (data.length == 17){//【10：13】同步北京时间，4字节
                    deviceId = data[6];
                    hostId = data[5];
                    byte[] bytes = new byte[4];
                    bytes[0] = data[10];
                    bytes[1] = data[11];
                    bytes[2] = data[12];
                    bytes[3] = data[13];
                    longTime = byteToInt(bytes);
                }else if (data.length == 14){
                    deviceId = data[6];
                    hostId = data[5];
                    state = data[10];
                }
                break;
            case 0:
                if (data.length == 19){
                    deviceId = data[6];
                    hostId = data[5];
                    if (data[11] == 1){
                        state = 4;
                    }else {
                        state = 1;
                    }

                    battery = data[10];
                }
                break;
            case 9://缓冲区数据
                if (data.length == 21){
                    deviceId = data[6];
                    hostId = data[5];
                    sumTimes = data[10];
                    currentTime = data[11];
                    byte[] bytes = new byte[4];
                    bytes[0] = data[12];
                    bytes[1] = data[13];
                    bytes[2] = data[14];
                    bytes[3] = data[15];
                    longTime = byteToInt(bytes);
                }
                break;

        }
        LogUtils.normal("肩胛返回设备数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());
    }

    @Override
    public String toString() {
        return "ShoulderResult{" +
                "data=" + Arrays.toString(data) +
                ", hostId=" + hostId +
                ", sumTimes=" + sumTimes +
                ", currentTime=" + currentTime +
                ", deviceId=" + deviceId +
                ", frequency=" + frequency +
                ", longTime=" + longTime +
                ", state=" + state +
                ", battery=" + battery +
                '}';
    }

    /**
     * byte 转换成int
     * @param bytes
     * @return
     */
    private int byteToInt(byte [] bytes){//高位在前
        int int1=bytes[3]&0xff;
        int int2=(bytes[2]&0xff)<<8;
        int int3=(bytes[1]&0xff)<<16;
        int int4=(bytes[0]&0xff)<<24;
        return int1|int2|int3|int4;
    }

    /**
     * 将int转为高字节在前，低字节在后的byte数组（大端）
     * @param n int
     * @return byte[]
     */
    public static byte[] intToByteBig(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) (n >> 8 & 0xff);
        b[1] = (byte) (n >> 16 & 0xff);
        b[0] = (byte) (n >> 24 & 0xff);
        return b;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    @Override
    public int getResult() {
        return sumTimes;
    }

    @Override
    public void setResult(int result) {
        sumTimes = result;
    }
}
