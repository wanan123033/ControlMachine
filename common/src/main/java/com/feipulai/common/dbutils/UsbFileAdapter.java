package com.feipulai.common.dbutils;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.mjdev.libaums.fs.UsbFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by James on 2019/1/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * 将File适配到UsbFile
 */
public class UsbFileAdapter implements UsbFile{
	
	private File file;
	
	public UsbFileAdapter(File file){
		this.file = file;
	}
	
	public File getFile(){
		return file;
	}
	
	@Nullable
	@Override
	public UsbFile search(@NonNull String path) throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public boolean isDirectory(){
		return file.isDirectory();
	}
	
	@Override
	public String getName(){
		return file.getName();
	}
	
	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}
	
	@Override
	public void setName(String newName) throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public long createdAt(){
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public long lastModified(){
		return file.lastModified();
	}
	
	@Override
	public long lastAccessed(){
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public UsbFile getParent(){
		return new UsbFileAdapter(file.getParentFile());
	}
	
	@Override
	public String[] list() throws IOException{
		return file.list();
	}
	
	@Override
	public UsbFile[] listFiles() throws IOException{
		File[] files = file.listFiles();
		if(files == null || files.length == 0){
			return new UsbFile[0];
		}
		UsbFile[] result = new UsbFile[files.length];
		int i = 0;
		for(File file : files){
			result[i++] = new UsbFileAdapter(file);
		}
		return result;
	}
	
	@Override
	public long getLength(){
		return file.length();
	}
	
	@Override
	public void setLength(long newLength) throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public void read(long offset,ByteBuffer destination) throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public void write(long offset,ByteBuffer source) throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public void flush() throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public void close() throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public UsbFile createDirectory(String name) throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public UsbFile createFile(String name) throws IOException{
		File file = new File(this.file,name);
		if(!file.createNewFile()){
			throw new IOException("文件已存在");
		}
		return new UsbFileAdapter(file);
	}
	
	@Override
	public void moveTo(UsbFile destination) throws IOException{
		throw new UnsupportedOperationException("not allowed");
	}
	
	@Override
	public void delete() throws IOException{
		file.delete();
	}
	
	@Override
	public boolean isRoot(){
		//Log.i("path",file.equals(Environment.getExternalStorageDirectory()) + "");
		return file.equals(Environment.getExternalStorageDirectory());
	}
	
}
