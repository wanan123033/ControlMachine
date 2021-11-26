package com.feipulai.exam.activity.pushUp.distance;

public class PushUpDistanceSetting {
    private int testNo;
    private int deviceSum;
    private int groupMode;
    private int testTime;
    private boolean autoPair;
    private boolean penalize;

    public void setTestNo(int testNo) {
        this.testNo = testNo;
    }

    public int getTestNo() {
        return testNo;
    }

    public void setDeviceSum(int deviceSum) {
        this.deviceSum = deviceSum;
    }

    public int getDeviceSum() {
        return deviceSum;
    }

    public void setGroupMode(int groupMode) {
        this.groupMode = groupMode;
    }

    public int getGroupMode() {
        return groupMode;
    }

    public void setTestTime(int testTime) {
        this.testTime = testTime;
    }

    public int getTestTime() {
        return testTime;
    }

    public void setAutoPair(boolean autoPair) {
        this.autoPair = autoPair;
    }

    public boolean getAutoPair() {
        return autoPair;
    }

    public boolean isPenalize() {
        return penalize;
    }

    public void setPenalize(boolean penalize) {
        this.penalize = penalize;
    }

    public int getAngle() {
        return 0;
    }
}
