package com.feipulai.device.manager;

import com.feipulai.device.serial.SerialDeviceManager;
import com.feipulai.device.serial.command.ConvertCommand;

/**
 * Created by James on 2018/3/8 0008.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallManager {

	public static final int VOLLEY_BALL_DISCONNECT = -1;
	public static final int VOLLEY_BALL_CONNECT  = 0;

	private static final byte[] EMPTY = {0X54, 0X44, 00, 0X10, 00, 0xa, 00, 00, 0, 00, 00, 00, 00, 0x1a, 0x27, 0x0d};
	private static final byte[] CMD_START = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x01, 00, 00, 00, 00, 00, 0x1b, 0x27, 0x0d};
	private static final byte[] CMD_END = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x02, 00, 00, 00, 0, 00, 0x1c, 0x27, 0x0d};
	private static final byte[] CMD_GET_SCORE = {0X54, 0X44, 00, 0X10, 00, 0Xa, 00, 0x04, 00, 00, 00, 00, 00, 0x1e, 0x27, 0x0d};

	// 主机下发，测量杆收到会原样回复(0x44变化为0x55)。主机每5秒发送一次，用于检查测量杆连接是否正常。
	public void emptyCommand(){
		SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,EMPTY));
	}

	public void startTest(){
		SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,CMD_START));
	}

	public void stopTest(){
		SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,CMD_END));
	}

	public void getScore(){
		SerialDeviceManager.getInstance().sendCommand(new ConvertCommand(ConvertCommand.CmdTarget.RS232,CMD_GET_SCORE));
	}

}
