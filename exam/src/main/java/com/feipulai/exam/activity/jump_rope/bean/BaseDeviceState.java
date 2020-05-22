package com.feipulai.exam.activity.jump_rope.bean;

import java.io.Serializable;

public class BaseDeviceState implements Serializable {

    private static final long serialVersionUID = 5136286117736499597L;

    // 设备状态(空闲,断开,低电量,冲突,暂停使用),这里的状态只
    // 与recyclerview的显示有关,不是设备发送回来的状态
    // 在设置adapter之前要将设备用于显示的状态确定
    public static final int STATE_FREE = 1;
    public static final int STATE_DISCONNECT = -1;
    public static final int STATE_LOW_BATTERY = 2;
    public static final int STATE_CONFLICT = 3;
    public static final int STATE_STOP_USE = 4;
    public static final int STATE_COUNTING = 9;
    public static final int STATE_FINISHED = 10;

    /**
     * 设备状态,为{@link #STATE_FREE}或{@link #STATE_DISCONNECT}{@link #STATE_LOW_BATTERY}{@link #STATE_CONFLICT}{@link #STATE_STOP_USE}
     * {@link #STATE_COUNTING} {@link #STATE_FINISHED}
     */
    private int state = STATE_DISCONNECT;
    //设备ID
    private int deviceId;


    private int disconnectCount = 0;

    private String deviceVersion;

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

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

    public int getDisconnectCount() {
        return disconnectCount;
    }

    public void setDisconnectCount(int disconnectCount) {
        this.disconnectCount = disconnectCount;
    }

    @Override
    public String toString() {
        return "BaseDeviceState{" +
                "state=" + state +
                ", deviceId=" + deviceId +
                '}';
    }

}
