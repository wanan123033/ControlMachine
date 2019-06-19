package com.feipulai.device.serial.parser;


import com.feipulai.device.serial.beans.RS232Result;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by James on 2018/11/9 0009.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public abstract class RS232Parser{
	
	public abstract RS232Result parse(InputStream is) throws InterruptedException,IOException;
	
}
