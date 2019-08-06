package com.feipulai.device.serial.beans;

import java.util.ArrayList;
import java.util.Arrays;
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
    private int voltameter;

    public VolleyBallCheck(byte[] data) {
        type = data[12];
        if (type != 0) {
            deviceId = data[4];
            mode = data[6];
            List<Integer> dList = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                if (i >= 8 && i <= 11) {
                    String binaryData = Integer.toBinaryString(data[i] & 0xFF);
                    for (int j = 0; j < 8; j++) {
                        if (binaryData.length() - 1 < j) {
                            dList.add(0);
                        } else {
                            dList.add(Integer.valueOf(binaryData.substring(j, j + 1)));
                        }
                    }
                }
            }

            Integer positionArray[] = new Integer[30];
            System.arraycopy(dList.toArray(), 0, positionArray, 0, positionArray.length);
            positionList = new ArrayList<>(Arrays.asList(positionArray));
            voltameter = dList.get(31);
            if (type == 1) {
                voltameter = 1;
            }
        }


    }

    public int getVoltameter() {
        return voltameter;
    }

    public void setVoltameter(int voltameter) {
        this.voltameter = voltameter;
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
