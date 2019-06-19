package com.orhanobut.logger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Abstract class that takes care of background threading the logFile log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 */
public class DiskLogStrategy implements LogStrategy{
	
	private final Handler handler;
	
	public DiskLogStrategy(Handler handler){
		this.handler = handler;
	}
	
	@Override
	public void log(int level,String tag,String message){
		handler.sendMessage(handler.obtainMessage(level,message));
	}
	
	static class WriteHandler extends Handler{
		
		private File logFile;
		
		WriteHandler(Looper looper,String filePath){
			super(looper);
			logFile = new File(filePath);
			ensureFolderExists(logFile.getParent());
		}
		
		private void ensureFolderExists(String folder){
			File fold = new File(folder);
			if(!fold.exists()){
				fold.mkdirs();
			}
		}
		
		@Override
		public void handleMessage(Message msg){
			writeLog((String)msg.obj);
		}
		
		protected void writeLog(String content){
			FileWriter fileWriter = null;
			try{
				fileWriter = new FileWriter(logFile,true);
				fileWriter.append(content);
				fileWriter.flush();
				fileWriter.close();
			}catch(IOException e){
				if(fileWriter != null){
					try{
						fileWriter.flush();
						fileWriter.close();
					}catch(IOException e1){ /* fail silently */ }
				}
			}
		}
		
	}
	
}