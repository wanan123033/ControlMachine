package com.feipulai.device.serial.command;

/**
 * Created by James on 2017/12/8.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public abstract class Command{
	
	abstract byte[] getCommand();
	
	abstract ConvertCommand.CmdTarget getTarget();

}
