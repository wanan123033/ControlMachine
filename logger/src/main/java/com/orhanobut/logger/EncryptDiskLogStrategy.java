package com.orhanobut.logger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.orhanobut.logger.utils.DESTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class EncryptDiskLogStrategy implements LogStrategy{
	
	private final Handler handler;
	
	public EncryptDiskLogStrategy(Handler handler){
		this.handler = handler;
	}
	
	@Override
	public void log(int level,String tag,String message){
		handler.sendMessage(handler.obtainMessage(level,message));
	}
	
	static class WriteHandler extends Handler{
		
		private File logFile;
		private String password;
		
		WriteHandler(Looper looper,String filePath,String password){
			super(looper);
			logFile = new File(filePath);
			ensureFolderExists(logFile.getParent());
			this.password = password;
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
			FileOutputStream fos = null;
			try{
				fos = new FileOutputStream(logFile,true);
				
				// 每次对8个字节数据进行加密
				byte[] tmp = new byte[8];
				byte[] contentBytes = content.getBytes();
				int byteLength;
				for(int j = 0;j < contentBytes.length / 8;j++){
					if((j + 1) * 8 > contentBytes.length){
						Arrays.fill(tmp,(byte)0);
						byteLength = contentBytes.length - 8 * j;
					}else{
						byteLength = 8;
					}
					System.arraycopy(contentBytes,8 * j,tmp,0,byteLength);
					fos.write(DESTool.encrypt(tmp,password));
				}
				
				fos.flush();
				fos.close();
			}catch(IOException e){
				if(fos != null){
					try{
						fos.flush();
						fos.close();
					}catch(IOException e1){ /* fail silently */ }
				}
			}
		}
		
	}
	
}
