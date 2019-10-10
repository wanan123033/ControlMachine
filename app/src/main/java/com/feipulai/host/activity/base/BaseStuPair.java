package com.feipulai.host.activity.base;

import com.feipulai.host.entity.Student;

import java.io.Serializable;

/**
 * Created by zzs on 2018/7/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseStuPair implements Serializable {

    private static final long serialVersionUID = -5114314856313860680L;
    //成绩
    private int result;
    private int resultState;//成绩状态 0正常  -1犯规    -2中退    -3放弃
    //学生
    private Student student;
    //设备
    private BaseDeviceState baseDevice;
    //轮次成绩
    private String[] timeResult;
    private boolean canTest;
    private int baseHeight;
    public BaseStuPair(int result, int resultState, Student student, BaseDeviceState baseDevice) {
        this.result = result;
        this.resultState = resultState;
        this.student = student;
        this.baseDevice = baseDevice;
    }

    public BaseStuPair() {
    }

    public int getResultState() {
        return resultState;
    }

    public void setResultState(int resultState) {
        this.resultState = resultState;
    }

    public BaseDeviceState getBaseDevice() {
        return baseDevice;
    }

    public void setBaseDevice(BaseDeviceState baseDevice) {
        this.baseDevice = baseDevice;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public static final String HAND_BASE_PAIRS = "HAND_BASE_PAIRS";

    public String[] getTimeResult() {
        return timeResult;
    }

    public void setTimeResult(String[] timeResult) {
        this.timeResult = timeResult;
    }

    public boolean isCanTest() {
        return canTest;
    }

    public void setCanTest(boolean canTest) {
        this.canTest = canTest;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public void setBaseHeight(int baseHeight) {
        this.baseHeight = baseHeight;
    }
}
