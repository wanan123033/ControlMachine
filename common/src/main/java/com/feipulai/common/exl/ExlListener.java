package com.feipulai.common.exl;

/**
 * Created by James on 2018/11/1 0001.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public interface ExlListener{
	
	int EXEL_READ_SUCCESS = 0x0;// 读取导入成功
	int EXEL_READ_FAIL = 0x1;// 读取导入失败
	int EXEL_WRITE_SUCCESS = 0x4;// 导出成功
	int EXEL_WRITE_FAILED = 0x5;// 导出失败
	
	void onExlResponse(int responseCode, String reason);
	
}
