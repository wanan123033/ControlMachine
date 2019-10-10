package com.feipulai.host.entity;

import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;

/**
 * Created by pengjf on 2019/7/31.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DeviceDetail {
    public DeviceDetail(){
        baseStuPair = new BaseStuPair();
        baseStuPair.setCanTest(true);
        baseStuPair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
    }
    private BaseStuPair baseStuPair;
    private RoundResult roundResult;
    private boolean isDeviceOpen;
    private int round;
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


    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }
}
