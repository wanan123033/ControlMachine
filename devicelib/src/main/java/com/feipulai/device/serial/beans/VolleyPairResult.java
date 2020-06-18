package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by pengjf on 2019/9/29.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class VolleyPairResult {
    private byte[] data;
    private int score;
    private int id;
    private int frequency;
    private int state ;//等待触摸：0X00  已触摸显示成绩：0X01
    private int hostId;
    public VolleyPairResult(byte[] data){
        if (data.length == 16){
            score = ((data[8] & 0xff) << 8) + (data[9] & 0xff);
        }else if (data.length == 18){
            score = ((data[9] & 0xff) << 8) + (data[10] & 0xff);
            state = (data[8] & 0xff);
        }
        this.data = data;
        id = data[6]&0xff;
        frequency = data[12]&0xff;
        hostId=data[5]&0xff;
//		Log.i("sargent",StringUtility.bytesToHexString(data));
        LogUtils.normal("排球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

    }

    public int getScore(){
        return score;
    }

    public void setScore(int score){
        this.score = score;
    }

    public int getDeviceId(){
        return id;
    }

    public int getFrequency(){
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public byte[] getDataArr() {
        return data;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    @Override
    public String toString() {
        return "VolleyPairResult{" +
                "data=" + Arrays.toString(data) +
                ", score=" + score +
                ", id=" + id +
                ", frequency=" + frequency +
                ", state=" + state +
                ", hostId=" + hostId +
                '}';
    }
}
