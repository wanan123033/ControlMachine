package com.feipulai.device.serial.command;

/**
 * Created by James on 2017/12/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class RadioChannelCommand extends Command{
	
	//【0:1】包头，2字节,即0XA5,0X5A
	//【2】命令字=0XB4，1字节
	//【3】数据长度=N=1，1字节
	//【4】频道号:0<N<141
	//【4+N】校验和，1字节，=sum{2:3+N}取余
	//【5+N:6+N】包尾2字节0xaa,0x55
	byte[] cmd = {(byte)0xa5,0x5a,(byte)0xb4,0x01,0x00,(byte)0xff,(byte)0xaa,0x55};
	
	/**
	 *
	 * @param channel 频道
	 */
	public RadioChannelCommand(int channel){
		cmd[4] = (byte)(channel & 0xff);
	}
	
	@Override
	public byte[] getCommand(){
		return cmd;
	}
	
	@Override
	ConvertCommand.CmdTarget getTarget(){
		return ConvertCommand.CmdTarget.CONVERTER;
	}
}
