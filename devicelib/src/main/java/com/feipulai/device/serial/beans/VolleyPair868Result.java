package com.feipulai.device.serial.beans;

import android.util.Log;

import com.feipulai.device.SysConvertUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VolleyPair868Result {
    private byte[] dataArr;
    private  List<Integer> positionList;
    private int frequency;
    private int state;
    private int score;
    private int childId;
    private int electricityState;

    private int deviceid;

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
        Log.e("TAG","----"+StringUtility.bytesToHexString(data));
        state = data[12];
        score = data[13] * 0x0100 + data[14];
        deviceid = data[5];
        childId = data[6];
        electricityState = data[15];
        frequency = (data[12]&0xff);
        if (data.length > 18) {       //自检结果大于18位
            List<Integer> dList = new ArrayList<>();
            byte[] checkResult = new byte[]{data[14], data[15], data[16], data[17], data[18]};
            for (int i = 0 ; i < checkResult.length ; i++){
                String binaryData = SysConvertUtil.convert16To2(checkResult[i]);
                for (int j = 0; j < binaryData.length(); j++) {
                    dList.add(Integer.valueOf(binaryData.substring(j, j + 1)));
                }
            }
            Integer positionArray[] = new Integer[50];
            System.arraycopy(dList.toArray(), 0, positionArray, 0, positionArray.length);
            positionList = new ArrayList<>(Arrays.asList(positionArray));
        }

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

    public int getDeviceid() {
        return deviceid;
    }

    public int getChildId() {
        return childId;
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
}