package com.feipulai.device.ic.entity;

public class StuInfo{
	
	public static final int MALE = 0;
	public static final int FEMALE = 1;
	
	private String stuCode;		//准考证号
	private String stuName;		//姓名
	private int sex;			//性别    0--男  1--女
	
	public StuInfo(String stuCode, String stuName, int sex){
		this.stuCode = stuCode;
		this.stuName = stuName;
		this.sex = sex;
	}

	public String getStuCode() {
		return stuCode;
	}

	public void setStuCode(String stuCode) {
		this.stuCode = stuCode;
	}

	public String getStuName() {
		return stuName;
	}

	public void setStuName(String stuName) {
		this.stuName = stuName;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}
	
	@Override
	public String toString(){
		return "StuInfo{" +
				"stuCode='" + stuCode + '\'' +
				", stuName='" + stuName + '\'' +
				", sex=" + sex +
				'}';
	}
}
