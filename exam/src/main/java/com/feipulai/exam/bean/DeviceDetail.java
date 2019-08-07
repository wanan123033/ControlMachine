package com.feipulai.exam.bean;

import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.entity.RoundResult;

/**
 * Created by pengjf on 2019/7/31.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DeviceDetail {
    public DeviceDetail(){
        BaseStuPair baseStuPair = new BaseStuPair();
        baseStuPair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_FREE));
    }
    private BaseStuPair baseStuPair;
    private RoundResult roundResult;
    private boolean isDeviceOpen;

    public BaseStuPair getStuDevicePair() {
        return baseStuPair;
    }

    public void setBaseStuPair(BaseStuPair baseStuPair) {
        this.baseStuPair = baseStuPair;
    }

    public RoundResult getRoundResult() {
        return roundResult;
    }

    public void setRoundResult(RoundResult roundResult) {
        this.roundResult = roundResult;
    }

    public boolean isDeviceOpen() {
        return isDeviceOpen;
    }

    public void setDeviceOpen(boolean deviceOpen) {
        isDeviceOpen = deviceOpen;
    }


}
