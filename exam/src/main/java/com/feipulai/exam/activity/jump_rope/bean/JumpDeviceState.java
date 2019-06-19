package com.feipulai.exam.activity.jump_rope.bean;

public class JumpDeviceState extends BaseDeviceState{
	
	private static final long serialVersionUID = 7141756086497273333L;
	
	public static final int INVALID_FACTORY_ID = -1;
	private int factoryId = INVALID_FACTORY_ID;
	
	public int getFactoryId(){
		return factoryId;
	}
	
	public void setFactoryId(int factoryId){
		this.factoryId = factoryId;
	}
	
}
