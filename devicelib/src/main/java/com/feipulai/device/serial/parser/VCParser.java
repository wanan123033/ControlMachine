package com.feipulai.device.serial.parser;


import com.feipulai.device.serial.beans.RS232Result;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by James on 2018/11/9 0009.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VCParser extends RS232Parser{
	
	@Override
	public RS232Result parse(InputStream is) throws IOException, InterruptedException {
		
		RS232Result result = null;
			byte[] head = new byte[2];
			while(is.available() < 1){
				Thread.sleep(10);
			}

			is.read(head,0,1);

			if((head[0] & 0xff) == 0xaa){
				while(is.available() < 1){
					Thread.sleep(10);
				}
				is.read(head,1,1);

				if((head[1] & 0xff) == 0xd0){
					byte[] data = new byte[5];
					data[0] = head[0];
					data[1] = head[1];
					while(is.available() < 3){
						Thread.sleep(10);
					}
					is.read(data,2,3);
					result = new RS232Result(data);
				}
			}
		return result;
	}
	
}
