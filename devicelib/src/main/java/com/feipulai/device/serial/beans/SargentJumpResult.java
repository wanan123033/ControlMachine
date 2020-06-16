package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by James on 2018/5/7 0007.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SargentJumpResult{
	
	private int score;
	private int id;
	private int frequency;
	private int state ;//等待触摸：0X00  已触摸显示成绩：0X01
    private int hostId;
    private byte incorrectPoles[];//检测杆
	public SargentJumpResult(byte[] data){
	    if (data[7] == 0x0A){
            score = ((data[8] & 0xff) << 8) + (data[9] & 0xff);
        }
        else if (data[6] == 0x01 && data.length == 18 && data[7] == 2){
            score = ((data[9] & 0xff) << 8) + (data[10] & 0xff);
            state = (data[8] & 0xff);
        }else if (data[7] == 0x04 && data.length!= 24){
            score = ((data[8] & 0xff) << 8) + (data[9] & 0xff);
        }else if (data.length == 24){
	        //13*8= 104
            incorrectPoles = new byte[104];
	        for (int i = 8;i<=20;i++){
                byte[] bytes = getBooleanArray(data[i]);
                System.arraycopy(bytes,0,incorrectPoles,(i-8)*8,8);
            }
        }

		id = data[4]&0xff;
		frequency = data[8]&0xff;
		if (data[7] == 0x01){
		    hostId = data[11];
        }
//		Log.i("sargent",StringUtility.bytesToHexString(data));
        LogUtils.normal("跳远返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

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

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    /**
     * 将byte转换为一个长度为8的byte数组，数组每个值代表bit
     */
    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    public byte[] getIncorrectPoles() {
        return incorrectPoles;
    }

    @Override
    public String toString() {
        return "SargentJumpResult{" +
                "score=" + score +
                ", id=" + id +
                ", frequency=" + frequency +
                ", state=" + state +
                ", hostId=" + hostId +
                ", incorrectPoles=" + Arrays.toString(incorrectPoles) +
                '}';
    }
}
