package com.feipulai.device.serial.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by James on 2018/5/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallCheck {

    //d1--4表示低杆的30对对管情况 8-11
    private List<Integer> positionList;
    private int deviceId;
    //单机模式为0；可以扩展 一对多同步模式为1，一对多异步模式为2
    private int mode;
    private int type;

    public VolleyBallCheck(byte[] data) {
        positionList = new ArrayList<>(32);
        type = data[12];
        deviceId = data[4];
        mode = data[6];
        for (int i = 0; i < data.length; i++) {
            if (i >= 8 && i <= 11) {
                char[] position = Integer.toBinaryString(data[i]).toCharArray();
                for (char c : position) {
                    positionList.add(Integer.valueOf(c));
                }
            }
        }

    }

    public List<Integer> getPositionList() {
        return positionList;
    }

    public void setPositionList(List<Integer> positionList) {
        this.positionList = positionList;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "VolleyBallCheck{" +
                "positionList=" + positionList +
                ", deviceId=" + deviceId +
                ", mode=" + mode +
                ", type=" + type +
                '}';
    }
}
