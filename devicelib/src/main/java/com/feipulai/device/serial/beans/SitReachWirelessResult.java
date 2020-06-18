package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by pengjf on 2020/4/20.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitReachWirelessResult {
    public SitReachWirelessResult(byte[] data) {
        power = (data[18] & 0xff);
        deviceId = (data[6] & 0xff);
        if (data[13] != 0xff){
            boolean isNegative = (data[13] & 0x40) == 0x40;
            capacity = (data[13] & 0x3f) * 256 + (data[14] & 0xff);
            if (isNegative){
                capacity = -capacity;
            }
        }
        state = (data[12] & 0xff);
        index = (data[7] & 0xff);
        hostId = data[5];
        if (index == 1 || index == 2){
            frequency = (data[12] & 0xff);
            velocity = (data[13] & 0xff);
        }
        LogUtils.normal("坐位体前屈返回设备数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

    }

    private int deviceId;
    private int power;
    private int state;//子设备状态（无效状态：0x00 准备就绪：0x01 推杆状态：0x02 测试状态：0x03 结束状态：0x04
    private int capacity;
    private int frequency;
    private int index;
    private int velocity;
    private int hostId;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }


    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    @Override
    public String toString() {
        return "SitReachWirelessResult{" +
                "deviceId=" + deviceId +
                ", power=" + power +
                ", state=" + state +
                ", capacity=" + capacity +
                ", frequency=" + frequency +
                ", index=" + index +
                ", velocity=" + velocity +
                ", hostId=" + hostId +
                '}';
    }
}

