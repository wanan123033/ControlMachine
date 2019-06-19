package com.feipulai.host.activity.jump_rope.bean;

/**
 * Created by James on 2018/8/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

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
