package com.feipulai.exam.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.feipulai.common.jump_rope.task.PreciseCountDownTimer;
import com.feipulai.exam.activity.person.BaseDeviceState;
import com.feipulai.exam.activity.person.BaseStuPair;
import com.feipulai.exam.entity.RoundResult;

/**
 * Created by pengjf on 2019/7/31.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class DeviceDetail implements MultiItemEntity {
    private int time;
    private int examType;
    private boolean isFinal;
    private boolean isStartCount;
    private boolean isStopCount;
    private PreciseCountDownTimer count;

    public DeviceDetail() {
        baseStuPair = new BaseStuPair();
        baseStuPair.setCanTest(true);
        baseStuPair.setBaseDevice(new BaseDeviceState(BaseDeviceState.STATE_ERROR));
    }

    private BaseStuPair baseStuPair;
    private RoundResult roundResult;
    private boolean isDeviceOpen;
    private int round;
    private boolean isConfirmVisible;
    private boolean isPunish;

    public boolean isStartCount() {
        return isStartCount;
    }

    public void setStartCount(boolean startCount) {
        isStartCount = startCount;
    }

    public boolean isStopCount() {
        return isStopCount;
    }

    public void setStopCount(boolean stopCount) {
        isStopCount = stopCount;
    }

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

    public void setTestTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public int getPreTime() {
        return 5;
    }


    public boolean isConfirmVisible() {
        return isConfirmVisible;
    }

    public void setConfirmVisible(boolean confirmVisible) {
        isConfirmVisible = confirmVisible;
    }

    public void setExamType(int examType) {
        this.examType = examType;
    }

    public int getExamType() {
        return examType;
    }

    public boolean isPunish() {
        return isPunish;
    }

    public void setPunish(boolean punish) {
        isPunish = punish;
    }

    public void setFinsh(boolean isFinal) {
        this.isFinal = isFinal;
    }

    public boolean isFinal() {
        return isFinal;
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

    public PreciseCountDownTimer getCount() {
        return count;
    }

    public void setCount(PreciseCountDownTimer count) {
        this.count = count;
    }
}
