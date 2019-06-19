package com.feipulai.device.ic.entity;

/**
 * 项目属性页
 * 机器码 (1字节)
 * 存储地址起始块号 (1字节)
 * 所占存储块数 (1字节)
 * 组次 (2字节)
 * 道次 (1字节)
 */
public class ItemProperty{
	
	private int machineCode;    //机器码
	private int startBlockNo;   //起始块号
	private int BlockNum;    //所需存储块数
	private int GroupNum;    //组次
	private int TrackNum;    //道次
	
	public ItemProperty(int machineCode,int startBlockNo,int blockNum,int groupNum,int trackNum){
		this.machineCode = machineCode;
		this.startBlockNo = startBlockNo;
		BlockNum = blockNum;
		GroupNum = groupNum;
		TrackNum = trackNum;
	}
	
	public int getMachineCode(){
		return machineCode;
	}
	
	public void setMachineCode(int machineCode){
		this.machineCode = machineCode;
	}
	
	public int getStartBlockNo(){
		return startBlockNo;
	}
	
	public void setStartBlockNo(int startBlockNo){
		this.startBlockNo = startBlockNo;
	}
	
	public int getBlockNum(){
		return BlockNum;
	}
	
	public void setBlockNum(int blockNum){
		BlockNum = blockNum;
	}
	
	public int getGroupNum(){
		return GroupNum;
	}
	
	public void setGroupNum(int groupNum){
		GroupNum = groupNum;
	}
	
	public int getTrackNum(){
		return TrackNum;
	}
	
	public void setTrackNum(int trackNum){
		TrackNum = trackNum;
	}
	
	@Override
	public String toString(){
		return "ItemProperty [machineCode=" + machineCode + ", startBlockNo=" + startBlockNo
				+ ", BlockNum=" + BlockNum + ", GroupNum=" + GroupNum
				+ ", TrackNum=" + TrackNum + "]";
	}
	
	@Override
	public boolean equals(Object o){
		if(this == o){
			return true;
		}
		if(!(o instanceof ItemProperty)){
			return false;
		}
		
		ItemProperty that = (ItemProperty)o;
		
		if(getMachineCode() != that.getMachineCode()){
			return false;
		}
		if(getStartBlockNo() != that.getStartBlockNo()){
			return false;
		}
		if(getBlockNum() != that.getBlockNum()){
			return false;
		}
		if(getGroupNum() != that.getGroupNum()){
			return false;
		}
		return getTrackNum() == that.getTrackNum();
	}
	
	@Override
	public int hashCode(){
		int result = getMachineCode();
		result = 31 * result + getStartBlockNo();
		result = 31 * result + getBlockNum();
		result = 31 * result + getGroupNum();
		result = 31 * result + getTrackNum();
		return result;
	}
}
