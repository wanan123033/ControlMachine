package com.feipulai.exam.activity.jump_rope.bean;

import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.exam.entity.Student;

import java.io.Serializable;

public class StuDevicePair implements Serializable {

    private static final long serialVersionUID = -5114314856313860680L;
    //学生
    private Student student;
    //设备
    private BaseDeviceState baseDevice;
    //成绩
	private IDeviceResult deviceResult;
	//判罚
	private int penalty;
	private int battery;
	public IDeviceResult getDeviceResult(){
		return deviceResult;
	}
	
	public void setDeviceResult(IDeviceResult deviceResult){
		this.deviceResult = deviceResult;
	}

    public BaseDeviceState getBaseDevice() {
        return baseDevice;
    }

    public void setBaseDevice(BaseDeviceState baseDevice) {
        this.baseDevice = baseDevice;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
	
	public int getPenalty() {
		return penalty;
	}
	
	public void setPenalty(int penalty) {
		this.penalty = penalty;
	}
	
	@Override
	public String toString() {
		return "StuDevicePair{" +
				"student=" + student +
				", baseDevice=" + baseDevice +
				", deviceResult=" + deviceResult +
				", penalty=" + penalty +
				'}';
	}

	public int getBattery() {
		return battery;
	}

	public void setBattery(int battery) {
		this.battery = battery;
	}
}
