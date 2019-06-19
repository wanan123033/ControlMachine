package com.feipulai.exam.activity.person;

import java.io.Serializable;

/**
 * Created by zzs on 2018/7/30
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class BaseDeviceState implements Serializable {

    // 设备状态(空闲,断开,低电量,冲突,暂停使用),这里的状态只
    // 与recyclerview的显示有关,不是设备发送回来的状态在设置adapter之前要将设备用于显示的状态确定
    public static final int STATE_FREE = 1;
    public static final int STATE_DISCONNECT = -1;
    public static final int STATE_LOW_BATTERY = 2;
    public static final int STATE_CONFLICT = 3;
    public static final int STATE_STOP_USE = 4;
    public static final int STATE_COUNTING = 9;
    public static final int STATE_FINISHED = 10;
    //一对一 设备状态（ 空闲、未开始 、正在使用中、结束、断开，或故障） 这里的状态只
    // 与recyclerview的显示有关,不是设备发送回来的状态在设置adapter之前要将设备用于显示的状态确定
    /**
     * 正在使用中
     */
    public static final int STATE_ONUSE = 5;
    /**
     * 结束
     */
    public static final int STATE_END = 6;
    /**
     * 未开始
     */
    public static final int STATE_NOT_BEGAIN = 7;
    /**
     * 断开，或故障
     */
    public static final int STATE_ERROR = 8;
    private static final long serialVersionUID = 5136286117736499597L;

    /**
     * 设备状态,为{@link #STATE_FREE}或{@link #STATE_DISCONNECT}{@link #STATE_LOW_BATTERY}{@link #STATE_CONFLICT}{@link #STATE_STOP_USE}
     * {@link #STATE_COUNTING} {@link #STATE_FINISHED}
     */
    private int state;
    //设备ID
    private int deviceId;
    //电量
    //private int batteryLeft;


    public BaseDeviceState() {
    }

    public BaseDeviceState(int state) {
        this.state = state;
    }

    public BaseDeviceState(int state, int deviceId) {
        this.state = state;
        this.deviceId = deviceId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "BaseDeviceState{" +
                "state=" + state +
                ", deviceId=" + deviceId +
                '}';
    }
}
