package com.feipulai.exam.config;


import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Item;

/**
 * Created by James on 2018/9/28 0028.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class HWConfigs {
	//这里写死
	public static final Item WEIGHT_ITEM = DBManager.getInstance().queryItemByName("身高");
	public static final Item HEIGHT_ITEM = DBManager.getInstance().queryItemByName("体重");
	
}
