package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallResult implements IDeviceResult{

	private int result;
	private boolean isFoul;

	public VolleyBallResult() {
	}

	public VolleyBallResult(byte[] data){
		isFoul = (((data[11] & 0xff) == 0xff) && ((data[12] & 0xff) == 0xff) && ((data[13] & 0xff) == 0xff)) || ((data[8] & 0x80) == 0x80);
		result = ((data[9] & 0xff) << 8) + (data[10] & 0xff);
		LogUtils.normal("排球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}

	public int getResult(){
		return result;
	}
	
	public void setResult(int result){
		this.result = result;
	}

	@Deprecated
	public boolean isFoul(){
		return isFoul;
	}

	@Deprecated
	public void setFoul(boolean foul){
		isFoul = foul;
	}

	@Override
	public String toString() {
		return "VolleyBallResult{" +
				"result=" + result +
				", isFoul=" + isFoul +
				'}';
	}

}
