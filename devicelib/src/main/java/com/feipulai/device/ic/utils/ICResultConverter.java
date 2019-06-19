package com.feipulai.device.ic.utils;

/**
 * Created by James on 2018/10/12 0012.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 * IC卡成绩值与数据库中成绩值的转换工具
 */
public class ICResultConverter{
	
	/**
	 * IC卡中项目成绩单位:
	 * <p>
	 * 项目名称       单位
	 * 身高体重      mm/0.1kg
	 * 肺活量        Ml(毫升)
	 * 立定跳远      cm
	 * 摸高          cm
	 * 俯卧撑	    个
	 * 仰卧起坐	    个
	 * 坐位体前屈	    mm
	 * 跳绳      	个
	 * 视力
	 * 引体向上      个
	 * 红外实心球    cm
	 * 中长跑	    毫秒
	 * 排球	        个
	 * 篮球运球	    毫秒
	 * 折返跑	    毫秒
	 * 1500米健步走	毫秒
	 * 2000米健步走	毫秒
	 * 50米	        毫秒
	 * 足球运球	    毫秒
	 * 踢毽子	    个
	 * 游泳	        毫秒
	 * <p>
	 * 数据库中保存成绩单位固定为:
	 * "毫米(mm)"、"毫秒(ms)"、"克(g)"、"次","毫升"
	 */
	/**
	 * 将IC卡中取出的成绩转换为数据库成绩格式,转化后的成绩可以通过保存在数据库中
	 * @param result IC卡中取出的成绩
	 * @param machineCode 成绩对应的机器码
	 * @param isWeight  该成绩是否为体重成绩
	 * @return 转化后的成绩
	 */
	public static int fromICToDB(int result,int machineCode,boolean isWeight){
		switch(machineCode){
			
			case ItemDefault.CODE_HW://身高体重
				// 体重 0.1kg -> g
				if(isWeight){
					return 100 * result;
				}else{
					// 身高
					return result;
				}
			
			case ItemDefault.CODE_FHL://肺活量
			case ItemDefault.CODE_FWC://俯卧撑
			case ItemDefault.CODE_YWQZ://仰卧起坐
			case ItemDefault.CODE_ZWTQQ://坐位体前屈
			case ItemDefault.CODE_TS://跳绳
			case ItemDefault.CODE_SL://视力
			case ItemDefault.CODE_YTXS://引体向上
			case ItemDefault.CODE_ZCP://中长跑(1000,800)
			case ItemDefault.CODE_PQ://排球
			case ItemDefault.CODE_LQYQ://篮球运球
			case ItemDefault.CODE_ZFP://折返跑
			case ItemDefault.CODE_1500MJBZ://1500米健步走
			case ItemDefault.CODE_2000MJBZ://2000米健步走
			case ItemDefault.CODE_50M://50米
			case ItemDefault.CODE_ZQYQ://足球运球
			case ItemDefault.CODE_TJZ://踢毽子
			case ItemDefault.CODE_YY://游泳
				return result;
			
			// cm -> mm
			case ItemDefault.CODE_LDTY://立定跳远
			case ItemDefault.CODE_MG://摸高
			case ItemDefault.CODE_HWSXQ://红外实心球
				return 10 * result;
			// 永远不该发生
			default:
				throw new IllegalArgumentException("illegal machineCode");
		}
	}
	
	/**
	 * 将数据库中取出的成绩转化为IC卡的格式,转化后的成绩可以通过IC卡模块写入IC卡
	 * @param result 数据库中取出的成绩
	 * @param machineCode 成绩对应的机器码
	 * @param isWeight  该成绩是否为体重成绩
	 * @return 转化后的成绩
	 */
	public static int fromDBToIC(int result,int machineCode,boolean isWeight){
		switch(machineCode){
			
			case ItemDefault.CODE_HW://身高体重
				// 体重 g->0.1kg
				if(isWeight){
					return result / 100;
				}else{
					// 身高
					return result;
				}
			
			case ItemDefault.CODE_FHL://肺活量
			case ItemDefault.CODE_FWC://俯卧撑
			case ItemDefault.CODE_YWQZ://仰卧起坐
			case ItemDefault.CODE_ZWTQQ://坐位体前屈
			case ItemDefault.CODE_TS://跳绳
			case ItemDefault.CODE_SL://视力
			case ItemDefault.CODE_YTXS://引体向上
			case ItemDefault.CODE_ZCP://中长跑(1000,800)
			case ItemDefault.CODE_PQ://排球
			case ItemDefault.CODE_LQYQ://篮球运球
			case ItemDefault.CODE_ZFP://折返跑
			case ItemDefault.CODE_1500MJBZ://1500米健步走
			case ItemDefault.CODE_2000MJBZ://2000米健步走
			case ItemDefault.CODE_50M://50米
			case ItemDefault.CODE_ZQYQ://足球运球
			case ItemDefault.CODE_TJZ://踢毽子
			case ItemDefault.CODE_YY://游泳
				return result;
			
			// cm -> mm
			case ItemDefault.CODE_LDTY://立定跳远
			case ItemDefault.CODE_MG://摸高
			case ItemDefault.CODE_HWSXQ://红外实心球
				return result / 10;
			
			// 永远不该发生
			default:
				throw new IllegalArgumentException("illegal machineCode");
		}
	}
	
}
