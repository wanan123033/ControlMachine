package com.feipulai.exam.activity.situp.newSitUp;

public class ShoulderSitUp {
    private int deviceId;
    private int deviceResult;
    private boolean sitUpUpdate;
    private int back;//背部
    private int waist;//腰部

    public int getBack() {
        return back;
    }

    public void setBack(int back) {
        this.back = back;
    }

    public int getWaist() {
        return waist;
    }

    public void setWaist(int waist) {
        this.waist = waist;
    }

    public ShoulderSitUp(int deviceId, boolean sitUpUpdate, boolean shoulderUpdate) {
        this.deviceId = deviceId;
        this.sitUpUpdate = sitUpUpdate;
        this.shoulderUpdate = shoulderUpdate;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isSitUpUpdate() {
        return sitUpUpdate;
    }

    public void setSitUpUpdate(boolean sitUpUpdate) {
        this.sitUpUpdate = sitUpUpdate;
    }

    public boolean isShoulderUpdate() {
        return shoulderUpdate;
    }

    public void setShoulderUpdate(boolean shoulderUpdate) {
        this.shoulderUpdate = shoulderUpdate;
    }

    private boolean shoulderUpdate;

    public int getDeviceResult() {
        return deviceResult;
    }

    public void setDeviceResult(int deviceResult) {
        this.deviceResult = deviceResult;
    }
}
