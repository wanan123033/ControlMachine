package com.feipulai.device.serial.beans;

import android.util.Log;

/**
 * Created by James on 2018/5/7 0007.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SargentJumpResult{
	
	private int score;
	private int id;
	private int frequency;
	public SargentJumpResult(byte[] data){
		score = ((data[8] & 0xff) << 8) + (data[9] & 0xff);
		id = data[4]&0xff;
		frequency = data[8]&0xff;
		Log.i("sargent",StringUtility.bytesToHexString(data));
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
}
