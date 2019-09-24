package com.feipulai.host.activity.situp;

import com.feipulai.device.serial.beans.SitPushUpStateResult;
import com.feipulai.host.entity.Student;

import java.io.Serializable;
@Deprecated
/**
 * Created by James on 2018/3/9 0009.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class SitUpStuPair implements Serializable{
	
	private static final long serialVersionUID = 5110096390585085337L;
	
	private SitPushUpStateResult mDeviceState;
	private Student mStudent;
	
	public SitUpStuPair(SitPushUpStateResult deviceState,Student student){
		mDeviceState = deviceState;
		mStudent = student;
	}
	
	public SitPushUpStateResult getDeviceState(){
		return mDeviceState;
	}
	
	public void setDeviceState(SitPushUpStateResult deviceState){
		mDeviceState = deviceState;
	}
	
	public Student getStudent(){
		return mStudent;
	}
	
	public void setStudent(Student student){
		mStudent = student;
	}
	
}
