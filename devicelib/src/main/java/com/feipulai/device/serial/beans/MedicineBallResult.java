package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * 实心球结果
 */

public class MedicineBallResult{
	
	//0x54  55  00  10  00  07  00  04  00  01  0E  00  00  02  27  0D
	private int result;

	/**是否犯规，协议中并没有关于是否犯规的说明 ，但实际上应该有*/
	private boolean fault ;
    /**扫描到的点数*/
	private int sweepPoint;

	public MedicineBallResult(byte[] data){
		result = ((data[9] & 0xff) << 8) + (data[10] & 0xff);
		checkFault(data);
		sweepPoint = data[13]&0xff;
		LogUtils.normal("实心球返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}
    public int getSweepPoint() {
        return sweepPoint;
    }

	//若byte8的最高位为1或 byte11/byte12这2个字节都为0xff，则表示犯规；
	private void checkFault(byte[] data) {
		if ((data[8] & 0x80) == 0x80) {
			fault = true ;
			return;
		}else for (int i = 11; i < 13; i++) {
			if ((data[i] & 0xff) != 0xff) {
				return;
			}
			fault = true;
		}
	}



	public boolean isFault() {
		return fault;
	}

	public int getResult(){
		return result;
	}
	
	public void setResult(int result){
		this.result = result;
	}

	@Override
	public String toString() {
		return "MedicineBallResult{" +
				"result=" + result +
				", fault=" + fault +
				", sweepPoint=" + sweepPoint +
				'}';
	}
}
