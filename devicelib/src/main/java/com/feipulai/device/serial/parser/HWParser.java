package com.feipulai.device.serial.parser;


import com.feipulai.device.serial.beans.RS232Result;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by James on 2018/11/9 0009.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class HWParser extends RS232Parser{
	
	// 考虑到身高体重的数据更新很慢
	@Override
	public RS232Result parse(InputStream is) throws IOException, InterruptedException {
		RS232Result result = null;
		byte[] data = new byte[17];
		while(is.available() < 1){
			Thread.sleep(10);
		}
		is.read(data,0,1);
		
		if(data[0] == 'W'){
			while(is.available() < 16){
				Thread.sleep(10);
			}
			is.read(data,1,16);
			
			result = new RS232Result(data);
		}
		return result;
	}
	
}
