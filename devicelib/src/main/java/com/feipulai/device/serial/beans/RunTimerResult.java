package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/4/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RunTimerResult {


	/**
	 * （2.7）控制盒－>PC，拦截时间(0XC7)
	 [0]PK_HEAD_INCEPT	：包头				 	0xBB
	 [1]PK_LEN			:  包长					0x10
	 [2]SYS_NAME		：系统名：折返跑计时	0XA0
	 [3]SYS_ID			：系统ID				0x00
	 [4]DES_DEVICE		：目的设备:控制盒		0xA1
	 [5]DEVICE_ID		：目的设备ID			0x00
	 [6]DEVICE_CMD		：获取时间          	0xC7
	 [7]INCEPT_TYPE		：折返/起点             	0x01-0x02
	 [8]TRACK_NUM		：道次                  0x01-0x08
	 [9]ORDER			：第几次                0x00-0xFF
	 [10]TIME1			：参数1         		0x00-0xFF
	 [11]TIME2			：参数2                 0x00-0xFF
	 [12]TIME3			：参数3                 0x00-0xFF
	 [13]TIME4			：参数4                 0x00-0xFF
	 [14]SUM				：校验累加和=[0]+….+[13]. 0x00-0xFF
	 [15]PK_END			：包尾					 0x0D


	 [7]INCEPT_TYPE		：折返/起点    2表示折返点，1表示起点

	 拦截时间（单位MS）由一个UINT32位数据表示
	 [10]TIME1	为第24-31位
	 [11]TIME2	为第16-23位
	 [12]TIME3	为第8-15位
	 [13]TIME4	为第0-7位

	 */
	private int result;
	private int trackNum ; //跑道

	public int getInterceptType() {
		return interceptType;
	}

	public void setInterceptType(int interceptType) {
		this.interceptType = interceptType;
	}

	/**
	 * 第几次
	 * @return
	 */
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	private int interceptType ;//拦截折返/起点
	private int order ;//第几次
	public int getResult(){
		return result;
	}

	public void setResult(int result){
		this.result = result;
	}


	public RunTimerResult(byte data[]) {
		result = ((data[10] & 0xff) << 24) | ((data[11] & 0xff) << 16) |((data[12] & 0xff) << 8)|(data[13] & 0xff);
		trackNum = data[8];
		order = data[9];
		LogUtils.normal("折返跑返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());

	}

	@Override
	public String toString() {
		return "RunTimerResult{" +
				"result=" + result +
				", trackNum=" + trackNum +
				", interceptType=" + interceptType +
				", order=" + order +
				'}';
	}

	/**
	 * 跑道
	 * @return
	 */
	public int getTrackNum() {
		return trackNum;
	}

	public void setTrackNum(int trackNum) {
		this.trackNum = trackNum;
	}
}
