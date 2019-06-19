package com.orhanobut.logger.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DecryptLogUtils{
	
	private DecryptLogUtils(){}
	
	/**
	 * 将文件夹下以".log"结尾的文件按照指定的公钥值解密
	 *
	 * @param fold 文件夹，如果不是文件夹，抛出异常
	 * @param key  指定公钥值
	 * @return 如果解密成功,返回true，失败则返回false
	 */
	public static boolean decrypt(String fold,byte key){
		
		File folder = new File(fold);
		
		if(!folder.exists()){
			throw new IllegalArgumentException("the specified folder:" + folder + "doesn't exists");
		}
		
		//Log.e("folder",folder.toString());
		
		File[] files = folder.listFiles();
		List<File> logFiles = new ArrayList<>();
		
		//Log.e("Arrays.toString(files)",Arrays.toString(files));
		
		for(int i = 0;i < files.length;i++){
			String fileName = files[i].getName();
			if(fileName.endsWith(".log") && !fileName.startsWith("decrypt")){
				logFiles.add(files[i]);
			}
		}
		
		for(File file : logFiles){
			
			//Log.e("james",folder.getPath() + "/decrypt" + file.getName());
			File targetFile = new File(folder.getPath() + "/decrypt" + file.getName());
			
			if(logFiles.contains(targetFile)){
				continue;
			}
			
			try{
				FileInputStream fis = new FileInputStream(file);
				FileOutputStream fos = new FileOutputStream(targetFile,true);
				byte[] readbBuffer = new byte[1024];
				int readNumber;
				while((readNumber = fis.read(readbBuffer)) != -1){
					for(int i = 0;i < readbBuffer.length;i++){
						readbBuffer[i] ^= key;
					}
					fos.write(readbBuffer,0,readNumber);
					fos.flush();
				}
				fos.close();
			}catch(FileNotFoundException e){
				e.printStackTrace();
				return false;
			}catch(IOException e){
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
}
