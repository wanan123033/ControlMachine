package com.feipulai.device.serial.parser;

import com.feipulai.device.serial.beans.RS232Result;

import java.io.IOException;
import java.io.InputStream;

public class GPSParser extends RS232Parser {
	
	@Override
	public RS232Result parse(InputStream is) throws IOException, InterruptedException {
		RS232Result result = null;
		byte[] head = new byte[2];
		while (is.available() < 2) {
			Thread.sleep(10);
		}
		is.read(head, 0, 2);
		if ((head[0] & 0xff) != 0xa5 || head[1] != 0x5a) {
			return null;
		}
		
		while (is.available() < 2) {
			Thread.sleep(10);
		}
		
		int command = is.read();
		int length = is.read();
		
		byte[] data = new byte[4 + length + 3];
		data[0] = (byte) 0xa5;
		data[1] = 0x5a;
		data[2] = (byte) command;
		data[3] = (byte) length;
		
		while (is.available() < length + 3) {
			Thread.sleep(10);
		}
		is.read(data, 4, length + 3);
		
		result = new RS232Result(data);
		return result;
	}
	
}
