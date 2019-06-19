package com.feipulai.common.dbutils;

import com.github.mjdev.libaums.fs.UsbFile;

public class FileItemBean{
	
	// 显示的文件名称
	private String name;
	private UsbFile file;
	//private boolean isChecked;
	
	public FileItemBean(String name,UsbFile file/*,boolean isChecked*/){
		this.name = name;
		this.file = file;
		//this.isChecked = isChecked;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	//public boolean isChecked(){
	//	return isChecked;
	//}
	//
	//public void setChecked(boolean checked){
	//	isChecked = checked;
	//}
	
	public UsbFile getFile(){
		return file;
	}
	
	public void setFile(UsbFile file){
		this.file = file;
	}
	
}