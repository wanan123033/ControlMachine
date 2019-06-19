package com.orhanobut.logger;

import android.os.Handler;
import android.os.HandlerThread;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AndroidDiskFormatStrategy implements FormatStrategy{
	
	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final String SEPARATOR = " ";
	
	private final Date date;
	private final SimpleDateFormat dateFormat;
	private final LogStrategy logStrategy;
	private final String tag;
	
	private AndroidDiskFormatStrategy(Builder builder){
		date = builder.date;
		dateFormat = builder.dateFormat;
		logStrategy = builder.logStrategy;
		tag = builder.tag;
	}
	
	public static Builder newBuilder(){
		return new Builder();
	}
	
	@Override
	public void log(int priority,String onceOnlyTag,String message){
		String tag = formatTag(onceOnlyTag);
		
		date.setTime(System.currentTimeMillis());
		
		StringBuilder builder = new StringBuilder();
		
		builder.append(dateFormat.format(date));
		builder.append(SEPARATOR);
		
		// log level
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
	
	public static final class Builder{
		Date date;
		SimpleDateFormat dateFormat;
		LogStrategy logStrategy;
		String tag = "DiskLogger";
		String filePath;
		
		private Builder(){
		}
		
		public Builder date(Date val){
			date = val;
			return this;
		}
		
		public Builder dateFormat(SimpleDateFormat val){
			dateFormat = val;
			return this;
		}
		
		public Builder logStrategy(LogStrategy val){
			logStrategy = val;
			return this;
		}
		
		public Builder tag(String tag){
			this.tag = tag;
			return this;
		}
		
		public AndroidDiskFormatStrategy build(){
			if(date == null){
				date = new Date();
			}
			if(dateFormat == null){
				dateFormat = new SimpleDateFormat("yyyy/MM/dd  HH:mm:ss",Locale.CHINA);
			}
			
			if(logStrategy == null){
				
				HandlerThread ht = new HandlerThread("AndroidFileLogger." + filePath);
				ht.start();
				
				Handler handler = new DiskLogStrategy.WriteHandler(ht.getLooper(),filePath);
				logStrategy = new DiskLogStrategy(handler);
			}
			return new AndroidDiskFormatStrategy(this);
		}
		
		public Builder path(String filePah){
			this.filePath = filePah;
			return this;
		}
	}
	
}
