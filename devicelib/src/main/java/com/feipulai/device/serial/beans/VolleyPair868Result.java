package com.feipulai.device.serial.beans;

import android.util.Log;

import com.feipulai.device.SysConvertUtil;
import com.orhanobut.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VolleyPair868Result {
    private int poleNum;
    private int deviceType;
    private byte[] dataArr;
    private List<Integer> positionList;
    private int frequency;
    private int state;
    private int score;
    private int deviceId;
    private int electricityState;

    private int hostid;

    private byte[] checkResult;

    public static final int STATE_FREE = 0;       //空闲
    public static final int STATE_TIME_PREPARE = 1;  //计时准备
    public static final int STATE_TIMING = 2;      //计时中
    public static final int STATE_TIME_END = 3;    //计时结束
    public static final int STATE_COUNT_PREPARE = 0x11;  //计数准备
    public static final int STATE_COUNTING = 0x12;   //计数中
    public static final int STATE_COUNT_END = 0x13;  //计数结束

    public static final int ELECTRICITY_STATE_NOMAL = 0x81;  //电量充足
    public static final int ELECTRICITY_STATE_INADEQUATE = 0x80;  //电量不足


    public VolleyPair868Result(byte[] data) {
        this.dataArr = data;
        Log.e("TAG", "----" + StringUtility.bytesToHexString(data));
        state = data[12];
        score = (data[13] & 0xFF) * 0x0100 + (data[14] & 0xFF);
        hostid = data[5];
        deviceId = data[6];
        electricityState = data[15];
        frequency = (data[12] & 0xff);
        if (data.length > 18) {       //自检结果大于18位
            checkResult = new byte[]{data[14], data[15], data[16], data[17], data[18]};
            List<Integer> dList = new ArrayList<>();
//            byte[] checkResult = new byte[]{data[14], data[15], data[16], data[17], data[18]};
//            for (int i = 0; i < checkResult.length; i++) {
//                String binaryData = SysConvertUtil.convert16To2(checkResult[i]);
//                for (int j = 0; j < binaryData.length(); j++) {
//                    dList.add(Integer.valueOf(binaryData.substring(j, j + 1)));
//                }
//            }
//            Integer positionArray[] = new Integer[50];
//            System.arraycopy(dList.toArray(), 0, positionArray, 0, positionArray.length);
//            positionList = new ArrayList<>(Arrays.asList(positionArray));


            for (int i = 14; i < 19; i++) {
                String binaryData = SysConvertUtil.convert16To2(data[i]);
                for (int j = 0; j < binaryData.length(); j++) {
                    dList.add(Integer.valueOf(binaryData.substring(j, j + 1)));
                }
            }
            Integer positionArray[] = new Integer[40];
            System.arraycopy(dList.toArray(), 0, positionArray, 0, positionArray.length);
            positionList = new ArrayList<>(Arrays.asList(positionArray));


            deviceType = data[12];
            poleNum = data[13];
        }
        LogUtils.normal("排球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());


    }
    public int getState() {
        return state;
    }

    public int getScore() {
        return score;
    }

    public int getElectricityState() {
        return electricityState;
    }

    public int getHostid() {
        return hostid;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public int getFrequency() {
        return frequency;
    }

    public List<Integer> getPositionList() {
        return positionList;
    }

    public byte[] getDataArr() {
        return dataArr;
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

    public byte[] getCheckResult() {
        return checkResult;
    }

    @Override
    public String toString() {
        return "VolleyPair868Result{" +
                "poleNum=" + poleNum +
                ", deviceType=" + deviceType +
                ", dataArr=" + Arrays.toString(dataArr) +
                ", positionList=" + positionList +
                ", frequency=" + frequency +
                ", state=" + state +
                ", score=" + score +
                ", deviceId=" + deviceId +
                ", electricityState=" + electricityState +
                ", hostid=" + hostid +
                ", checkResult=" + Arrays.toString(checkResult) +
                '}';
    }
}
