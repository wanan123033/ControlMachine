package com.feipulai.device.serial.beans;

import com.feipulai.device.SysConvertUtil;
import com.orhanobut.logger.utils.LogUtils;

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
    private int voltameter;//即1表示电量正常，0表示电量低。

    private int poleNum;//1米杆为2，2米杆为4，3米杆为6。
    private int deviceType;//对空垫球为0；对墙垫球为1
    private int checkType;

    public VolleyBallCheck(byte[] data) {
        checkType = data[7] & 0xff;
        switch (data[7] & 0xff) {
            case 7:
                deviceId = data[4];
                mode = data[6];
                List<Integer> dList = new ArrayList<>();
                for (int i = 8; i < 12; i++) {
                    String binaryData = SysConvertUtil.convert16To2(data[i]);
                    for (int j = 0; j < binaryData.length(); j++) {
                        dList.add(Integer.valueOf(binaryData.substring(j, j + 1)));
                    }
                }
                Integer positionArray[] = new Integer[30];
                System.arraycopy(dList.toArray(), 0, positionArray, 0, positionArray.length);
                positionList = new ArrayList<>(Arrays.asList(positionArray));
                voltameter = dList.get(31);
                if (type == 1) {
                    voltameter = 1;
                }
                break;
            case 8:
                //0x54  0x55  0x00  0x10  devId  0x0a  mode  0x08 T1 P1 P2 P3 d1  d2  d3  d4  d5 sum  0x27  0x0d
                //P1: 对空垫球为0；对墙垫球为1
                //P2：1米杆为2，2米杆为4，3米杆为6。
                //P3:表示电量信息，0x80:电量低；0x81：电量正常；0~100：电量百分比（目前不支持）
                //d1--d5表示低杆的40对对管情况，每对管占一位，0正常，1异常
                deviceId = data[4];
                mode = data[6];
                deviceType = data[9];
                poleNum = data[10];
                if ((data[11] & 0xff) == 0x80) {
                    voltameter = 0;
                } else {
                    voltameter = 1;
                }
                dList = new ArrayList<>();
                for (int i = 12; i < 17; i++) {
                    String binaryData = SysConvertUtil.convert16To2(data[i]);
                    for (int j = 0; j < binaryData.length(); j++) {
                        dList.add(Integer.valueOf(binaryData.substring(j, j + 1)));
                    }
                }
                positionArray = new Integer[40];
                System.arraycopy(dList.toArray(), 0, positionArray, 0, positionArray.length);
                positionList = new ArrayList<>(Arrays.asList(positionArray));

                break;

        }
        LogUtils.normal("排球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());


    }

    public VolleyBallCheck() {
    }

    public int getPoleNum() {
        return poleNum;
    }

    public void setPoleNum(int poleNum) {
        this.poleNum = poleNum;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getCheckType() {
        return checkType;
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
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
