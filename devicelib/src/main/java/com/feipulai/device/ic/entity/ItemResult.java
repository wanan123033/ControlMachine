package com.feipulai.device.ic.entity;

import java.util.Arrays;

/**
 * Created by James on 2018/10/11 0011.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class ItemResult{
	
	public static final int RESULT_STATE_INVALID = 0;
	public static final int RESULT_STATE_VALID = 1;
	
	public static final int RESULT_STATE_UN_FOUL = 0;
	public static final int RESULT_STATE_FOUL = 1;
	
	private int machineCode;// 机器码
	/**
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
	 * 当为身高体重成绩时
	 * result[2n]为第n次测试身高成绩,result[2n + 1]为为第n次测试体重成绩
	 * <p>
	 * 当为视力成绩时:
	 * result[2n]为第n次测试左眼视力成绩,result[2n + 1]为第n次测试右眼视力成绩
	 * <p>
	 * 其他参数同上述规则,形成一一对应
	 * 每个数组的长度均相同为
	 */
	private int[] result;// 成绩
	private int[] validState;// 0:无效  1:有效
	private int[] foulState;// 0:不犯规 1:犯规
	// 判罚值	只有仰卧起坐/俯卧撑/排球有判罚值  真实成绩 = 计数值 + 判罚值(犯规时为负,漏计时为正),  已与小胖确认
	// 虽然只在仰卧起坐/俯卧撑/排球有使用到判罚值,但是真实成绩的算法可以统一,因为不使用的项目该项均为0
	private int[] penalValue;
	private int[] retain;        //拓展位    暂时不使用
	
	public ItemResult(int machineCode,int[] result,int[] validState,int[] foulState,int[] penalValue,int[] retain){
		this.machineCode = machineCode;
		this.result = result;
		this.validState = validState;
		this.foulState = foulState;
		this.penalValue = penalValue;
		this.retain = retain;
	}
	
	public int getMachineCode(){
		return machineCode;
	}
	
	public void setMachineCode(int machineCode){
		this.machineCode = machineCode;
	}
	
	public int[] getResult(){
		return result;
	}
	
	public void setResult(int[] result){
		this.result = result;
	}
	
	public int[] getValidState(){
		return validState;
	}
	
	public void setValidState(int[] validState){
		this.validState = validState;
	}
	
	public int[] getFoulState(){
		return foulState;
	}
	
	public void setFoulState(int[] foulState){
		this.foulState = foulState;
	}
	
	public int[] getPenalValue(){
		return penalValue;
	}
	
	public void setPenalValue(int[] penalValue){
		this.penalValue = penalValue;
	}
	
	public int[] getRetain(){
		return retain;
	}
	
	public void setRetain(int[] retain){
		this.retain = retain;
	}
	
	@Override
	public String toString(){
		return "ItemResult{" +
				"machineCode=" + machineCode +
				", result=" + Arrays.toString(result) +
				", validState=" + Arrays.toString(validState) +
				", foulState=" + Arrays.toString(foulState) +
				", penalValue=" + Arrays.toString(penalValue) +
				", retain=" + Arrays.toString(retain) +
				'}';
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(!(o instanceof ItemResult)){
			return false;
		}
		
		ItemResult that = (ItemResult)o;
		
		if(getMachineCode() != that.getMachineCode()){
			return false;
		}
		if(!Arrays.equals(getResult(),that.getResult())){
			return false;
		}
		if(!Arrays.equals(getValidState(),that.getValidState())){
			return false;
		}
		if(!Arrays.equals(getFoulState(),that.getFoulState())){
			return false;
		}
		if(!Arrays.equals(getPenalValue(),that.getPenalValue())){
			return false;
		}
		return Arrays.equals(getRetain(),that.getRetain());
	}
	
	@Override
	public int hashCode(){
		int result1 = getMachineCode();
		result1 = 31 * result1 + Arrays.hashCode(getResult());
		result1 = 31 * result1 + Arrays.hashCode(getValidState());
		result1 = 31 * result1 + Arrays.hashCode(getFoulState());
		result1 = 31 * result1 + Arrays.hashCode(getPenalValue());
		result1 = 31 * result1 + Arrays.hashCode(getRetain());
		return result1;
	}
}
