package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by James on 2018/5/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class MedicineBallSelfCheckResult{
	
	private int numOfPoles;
	private boolean isInCorrect;
	private int[] incorrectPoles;
	
	//终端上发自检(校准)结果格式：
	//杆1-8  杆9-16   杆17-20  总杆数(发射+接收)  0   0
	public MedicineBallSelfCheckResult(byte[] data){
		numOfPoles = data[11] & 0xff;
		int tmp = ((data[8] & 0xff) << 16) + ((data[9] & 0xff) << 8) + (data[10] & 0xff);
		incorrectPoles = new int[numOfPoles];
		int bit = 0x80_00_00;
		int j = 0;
		for(int i = 1;i < numOfPoles + 1;i++){
			if((tmp & bit) != 0){
				isInCorrect = true;
				incorrectPoles[j ++] = i;
			}
			bit >>= 1;
		}
		LogUtils.normal("实心球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}
	
	public int getNumOfPoles(){
		return numOfPoles;
	}
	
	public void setNumOfPoles(int numOfPoles){
		this.numOfPoles = numOfPoles;
	}
	
	public boolean isInCorrect(){
		return isInCorrect;
	}
	
	public void setInCorrect(boolean inCorrect){
		isInCorrect = inCorrect;
	}
	
	public int[] getIncorrectPoles(){
		return incorrectPoles;
	}
	
	public void setIncorrectPoles(int[] incorrectPoles){
		this.incorrectPoles = incorrectPoles;
	}

	@Override
	public String toString() {
		return "MedicineBallSelfCheckResult{" +
				"numOfPoles=" + numOfPoles +
				", isInCorrect=" + isInCorrect +
				", incorrectPoles=" + Arrays.toString(incorrectPoles) +
				'}';
	}
}
