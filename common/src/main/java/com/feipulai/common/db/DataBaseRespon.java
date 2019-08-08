package com.feipulai.common.db;

/**
 * 数据库操作返回
 *
 * 
 */
public class DataBaseRespon {
	
	public DataBaseRespon(){};
	
	public DataBaseRespon(boolean isSuccess , String info , Object object){
		this.success = isSuccess;
		this.info = info;
		this.object = object;
	}

	/** 数据库操作是否成功 */
	private boolean success = false;
	
	/** 数据库操作描述 */
	private String info = "";

	/** 数据库操作返回数据 */
	private Object object;

	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	@Override
	public String toString() {
		return "DataBaseRespon [success=" + success + ", info=" + info + ", object=" + object + "]";
	}

	
	
}
