package com.feipulai.device.ic.entity;

/**
 * Created by James on 2018/10/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * 扩展信息
 */

/**
 * 扩展区(存放单位等信息放在第60,61,62块),共48个字节
 * BYTE[0]---表示项目属性描述页所占块数 目前为6
 * BYTE[1]---表示考生考试项目数
 * byte[2]—byte[21]    单位,20个字节，10个汉字
 * byte[22]—byte[29]   学年,8个字节,2017090120180631,表示有效时间从2017年9月1日---2018年6月31日为有效测量时间,其余时间测量无效
 * 其中年--2个字节,月--1个字节,日---1个字节
 * byte[30]-byte[47]   18个字节，用于扩展信息,文档中写的是如果项目属性超过16个,这里存储项目属性页,但与小胖确认,最多16个项目
 */
public class ExpandInfo{
	
	private int itemPropertyBlockSum;
	private int testProjectSum;
	private String organization;
	private String schoolYear;
	
	public ExpandInfo(int itemPropertyBlockSum, int testProjectSum, String organization, String schoolYear){
		this.itemPropertyBlockSum = itemPropertyBlockSum;
		this.testProjectSum = testProjectSum;
		this.organization = organization;
		this.schoolYear = schoolYear;
	}
	
	public int getItemPropertyBlockSum(){
		return itemPropertyBlockSum;
	}
	
	public void setItemPropertyBlockSum(int itemPropertyBlockSum){
		this.itemPropertyBlockSum = itemPropertyBlockSum;
	}
	
	public int getTestProjectSum(){
		return testProjectSum;
	}
	
	public void setTestProjectSum(int testProjectSum){
		this.testProjectSum = testProjectSum;
	}
	
	public String getOrganization(){
		return organization;
	}
	
	public void setOrganization(String organization){
		this.organization = organization;
	}
	
	public String getSchoolYear(){
		return schoolYear;
	}
	
	public void setSchoolYear(String schoolYear){
		this.schoolYear = schoolYear;
	}
	
	@Override
	public String toString(){
		return "ExpandInfo{" +
				"itemPropertyBlockSum=" + itemPropertyBlockSum +
				", testProjectSum=" + testProjectSum +
				", organization='" + organization + '\'' +
				", schoolYear='" + schoolYear + '\'' +
				'}';
	}
	
}
