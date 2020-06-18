package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

import java.util.Arrays;

/**
 * Created by James on 2017/10/30.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class JumpSelfCheckResult{
	
	//控制主机下发：：0x54 0x44(2字节包头，下同) 0x00 0x0c(2字节包长，下同) 0x00 0x02 0x00 0x07(命令字节) 0x00 0x00(2字节参数) 0x27 0x0d(2字节包尾，下同)
	//测试终端回复：0x54 0x55 0x00 0x38（包长） 0x01 0x02 0x00 0x07（自测命令）0x00（参数）
	//54 55 00 38(包长) 01 02 00 07(命令字节) 00(参数)
	//CPU每片占用5个字节
	//(第1字节高4位显示CPU号,低4位表示第34-33位LED状态,1表示遮挡,0表示没有遮挡)
	//第2字节表示第32-24位LED状态
	//第3字节表示第23-16位LED状态
	//第4字节表示第15-9位LED状态
	//第5字节表示第8-1位LED状态
	//10 00 00 00 00(1号CPU)
	//20 00 00 00 00(2号CPU)
	//30 00 00 00 00(3号CPU)
	//40 00 00 00 00(4号CPU)
	//50 00 00 00 00(5号CPU)
	//60 00 00 00 00(6号CPU)
	//70 00 00 00 00(7号CPU)
	//80 00 00 00 00(8号CPU)
	//90 00 00 00 00(9号CPU)
	//27 0d(包尾)
	private int brokenLEDs[] = new int[3];//记录被遮挡的LED编码
	private int mTerminalCondition;
	public static final int NORMAL = 0x0;
	public static final int NEED_CHANGE = 0x01;
	public static final int HAS_BROKEN_POINTS = 0x02;
	
	//如果连续两个或者总共3个及以上LED被遮挡,则认为立定跳远垫子有问题
	public JumpSelfCheckResult(byte[] buf){
		int b = 0;//记录被遮挡的LED个数
		int tmp, tmp1;
		//杆号
		for(int i = 0;i < 3;i++){
			//u号
			for(int j = 0;j < 3;j++){
				//u号不对
				if((buf[9 + (i * 3 + j) * 5] >> 4 & 0x0f) != (i * 3 + j + 1)){
					b = 3;
					break;
				}
				//每个测量杆的第0个CPU控制34颗LED,每个CPU状态的第1个字节最后2位分别表示第100、99个LED状态
					if(j == 0){
					//第99颗LED被遮挡
					if((buf[9 + (i * 3 + j) * 5] & 0x01) != 0){
						//brokenLEDs[b++] = 99;
						brokenLEDs[b++] = 100 * i + 99;
						if(b >= 3){
							break;
						}
					}
					//第100颗LED被遮挡
					if((buf[9 + (i * 3 + j) * 5] & 0x02) != 0){
						brokenLEDs[b++] = 100 * i + 100;
						if(b >= 3){
							break;
						}
					}
				}else if((buf[9 + (i * 3 + j) * 5] & 0x01) != 0){
					//每个测量杆的第1、2号CPU的第1个字节最后一位表示第33个LED状态
					//brokenLEDs[b++] = 33 * (j + 1);
					brokenLEDs[b++] = 100 * i + 33 * (j + 1);
					if(b >= 3){
						break;
					}
				}
				
				//每个CPU状态字节的后四个字节表示32-1号LED状态
				tmp = (buf[10 + (i * 3 + j) * 5] << 24) | (buf[11 + (i * 3 + j) * 5] << 16) |
						(buf[12 + (i * 3 + j) * 5] << 8) | buf[13 + (i * 3 + j) * 5];
				tmp1 = 1;
				for(int k = 0;k < 32;k++){
					if((tmp & tmp1) != 0){
						brokenLEDs[b++] = 100 * i + 33 * j + k + 1;
						//brokenLEDs[b++] = 33 * j + k + 1;
						//如果总共有3个LED被遮挡,即认为测量垫已坏
						if(b >= 3){
							break;
						}
					}
					tmp1 <<= 1;
				}
				if(b >= 3){
					break;
				}
			}
			if(b >= 3){
				break;
			}
		}
		//认为测量垫已坏的条件：
		//1. 总共被遮挡的LED数>=3个
		//2. 或者相邻的两颗LED显示被遮挡
		if((b >= 3) || (Math.abs(brokenLEDs[0] - brokenLEDs[1]) == 1 && brokenLEDs[1] != 0)){
			//需要更换
			mTerminalCondition = NEED_CHANGE;
		}else if(b == 0){
			mTerminalCondition = NORMAL;
		}else{
			//有坏点
			mTerminalCondition = HAS_BROKEN_POINTS;
		}
		LogUtils.normal("立定跳远自检返回数据(解析前):"+buf.length+"---"+StringUtility.bytesToHexString(buf)+"---\n(解析后):"+toString());
	}
	
	/**
	 * 获取被遮挡的LED编号
	 */
	public int[] getBrokenLEDs(){
		return brokenLEDs;
	}
	
	/**
	 * 返回终端检测结果的状态
	 *
	 * @return
	 * @code NORMAL
	 * @code NEED_CHANGE
	 * @code HAS_BROKEN_POINTS
	 */
	public int getTerminalCondition(){
		return mTerminalCondition;
	}

	@Override
	public String toString() {
		return "JumpSelfCheckResult{" +
				"brokenLEDs=" + Arrays.toString(brokenLEDs) +
				", mTerminalCondition=" + mTerminalCondition +
				'}';
	}
}