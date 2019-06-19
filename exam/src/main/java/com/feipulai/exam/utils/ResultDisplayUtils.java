package com.feipulai.exam.utils;

import com.feipulai.common.utils.ResultDisplayTools;
import com.feipulai.exam.config.TestConfigs;
import com.feipulai.exam.entity.Item;


/**
 * Created by James on 2018/4/25 0025.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ResultDisplayUtils{
	
	//private static final int BQS = 0x0;
	//private static final int SSWR = 0x01;
	//private static final int FLSQ = 0x02;
	//private static final int FLJW = 0x03;
	
	/**
	 * 当前测试项目成绩转换为显示格式
	 *
	 * @param dbResult 数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
	 * @return 可以用于显示的成绩字符串
	 */
	public static String getStrResultForDisplay(int dbResult){
		Item item = TestConfigs.sCurrentItem;
		return ResultDisplayTools.getStrResultForDisplay(item.getMachineCode(),dbResult,
				item.getDigital(),
				item.getCarryMode(),
				item.getUnit(),0,
				true);
	}
	
	/**
	 * 当前测试项目成绩转换为显示格式
	 *
	 * @param dbResult     数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、("次","毫升",这两个不需要在这里转换)
	 * @param isReturnUnit 是否带单位返回
	 * @return 可以用于显示的成绩字符串
	 */
	public static String getStrResultForDisplay(int dbResult,boolean isReturnUnit){
		Item item = TestConfigs.sCurrentItem;
		return ResultDisplayTools.getStrResultForDisplay(item.getMachineCode(),dbResult,
				item.getDigital(),
				item.getCarryMode(),
				item.getUnit(),0,
				isReturnUnit);
	}
	
	/**
	 * 当前测试项目成绩转换为显示格式
	 *
	 * @param dbResult 数据库中的原有数值,单位为"毫米(mm)"、"毫秒(ms)"、"克(g)"、("次","毫升",这两个不许要在这里转换)
	 * @param item     指定项目进行格式转换
	 * @return 可以用于显示的成绩字符串, 如果item.unit为空, 或者未找到对应的单位, 返回null
	 */
	public static String getStrResultForDisplay(int dbResult,Item item){
		return ResultDisplayTools.getStrResultForDisplay(item.getMachineCode(),dbResult,
				item.getDigital(),
				item.getCarryMode(),
				item.getUnit(),0,
				true);
	}
	
	/**
	 * 获得有效的成绩单位,首先检查数据库的单位,如果数据库的没有 或 不合格,则使用默认的单位
	 *
	 * @param machineCode 机器码
	 * @param unit        数据库中的单位
	 * @return 有效的成绩单位,获取成绩显示时会使用该单位
	 */
	public static String getQualifiedUnit(int machineCode,String unit){
		return ResultDisplayTools.getQualifiedUnit(machineCode,unit,0);
	}
	
	public static String getQualifiedUnit(Item item){
		return ResultDisplayTools.getQualifiedUnit(item.getMachineCode(),item.getUnit(),0);
	}
}
