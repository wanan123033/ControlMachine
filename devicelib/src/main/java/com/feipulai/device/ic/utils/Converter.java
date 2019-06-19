package com.feipulai.device.ic.utils;

import java.io.UnsupportedEncodingException;

/**
 * 帮助转换类(Convert help class)
 */
public class Converter{
	
	// Hex help
	private static final byte[] HEX_CHAR_TABLE = {(byte)'0',(byte)'1',
			(byte)'2',(byte)'3',(byte)'4',(byte)'5',(byte)'6',
			(byte)'7',(byte)'8',(byte)'9',(byte)'a',(byte)'b',
			(byte)'c',(byte)'d',(byte)'e',(byte)'f'};
	
	private static String hexString = "0123456789abcdef ";
	
	/**
	 * 整型转byte数组
	 */
	public static byte[] intToByteArray(int i){
		byte[] result = new byte[4];
		//为了避免正负号带来的影响,这里直接通过无符号移位
		result[0] = (byte)(i >>> 24);
		result[1] = (byte)((i >>> 16) & 0xff);
		result[2] = (byte)((i >>> 8) & 0xff);
		result[3] = (byte)(i & 0xFF);
		return result;
	}
	
	/**
	 * 将16进制数字解码成字符串,适用于所有字符（包括中文）
	 * modified by James 2018/4/27
	 */
	public static String decode(byte[] bytes){
		String s = null;
		try{
			s = new String(bytes,"GBK");
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return s;
	}
	
}
