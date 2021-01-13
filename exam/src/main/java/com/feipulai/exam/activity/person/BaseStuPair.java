package com.feipulai.exam.activity.person;

import com.feipulai.exam.entity.Student;

import java.io.Serializable;
import java.util.Arrays;


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
    private int baseHeight;
    private boolean devicePairState = false;  //true 已配对  false 没有配对
    private int penaltyNum;

    private int roundNo;  //轮次
    private boolean LEDupdate = true;
    private int time;
    private String testTime; //测试当前轮次的开始时间
    private String endTime; // 结束时间

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
        this.baseDevice = new BaseDeviceState();
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

    public int getBaseHeight() {
        return baseHeight;
    }

    public void setBaseHeight(int baseHeight) {
        this.baseHeight = baseHeight;
    }

    @Override
    public String toString() {
        return "BaseStuPair{" +
                "result=" + result +
                ", resultState=" + resultState +
                ", isFullMark=" + isFullMark +
                ", trackNo=" + trackNo +
                ", student=" + student +
                ", baseDevice=" + baseDevice +
                ", timeResult=" + Arrays.toString(timeResult) +
                ", canTest=" + canTest +
                ", canCheck=" + canCheck +
                ", notBest=" + notBest +
                ", baseHeight=" + baseHeight +
                '}';
    }


    public void setDevicePairState(boolean devicePairState) {
        this.devicePairState = devicePairState;
    }

    public boolean getDevicePairState() {
        return devicePairState;
    }

    public int getPenaltyNum() {
        return penaltyNum;
    }
    public void setPenaltyNum(int penaltyNum) {
        this.penaltyNum = penaltyNum;
    }

    public int getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(int roundNo) {
        this.roundNo = roundNo;
    }

    public void setLEDupdate(boolean leDupdate) {
        this.LEDupdate = leDupdate;
    }

    public boolean getLEDupdate() {
        return LEDupdate;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTestTime() {
        return testTime;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }


    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

}
