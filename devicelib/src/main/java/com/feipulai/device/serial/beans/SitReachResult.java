package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/3 0003.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class SitReachResult{
	
	//data[10]：测量仪当前状态：0—无效、1—就绪、2—回拉、3—测量、4—结束
	//data[11]：电压状态：0—无效、1—欠压、2—正常、＞10—电量百分比
	
	//成绩字节定义:
	//data[8]   data[9]
	//D7	D6	D5—D0	D7—D0   D13—D0
	//foul	nega
	//foul----犯规标志，1-犯规  0-不犯规
	//nega----负值标志，1-负值  0-正值
	//d13--d0   成绩 数值
	public static final int INVALID = 0x0;
	public static final int READY = 0x1;
	public static final int PULL_BACK = 0x2;
	public static final int TESTING = 0x3;
	public static final int TEST_ENDED = 0x4;
	
	private int score;
	private int state;
	private boolean isFoul;
	
	public SitReachResult(byte[] data){
		switch(data[10]){
			
			case 0x0:
				setState(INVALID);
				break;
			
			case 0x01:
				setState(READY);
				break;
			
			case 0x02:
				setState(PULL_BACK);
				break;
			
			case 0x03:
				setState(TESTING);
				break;
			
			case 0x04:
				setState(TEST_ENDED);
				break;
		}
		
		isFoul = (data[8] & 0x80) == 0x80;
		boolean isNegative = (data[8] & 0x40) == 0x40;
		score = (data[8] & 0x3f) * 256 + (data[9] & 0xff);
		if(isNegative){
			score = -score;
		}
		LogUtils.normal("坐位体前屈返回设备结果数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());
	}
	
	
	public int getScore(){
		return score;
	}
	
	public void setScore(int score){
		this.score = score;
	}
	
	public int getState(){
		return state;
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	public boolean isFoul(){
		return isFoul;
	}
	
	public void setFoul(boolean foul){
		isFoul = foul;
	}

	@Override
	public String toString() {
		return "SitReachResult{" +
				"score=" + score +
				", state=" + state +
				", isFoul=" + isFoul +
				'}';
	}
}
