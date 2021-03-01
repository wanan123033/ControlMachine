package com.feipulai.exam.activity.situp.newSitUp;

import com.feipulai.device.serial.beans.ArmStateResult;
import com.feipulai.device.serial.beans.SitPushUpStateResult;

public class DeviceCollect {
    private ArmStateResult armStateResult;
    private SitPushUpStateResult sitPushUpStateResult;

    public DeviceCollect(SitPushUpStateResult sitPushUpStateResult, ArmStateResult armStateResult) {
        this.armStateResult = armStateResult;
        this.sitPushUpStateResult = sitPushUpStateResult;
    }

    public ArmStateResult getArmStateResult() {
        return armStateResult;
    }

    public void setArmStateResult(ArmStateResult armStateResult) {
        this.armStateResult = armStateResult;
    }

    public SitPushUpStateResult getSitPushUpStateResult() {
        return sitPushUpStateResult;
    }

    public void setSitPushUpStateResult(SitPushUpStateResult sitPushUpStateResult) {
        this.sitPushUpStateResult = sitPushUpStateResult;
    }


}
