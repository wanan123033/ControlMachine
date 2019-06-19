package com.feipulai.host.activity.jump_rope.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * 作者 王伟
 * 公司 深圳菲普莱体育
 * 密级 绝密
 * Created on 2017/12/11.
 */

public class JumpRopeState implements Parcelable, Serializable{
	
	private static final long serialVersionUID = 671743232738444248L;
	private int handNumber;//手柄号 1-n
	private int handGroup;//手柄组 1-n
	//状态定义：
	//中断：-1(人为定义,不是跳绳返回结果)
	//复位：0  空闲：1    准备：2    计数：3    结束：4    休眠：5
	private int handState = -1;//手柄状态
	private double handPower;//手柄电量
	private int handResult;//手柄上记录的成绩
	
	public JumpRopeState(){
	}
	
	public JumpRopeState(int handNumber,int handGroup,int handState,int handPower,int handResult/*,String handHas*/){
		this.handNumber = handNumber;
		this.handGroup = handGroup;
		this.handState = handState;
		this.handPower = handPower;
		this.handResult = handResult;
	}
	
	public int getHandResult(){
		return handResult;
	}
	
	public void setHandResult(int handResult){
		this.handResult = handResult;
	}
	
	public int getHandState(){
		return handState;
	}
	
	public void setHandState(int handState){
		this.handState = handState;
	}
	
	public double getHandPower(){
		return handPower;
	}
	
	public void setHandPower(double handPower){
		this.handPower = handPower;
	}
	
	public int getHandGroup(){
		return handGroup;
	}
	
	public void setHandGroup(int handGroup){
		this.handGroup = handGroup;
	}
	
	public int getHandNumber(){
		return handNumber;
	}
	
	public void setHandNumber(int handNumber){
		this.handNumber = handNumber;
	}
	
	@Override
	public String toString(){
		return "HandState{" +
				"handNumber=" + handNumber +
				", handGroup=" + handGroup +
				", handState=" + handState +
				", handPower=" + handPower +
				", handResult=" + handResult +
				'}';
	}
	
	@Override
	public int describeContents(){
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest,int flags){
		dest.writeInt(this.handGroup);
		dest.writeInt(this.handNumber);
		dest.writeDouble(this.handPower);
		dest.writeInt(this.handResult);
		dest.writeInt(this.handState);
	}
	
	public static final Parcelable.Creator<JumpRopeState> CREATOR = new Creator<JumpRopeState>(){
		@Override
		public JumpRopeState createFromParcel(Parcel source){
			return new JumpRopeState(source.readInt(),source.readInt(),source.readInt(),source.readInt(),source.readInt()/*,source.readString()*/);
		}
		
		@Override
		public JumpRopeState[] newArray(int size){
			return new JumpRopeState[size];
		}
	};
	
}
