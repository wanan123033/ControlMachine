package com.orhanobut.logger;

import java.io.File;
import java.io.IOException;

public final class Logger{
	
	public static final int VERBOSE = 2;
	public static final int DEBUG = 3;
	public static final int INFO = 4;
	public static final int WARN = 5;
	public static final int ERROR = 6;
	public static final int ASSERT = 7;
	
	//the only guy that do everything
	private static Printer printer = new LoggerPrinter();
	
	private Logger(){
		//no instance
	}
	
	public static void printer(Printer printer){
		Logger.printer = printer;
	}
	
	public static void addLogAdapter(LogAdapter adapter){
		printer.addAdapter(adapter);
	}
	
	public static void clearLogAdapters(){
		printer.clearLogAdapters();
	}
	
	public static Printer t(String tag){
		return printer.t(tag);
	}
	
	public static void log(int priority,String tag,String message,Throwable throwable){
		printer.log(priority,tag,message,throwable);
	}
	
	public static void d(String message,Object... args){
		printer.d(message,args);
	}
	
	public static void d(Object object){
		printer.d(object);
	}
	
	public static void e(String message,Object... args){
		printer.e(null,message,args);
	}
	
	public static void e(Throwable throwable,String message,Object... args){
		printer.e(throwable,message,args);
	}
	
	public static void i(String message,Object... args){
		printer.i(message,args);
	}
	
	public static void v(String message,Object... args){
		printer.v(message,args);
	}
	
	public static void w(String message,Object... args){
		printer.w(message,args);
	}
	
	public static void wtf(String message,Object... args){
		printer.wtf(message,args);
	}
	
	public static void json(String json){
		printer.json(json);
	}
	
	public static void xml(String xml){
		printer.xml(xml);
	}
	
	/**
	 * 将当前应用的所有日志打印到指定文件中(原生格式),只在debug版本上有效
	 */
	public static void rawLogToFile(String filePath){
		File file = new File(filePath);
		File parentFile = file.getParentFile();
		if(!parentFile.exists() && !parentFile.mkdirs()){
			throw new RuntimeException("创建文件夹" + parentFile.getPath() + "失败");
		}
		try{
			Process process = Runtime.getRuntime().exec("logcat -f " + filePath);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
}
