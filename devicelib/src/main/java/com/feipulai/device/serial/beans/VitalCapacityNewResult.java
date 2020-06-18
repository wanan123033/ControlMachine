package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * 作者 pengjf
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2019/02/13.
 */

public class VitalCapacityNewResult {

	public VitalCapacityNewResult(byte [] data){
		power = (data[15]&0xff);
		deviceId = (data[6]&0xff);
		capacity = ((data[13] & 0xff) << 8) + (data[14] & 0xff);
		state = (data[12]&0xff);
		index = (data[7]&0xff);
		if (index == 1 || index == 2)
		    frequency = (data[12]&0xff);
		    hostId = data[5];
		velocity = (data[13]&0xff);
		LogUtils.normal("肺活量返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}
	private int deviceId ;
	private int power ;
	private int state ;
	private int capacity ;
	private int frequency ;
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
		return "VitalCapacityNewResult{" +
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
