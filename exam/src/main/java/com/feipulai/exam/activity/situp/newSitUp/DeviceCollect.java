package com.feipulai.exam.activity.situp.newSitUp;

import com.feipulai.device.serial.beans.ShoulderResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;

public class DeviceCollect {
    private ShoulderResult shoulderResult;
    private SitPushUpStateResult sitPushUpStateResult;

    public DeviceCollect(SitPushUpStateResult sitPushUpStateResult, ShoulderResult armStateResult) {
        this.shoulderResult = armStateResult;
        this.sitPushUpStateResult = sitPushUpStateResult;
    }

    public ShoulderResult getShoulderResult() {
        return shoulderResult;
    }

    public void setShoulderResult(ShoulderResult shoulderResult) {
        this.shoulderResult = shoulderResult;
    }

    public SitPushUpStateResult getSitPushUpStateResult() {
        return sitPushUpStateResult;
    }

    public void setSitPushUpStateResult(SitPushUpStateResult sitPushUpStateResult) {
        this.sitPushUpStateResult = sitPushUpStateResult;
    }


}
