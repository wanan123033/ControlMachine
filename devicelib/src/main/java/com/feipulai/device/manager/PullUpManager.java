package com.feipulai.device.manager;

import android.util.Log;

import com.feipulai.device.serial.RadioManager;
import com.feipulai.device.serial.beans.StringUtility;
import com.feipulai.device.serial.command.ConvertCommand;
import com.feipulai.device.serial.command.RadioChannelCommand;
import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/5/14 0014.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PullUpManager{
	
	private static final String TAG = PullUpManager.class.getSimpleName();
	//工作状态（空闲=1 准备=2 计数=3 结束=4其他=未知）。这里定义-1为断开
	public static final int STATE_DISCONNECT = -1;
	public static final int STATE_FREE = 1;
	public static final int STATE_READY = 2;
	public static final int STATE_COUNTING = 3;
	public static final int STATE_ENDED = 4;
	
	public static final int DEFAULT_COUNT_DOWN_TIME = 5;
	
	/**
	 * 设置终端频率
	 *
	 * @param originFrequency 原来的频段(终端目前工作所在频段)
	 * @param deviceId        设备ID
	 * @param channel         频道号
	 *                        <p>
	 *                        该函数中延时为调优过得,请不要随意更改
	 */
	public void setFrequency(int originFrequency,int deviceId,int channel){
		byte[] buf = new byte[16];
		buf[0] = 0x54;
		buf[1] = 0x44;    //包头
		buf[2] = 0;       //包长
		buf[3] = 0x10;
		buf[4] = (byte)(deviceId & 0xff);      //设备号
		buf[5] = 0x0b;     //测试项目
		buf[6] = 0;       //单机模式
		buf[7] = 0x0b;      //命令
		buf[8] = (byte)(channel & 0xff); //高字节在先
		buf[9] = 4;
		buf[10] = 0;
		buf[11] = 0;
		buf[12] = 0;
		for(int i = 2;i < 13;i++){
			buf[13] += buf[i] & 0xff;
		}
		//buf[13] = (byte)(buf[2] + buf[3] + buf[4] + buf[5] + buf[6] + buf[7] + buf[8] + buf[9] + buf[10] + buf[11] + buf[12]);
		buf[14] = 0x27;
		buf[15] = 0x0d;   //包尾
		//Logger.i(StringUtility.bytesToHexString(buf));
		//先切到通信频段
		Log.i("james","originFrequency:" + originFrequency);
		RadioChannelCommand command = new RadioChannelCommand(originFrequency);
		LogUtils.normal(command.getCommand().length+"---"+StringUtility.bytesToHexString(command.getCommand())+"---引体向上切频指令");
		RadioManager.getInstance().sendCommand(new ConvertCommand(command));
		Log.i("james",StringUtility.bytesToHexString(buf));
		try{
			Thread.sleep(500);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		LogUtils.normal(buf.length+"---"+StringUtility.bytesToHexString(buf)+"---引体向上设置频率指令");
		RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,buf));
		command = new RadioChannelCommand(channel);
		LogUtils.normal(command.getCommand().length+"---"+StringUtility.bytesToHexString(command.getCommand())+"---引体向上切频指令");
		RadioManager.getInstance().sendCommand(new ConvertCommand(command));
	}
	
	/**
	 * 开始测试
	 *
	 * @param deviceId  设备id
	 * @param countTime 倒计时时间
	 * @param testTime  测试时间
	 * @param interval  间隔时间  相邻两次的最长间隔时间,为0表示不设间隔时间
	 */
	public void startTest(int deviceId,int countTime,int testTime,int interval){
		//[00] [01]：包头高字节0x54  低字节0x44
		//[02] [03]：长度高字节0x00  低字节0x10
		//[04]：子机id
		//[05]：0x0b   项目编号
		//[06]：0   –单机模式
		//[07]：1   --开始命令
		//[08]：倒计时时间，单位为秒
		//[09]：测试时间，单位为秒
		//[10]：间隔时间(单位为秒)   =0，为不设间隔时间
		//[11] [12]：0  0
		//[13]：累加和（从02到12共11个字节算术和的低字节）
		//[14] [15]：包尾高字节0x27   低字节0x0d
		byte[] buf = {0x54,0x44,0x00,0x10,00,0x0b,00,1,00,00,00,00,00,0x00,0x27,0x0d};
		buf[4] = (byte)(deviceId & 0xff);
		buf[8] = (byte)(countTime & 0xff);
		buf[9] = (byte)(testTime & 0xff);
		buf[10] = (byte)(interval & 0xff);
		for(int i = 2;i < 13;i++){
			buf[13] += buf[i] & 0xff;
		}
		buf[13] = (byte)(buf[13] & 0xff);
		//Log.i(TAG,StringUtility.bytesToHexString(buf));
		LogUtils.normal(buf.length+"---"+StringUtility.bytesToHexString(buf)+"---引体向上开始指令");
		RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,buf));
	}
	
	/**
	 * 查询终端工作状态,获取计数值。
	 *
	 * @param deviceId 设备号
	 */
	public void getState(int deviceId){
		//[00] [01]：包头高字节0x54   低字节0x44
		//[02] [03]：长度高字节0x00   低字节0x10
		//[04]：子机id
		//[05]：0x0b  项目编号
		//[06]：0   --单机模式
		//[07]：4   --查询
		//[08] --[12]：0  0  0  0  0
		//[13]：累加和（从02到12共11个字节算术和的低字节）
		//[14] [15]：包尾高字节0x27   低字节0x0d
		byte[] buf = {0x54,0x44,0x00,0x10,00,0x0B,00,4,00,00,00,00,00,0x00,0x27,0x0d};
		buf[4] = (byte)(deviceId & 0xff);
		for(int i = 2;i < 13;i++){
			buf[13] += buf[i] & 0xff;
		}
		buf[13] = (byte)(buf[13] & 0xff);
		//Logger.i(StringUtility.bytesToHexString(buf));
		LogUtils.normal(buf.length+"---"+StringUtility.bytesToHexString(buf)+"---引体向上获取状态指令");
		RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,buf));
	}
	
	/**
	 * 结束测试
	 *
	 * @param deviceId 设备id
	 */
	public void endTest(int deviceId){
		//[00] [01]：包头高字节0x54  低字节0x44
		//[02] [03]：长度高字节0x00  低字节0x10
		//[04]：子机id
		//[05]：0x0b 项目编号
		//[06]：0   –单机模式
		//[07]：2-- 结束命令
		//[08]-- - [12]：0 0 0 0 0
		//[13]：累加和（从02到12共11个字节算术和的低字节）
		//[14] [15]：包尾高字节0x27   低字节0x0d
		byte[] buf = {0x54,0x44,0x00,0x10,00,0x0B,00,2,00,00,00,00,00,0x00,0x27,0x0d};
		buf[4] = (byte)(deviceId & 0xff);
		for(int i = 2;i < 13;i++){
			buf[13] += buf[i] & 0xff;
		}
		buf[13] = (byte)(buf[13] & 0xff);
		//Log.i(TAG,StringUtility.bytesToHexString(buf));
		LogUtils.normal(buf.length+"---"+StringUtility.bytesToHexString(buf)+"---引体向上结束测试指令");
		RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,buf));
	}
	
	public void getVersion(int deviceId){
		//[00] [01]：包头高字节0x54   低字节0x44
		//[02] [03]：长度高字节0x00   低字节0x10
		//[04]：子机id
		//[05]：0x0b   项目编号
		//[06]：0   –单机模式
		//[07]：12   --查询版本
		//[08] --[12]：0  0  0  0  0
		//[13]：累加和（从02到12共11个字节算术和的低字节）
		//[14] [15]：包尾高字节0x27   低字节0x0d
		byte[] buf = {0x54,0x44,0x00,0x10,00,0x0B,00,0x0c,00,00,00,00,00,0x00,0x27,0x0d};
		buf[4] = (byte)(deviceId & 0xff);
		for(int i = 2;i < 13;i++){
			buf[13] += buf[i] & 0xff;
		}
		buf[13] = (byte)(buf[13] & 0xff);
		Log.i("",StringUtility.bytesToHexString(buf));
		//Log.i(TAG,StringUtility.bytesToHexString(buf));
		LogUtils.normal(buf.length+"---"+StringUtility.bytesToHexString(buf)+"---引体向上获取版本指令");
		RadioManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RADIO_868,buf));
	}
	
}
