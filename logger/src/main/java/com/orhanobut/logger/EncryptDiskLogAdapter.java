package com.orhanobut.logger;


public class EncryptDiskLogAdapter implements LogAdapter{
	
	private final FormatStrategy mFormatStrategy;
	
	/**
	 * @param filePath      日志保存的文件路径
	 * @param password      日志的加密公钥
	 */
	public EncryptDiskLogAdapter(String filePath,String password){
		mFormatStrategy = new EncryptDiskFormatStrategy(filePath,password);
	}
	
	@Override
	public boolean isLoggable(int priority,String tag){
		return true;
	}
	
	@Override
	public void log(int priority,String tag,String message){
		mFormatStrategy.log(priority,tag,message);
	}
	
}
