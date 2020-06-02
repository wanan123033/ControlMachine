package com.feipulai.host.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipulai.host.activity.base.BaseDeviceState;
import com.feipulai.host.activity.base.BaseStuPair;
import com.feipulai.host.entity.RoundResult;

/**
 * Created by pengjf on 2019/7/31.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DeviceDetail implements MultiItemEntity {


    public DeviceDetail(){
        baseStuPair = new BaseStuPair();
        baseStuPair.setCanTest(true);
        baseStuPair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
    }
    private BaseStuPair baseStuPair;
    private RoundResult roundResult;
    private boolean isDeviceOpen;
    //第几轮 仅用于个人
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
    public static final int ITEM_ONE = 1;
    public static final int ITEM_MORE = 2;
    private int itemType = ITEM_MORE;

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
    @Override
    public int getItemType() {
        return itemType;
    }
}
