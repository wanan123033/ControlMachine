package com.feipulai.host.activity.jump_rope.bean;

import com.feipulai.device.serial.beans.IDeviceResult;
import com.feipulai.host.entity.Student;

import java.io.Serializable;

public class StuDevicePair implements Serializable {

    private static final long serialVersionUID = -5114314856313860680L;
    //学生
    private Student student;
    //设备
    private BaseDeviceState baseDevice;
    //成绩
	private IDeviceResult deviceResult;
	private long startTime;
	private long endTime;
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
	
	@Override
	public String toString() {
		return "StuDevicePair{" +
				"student=" + student +
				", baseDevice=" + baseDevice +
				", deviceResult=" + deviceResult +
				'}';
	}

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
