package com.feipulai.host.exl;

/**
 * Created by James on 2018/2/2 0002.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ExelReadBean{
	
	private String studentCode;//准考证号
	private String studentName;//姓名
	private int sex;//性别
	private String itemName;//项目
	private String itemCode;//项目代码
	private String idCardNo;
	
	public String getStudentCode(){
		return studentCode;
	}
	
	public void setStudentCode(String studentCode){
		this.studentCode = studentCode;
	}
	
	public String getStudentName(){
		return studentName;
	}
	
	public void setStudentName(String studentName){
		this.studentName = studentName;
	}
	
	public int getSex(){
		return sex;
	}
	
	public void setSex(int sex){
		this.sex = sex;
	}
	
	public String getItemName(){
		return itemName;
	}
	
	public void setItemName(String itemName){
		this.itemName = itemName;
	}
	
	public String getItemCode(){
		return itemCode;
	}
	
	public void setItemCode(String itemCode){
		this.itemCode = itemCode;
	}
	
	public String getIdCardNo(){
		return idCardNo;
	}
	
	public void setIdCardNo(String idCardNo){
		this.idCardNo = idCardNo;
	}
	
}
