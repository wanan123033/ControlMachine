package com.feipulai.device.serial.command;

/**
 * Created by pengjf on 2018/11/7.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ConvertCommand{
	
	public enum CmdTarget{
		CONVERTER,
		RADIO_868,
		// 实际上,对于232这里完全没有必要,但是之前已经这么写了,为了兼容老代码
		RS232
	}
	
	private static final byte[] PACKET_HEAD = {(byte)0XA5,0X5A};
	private CmdTarget target;
	
	private byte[] cmd;
	private byte checksum;
	private static final byte[] PACKET_END = {(byte)0xaa,0x55};
	
	/**
	 * 在命令中可以自定义的选项,在构造函数中获取
	 *
	 * @param target 命令要发送到的目的地
	 * @param cmd    发送的数据内容,即通过串口发送的具体命令(如立定跳远开始、结束命令)
	 */
	public ConvertCommand(CmdTarget target,byte[] cmd){
		this.target = target;
		this.cmd = cmd;
		checkSum();
	}
	
	public ConvertCommand(Command cmd){
		this.target = cmd.getTarget();
		this.cmd = cmd.getCommand();
		checkSum();
	}
	
	// TODO: 2017/12/1 14:30 目前校验和还没有用,checkSum均为0xff
	private void checkSum(){
		checksum = (byte)0xff;
	}
	
	//【0:1】包头，2字节,即0XA5,0X5A
	//【2】命令字，1字节
	//【3】数据长度=N，1字节，0<=N<256, 【4:3+N】
	//【4:3+N】数据
	//【4+N】校验和，1字节，=sum{2:3+N}取余
	//【5+N:6+N】包尾2字节0xaa,0x55
	public byte[] getCmdBytes(){
		byte[] result = null;
		// 发到转换器的命令,不需要经过转换
		switch(target){
			case CONVERTER:
			case RS232:
				result = cmd;
				break;
			
			case RADIO_868:
				// 发到868的命令需要转换
				result = new byte[cmd.length + 7];
				System.arraycopy(PACKET_HEAD,0,result,0,PACKET_HEAD.length);
				result[2] = (byte)0xc1;
				result[3] = (byte)(cmd.length & 0xff);
				System.arraycopy(cmd,0,result,4,cmd.length);
				result[cmd.length + 4] = checksum;
				System.arraycopy(PACKET_END,0,result,cmd.length + 5,PACKET_END.length);
				break;
				
			default:
				throw new RuntimeException("what the hell is that");
		}
		return result;
	}
	
}
