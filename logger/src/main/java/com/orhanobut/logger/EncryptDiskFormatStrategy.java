package com.orhanobut.logger;

import android.os.Handler;
import android.os.HandlerThread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EncryptDiskFormatStrategy implements FormatStrategy{
	
	private Date date = new Date();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss",Locale.CHINA);
	private LogStrategy logStrategy;
	private String tag;
	
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String SEPARATOR = " ";
	
	public EncryptDiskFormatStrategy(String filePath,String password){
		
		HandlerThread ht = new HandlerThread("AndroidFileLogger." + filePath);
		ht.start();
		
		Handler handler = new EncryptDiskLogStrategy.WriteHandler(ht.getLooper(),filePath,password);
		this.logStrategy = new EncryptDiskLogStrategy(handler);
	}
	
	@Override
	public void log(int priority,String onceOnlyTag,String message){
		String tag = formatTag(onceOnlyTag);
		
		date.setTime(System.currentTimeMillis());
		
		StringBuilder builder = new StringBuilder();
		// time
		builder.append(dateFormat.format(date));
		// level
		builder.append(SEPARATOR);
		builder.append(Utils.logLevel(priority));
		// tag
		builder.append(SEPARATOR);
		builder.append(tag);
		
		// message
		builder.append(SEPARATOR);
		builder.append(message);
		
		// new line
		builder.append(NEW_LINE);
		
		logStrategy.log(priority,tag,builder.toString());
	}
	
	private String formatTag(String tag){
		if(!Utils.isEmpty(tag) && !Utils.equals(this.tag,tag)){
			return this.tag + "-" + tag;
		}
		return this.tag;
	}
	
}
