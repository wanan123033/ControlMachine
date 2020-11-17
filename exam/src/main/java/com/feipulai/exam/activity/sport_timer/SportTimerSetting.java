package com.feipulai.exam.activity.sport_timer;

public class SportTimerSetting {
    public boolean isAutoPair() {
        return isAutoPair;
    }

    public void setAutoPair(boolean autoPair) {
        isAutoPair = autoPair;
    }

    private boolean isAutoPair;
    private int deviceCount;

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }
}
