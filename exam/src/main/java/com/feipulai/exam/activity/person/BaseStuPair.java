package com.feipulai.exam.activity.person;

import com.feipulai.exam.entity.Student;

import java.io.Serializable;


/**
 * Created by zzs on 2018/7/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseStuPair implements Serializable {

    private static final long serialVersionUID = -5114314856313860680L;
    //成绩
    private int result;
    private int resultState = 0;//成绩状态 //是否犯规 0:未检录 1:正常 2:犯规 3:中退 4:弃权 5:测试  -99已测试（循环测试区分使用）

    private boolean isFullMark;
    private int trackNo;//道次
    //学生
    private Student student;
    //设备
    private BaseDeviceState baseDevice;
    //轮次成绩
    private String[] timeResult;
    //可测试
    private boolean canTest ;
    //可检入
    private boolean canCheck ;
    //是否最好
    private boolean notBest ;
    public BaseStuPair(int result, int resultState, Student student, BaseDeviceState baseDevice) {
        this.result = result;
        this.resultState = resultState;
        this.student = student;
        this.baseDevice = baseDevice;
    }

    public BaseStuPair(Student student, BaseDeviceState baseDevice) {
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


    public String[] getTimeResult() {
        return timeResult;
    }

    public void setTimeResult(String[] timeResult) {
        this.timeResult = timeResult;
    }

    public boolean isFullMark() {
        return isFullMark;
    }

    public void setFullMark(boolean fullMark) {
        isFullMark = fullMark;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public static final String HAND_BASE_PAIRS = "HAND_BASE_PAIRS";

    public int getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }


    public boolean isCanTest() {
        return canTest;
    }

    public void setCanTest(boolean canTest) {
        this.canTest = canTest;
    }

    public boolean isCanCheck() {
        return canCheck;
    }

    public void setCanCheck(boolean canCheck) {
        this.canCheck = canCheck;
    }

    public boolean isNotBest() {
        return notBest;
    }

    public void setNotBest(boolean notBest) {
        this.notBest = notBest;
    }
}
