package com.feipulai.device.serial.beans;

import com.orhanobut.logger.utils.LogUtils;

/**
 * Created by James on 2018/4/10 0010.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class HeightWeightResult{
	// 单位:  kg  cm
	private double weight;
	private double height;
	
	public HeightWeightResult(byte[] data){
		String raw = new String(data);
		//Logger.e(raw);
		weight = Double.parseDouble(raw.substring(2,7));
		height = Double.parseDouble(raw.substring(10,15));
		LogUtils.normal("身高体重返回数据(解析前):"+data.length+"---"+StringUtility.bytesToHexString(data)+"---\n(解析后):"+toString());
	}
	
	// 测试用
	public HeightWeightResult(double weight,double height){
		this.weight = weight;
		this.height = height;
	}
	
	public double getHeight(){
		return height;
	}
	
	public void setHeight(double height){
		this.height = height;
	}
	
	public double getWeight(){
		return weight;
	}
	
	public void setWeight(double weight){
		this.weight = weight;
	}
	
	@Override
	public String toString(){
		return "HeightWeightResult{" +
				"height=" + height +
				", weight=" + weight +
				'}';
	}
	
}
